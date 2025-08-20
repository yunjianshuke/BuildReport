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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.ds.simple.SimpleDataSource;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.DbType;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.dto.DbFieldDTO;
import com.yunjian.datarelation.dto.TableDTO;
import com.yunjian.datarelation.utils.SchemaUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author yujian
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class MysqlOptions extends BaseOptions {

    /**
     * 如果你不清楚这些参数的含义,请不要随意改动
     */
    private static final String JDBC_MYSQL_URL_TEMPLATE = "jdbc:mysql://{}:{}/{}?connectTimeout=5000&socketTimeout=60000&autoReconnect=false&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048&useServerPrepStmts=true&useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true";
    private static final String SHOW_TABLES =
            "SELECT \n"
                    + "    table_name, \n"
                    + "    table_comment \n"
                    + "FROM \n"
                    + "    information_schema.tables \n"
                    + "WHERE \n"
                    + "    table_schema = '{}' \n"
                    + "    AND (table_name LIKE '%{}%' OR table_comment LIKE '%{}%');";
    private static final String SHOW_TABLES_NOT_LIKE = "show tables ";

    @Override
    public boolean testConnection() {
        Optional<Connection> connection = Optional.empty();
        try (SimpleDataSource simpleDataSource = new SimpleDataSource(jdbcUrl(), userName, password,
                driver)) {
            simpleDataSource.addConnProps("connectTimeout", "10000");
            connection = Optional.ofNullable(simpleDataSource.getConnection());

        } catch (Exception e) {
            log.error("mysql 测试连接失败 : {}" + JSONUtil.toJsonPrettyStr(this), e.getMessage());
            return false;
        } finally {
            connection
                    .ifPresent(JdbcUtils::closeConnection);
        }

        return true;
    }

    @Override
    public String jdbcUrl() {
        //jdbc:mysql://${MYSQL_HOST:build-mysql}:${MYSQL_PORT:3306}/${MYSQL_DB:build_config_replaceTarget}?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true&allowPublicKeyRetrieval=true
        return StrUtil.format(JDBC_MYSQL_URL_TEMPLATE, host, port, database);
    }

    @Override
    public String validationQuerySql() {
        return "SELECT 1";
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
        Map<String, String> columnSchema = SchemaUtils.getMysqlTableSchema(database, template);

        String sql = StrUtil.isBlank(tableName0) ? SHOW_TABLES_NOT_LIKE : StrUtil.format(SHOW_TABLES, database, tableName0, tableName0);

        return template.queryForList(sql, Collections.emptyMap()).stream()
                .map(map -> {
                    String tableName = String.valueOf(CollUtil.getFirst(map.values()));
                    return new TableDTO(tableName, columnSchema.getOrDefault(tableName, ""), null, "");
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<DbFieldDTO> tableFields(String tableName) {
        if (ObjectUtil.isNull(template)) {
            throw new BusinessException("0", "使用DatasourcePoolManger来进行调用");
        }
        if (StrUtil.isBlank(database)) {
            throw new BusinessException("0", "获取表字段时,数据库不能为空.");
        }
        if (StrUtil.isBlank(tableName)) {
            throw new BusinessException("0", "取表字段时,表名称不能为空.");
        }
        return template.query(StrUtil.format("select * from `{}` limit 1", tableName),
                Collections.emptyMap(), rs -> {
                    return buildFieldsFunction.apply(tableName, DbType.mysql, rs);
                });
    }
}
