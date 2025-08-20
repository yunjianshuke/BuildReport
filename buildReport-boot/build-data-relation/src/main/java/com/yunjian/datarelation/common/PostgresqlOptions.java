/*
 * Copyright 2025-2030 大连云建数科科技有限公司.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yunjian.datarelation.common;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.ds.simple.SimpleDataSource;
import cn.hutool.json.JSONUtil;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.dto.DbFieldDTO;
import com.yunjian.datarelation.dto.TableDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * postgresql 16.x+
 *
 * @author yujian
 **/
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresqlOptions extends BaseOptions {

    private static final String JDBC_POSTGRESQL_URL_TEMPLATE = "jdbc:postgresql://{}:{}/{}";
    private static final String SHOW_ALL_TABLE =
            "SELECT relname, obj_description(relfilenode, 'pg_class') AS description\n"
                    + "FROM pg_class\n"
                    + "WHERE relkind = 'r' AND relname NOT LIKE 'pg_%' AND relname NOT LIKE 'sql_%' and relname like '%{}%';";
    private static final String SHOW_ALL_TABLE_NOT_LIEK =
            "SELECT relname, obj_description(relfilenode, 'pg_class') AS description\n"
                    + "FROM pg_class\n"
                    + "WHERE relkind = 'r' AND relname NOT LIKE 'pg_%' AND relname NOT LIKE 'sql_%' ;";
    private static final String SHOW_TABLE_FIELDS = "SELECT\n"
            + "a.attname as name,\n"
            + "format_type(a.atttypid,a.atttypmod) as type,\n"
            + " col_description(a.attrelid,a.attnum) as remark\n"
            + "FROM\n"
            + "pg_class as c,pg_attribute as a\n"
            + "where\n"
            + "a.attrelid = c.oid\n"
            + "and\n"
            + "a.attnum>0\n"
            + "and\n"
            + "c.relname = '{}';";

    @Override
    public boolean testConnection() {
        Optional<Connection> connection = Optional.empty();
        try (SimpleDataSource simpleDataSource = new SimpleDataSource(jdbcUrl(), userName, password,
                driver)) {
            simpleDataSource.addConnProps("connectTimeout", "10000");
            connection = Optional.ofNullable(simpleDataSource.getConnection());

        } catch (Exception e) {
            log.warn("postgresql 测试连接失败 : {}" + JSONUtil.toJsonPrettyStr(this), e.getMessage());
            return false;
        } finally {
            connection
                    .ifPresent(JdbcUtils::closeConnection);
        }

        return true;
    }

    @Override
    public String jdbcUrl() {
        return StrUtil.format(JDBC_POSTGRESQL_URL_TEMPLATE, host, port, database);
    }

    @Override
    public String validationQuerySql() {
        return " SELECT 'x' ";
    }

    @Override
    public List<AnyData> privew(String tableName, Integer limit) {
        if (limit == null) {
            limit = 100;
        }
        return template.queryForList(
                        "select * from " + tableName + " limit " + limit,
                        Collections.emptyMap()).stream().map(AnyData::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<TableDTO> allTables(String tableName0) {

        if (ObjectUtil.isNull(template)) {
            throw new BusinessException("0", "使用DatasourcePoolManger来进行调用");
        }

        if (StrUtil.isBlank(database)) {
            throw new BusinessException("0", "获取表时,数据库不能为空" + database);
        }

        String sql = StrUtil.isBlank(tableName0) ? SHOW_ALL_TABLE_NOT_LIEK : StrUtil.format(SHOW_ALL_TABLE, tableName0);

        return template.queryForList(sql, Collections.emptyMap()).stream()
                .map(map -> {
                    String tableName = (String) map.get("relname");
                    String comment = (String) map.get("description");
                    return new TableDTO(tableName, comment, null, "");
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DbFieldDTO> tableFields(String tableName) {
        return template.queryForList(StrUtil.format(SHOW_TABLE_FIELDS, tableName),
                        Collections.emptyMap()).stream()
                .map(map -> {
                    String name = (String) map.get("name");
                    String type = (String) map.get("type");
                    String comment = (String) map.get("remark");
                    return new DbFieldDTO(name, comment, type);
                })
                .collect(Collectors.toList());
    }
}
