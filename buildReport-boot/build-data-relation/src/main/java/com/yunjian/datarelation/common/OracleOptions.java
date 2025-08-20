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

import javax.validation.constraints.NotBlank;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author yujian
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class OracleOptions extends BaseOptions {

    private static final String JDBC_ORACLE_URL_TEMPLATE = "jdbc:oracle:thin:@{}:{}:{}";

    @NotBlank(groups = CreateGroup.class, message = "sid不能为空")
    private String sid;

    @Override
    public boolean testConnection() {
        Optional<Connection> connection = Optional.empty();
        try (SimpleDataSource simpleDataSource = new SimpleDataSource(jdbcUrl(), userName, password,
                driver)) {
            simpleDataSource.addConnProps("oracle.net.CONNECT_TIMEOUT", "10000");
            connection = Optional.ofNullable(simpleDataSource.getConnection());

        } catch (Exception e) {
            log.error("oracle 测试连接失败 : {}" + JSONUtil.toJsonPrettyStr(this), e.getMessage());
            return false;
        } finally {
            connection
                    .ifPresent(JdbcUtils::closeConnection);
        }
        return true;
    }

    @Override
    public String jdbcUrl() {
        return StrUtil.format(JDBC_ORACLE_URL_TEMPLATE, host, port, sid);
    }

    @Override
    public String validationQuerySql() {
        return "SELECT 'x' FROM DUAL";
    }

    @Override
    public List<AnyData> privew(String tableName, Integer limit) {
        if (limit == null) {
            limit = 100;
        }
        return template.queryForList(
                "select * from " + database + "." + tableName + " where ROWNUM = " + limit,
                Collections.emptyMap()).stream().map(AnyData::new).collect(Collectors.toList());
    }

    @Override
    public List<TableDTO> allTables(String tableName) {

        if (StrUtil.isBlank(database)) {
            throw new BusinessException("0", "获取表时,服务名不能为空.");
        }
        Map<String, String> columnSchema = SchemaUtils.getOracleTableSchema(database, tableName,
                template);

        return columnSchema.entrySet().stream()
                .map(map -> new TableDTO(map.getKey(), map.getValue(), null, ""))
                .collect(Collectors.toList());
    }

    @Override
    public List<DbFieldDTO> tableFields(String tableName) {
        if (StrUtil.isBlank(tableName)) {
            throw new BusinessException("0", "获取表字段时,表名称不能为空.");
        }

        return template.query(StrUtil.format("select * from {} where ROWNUM = 1", StrUtil.format("{}.{}", database, tableName)),
                Collections.emptyMap(), rs -> {
                    return buildFieldsFunction.apply(tableName, DbType.oracle, rs);
                });
    }
}
