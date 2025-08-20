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
package com.yunjian.datarelation.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.enums.SqlLike;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author yujian
 */
public class SchemaUtils {

    private SchemaUtils() {
    }

    private static final String MYSQL_TABLE_SQL, MYSQL_COLUMN_SQL, ORACLE_TABLE_SQL, ORACLE_TABLE_SQL_FOR_SEARCH, ORACLE_COLUMN_SQL;

    static {
        MYSQL_TABLE_SQL = "select TABLE_COMMENT,TABLE_NAME from information_schema.TABLES where TABLE_SCHEMA = :dbName";
        MYSQL_COLUMN_SQL = "select COLUMN_COMMENT,COLUMN_NAME,TABLE_NAME from information_schema.COLUMNS where TABLE_NAME = :tableName and TABLE_SCHEMA=:database";
        // oracle 为什么做了100条限制 是因为一个用户视图下有几十万张表 需要查很久 所以默认100条 如果没有找到你需要的 则需要按下面模糊搜索sql查找
        ORACLE_TABLE_SQL = "select table_name, comments FROM all_tab_comments  where owner= :dbName and ROWNUM <= 100 ORDER BY TABLE_NAME";
        ORACLE_TABLE_SQL_FOR_SEARCH = "select table_name, comments FROM all_tab_comments  where owner= :dbName and regexp_like( table_name ,:searchTableName,'i')  ORDER BY TABLE_NAME";
        ORACLE_COLUMN_SQL = "select DISTINCT COLUMN_NAME,COMMENTS from all_col_comments WHERE Table_Name= :tableName AND OWNER= :dbName";
    }


    public static Map<String, String> getMysqlTableSchema(String dbName,
                                                          NamedParameterJdbcTemplate jdbcTemplate) {
        if (StrUtil.isBlank(dbName)) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> schemas = jdbcTemplate.queryForList(
                MYSQL_TABLE_SQL,
                Collections.singletonMap("dbName", dbName));
        return schemas.stream().collect(Collectors.toMap(k -> (String) k.get("TABLE_NAME"),
                v -> (String) v.getOrDefault("TABLE_COMMENT", "")));
    }

    public static Map<String, String> getOracleTableSchema(String dbName, String searchTableName,
                                                           NamedParameterJdbcTemplate jdbcTemplate) {
        List<Map<String, Object>> schemas;
        if (StrUtil.isBlank(searchTableName)) {
            schemas = jdbcTemplate.queryForList(ORACLE_TABLE_SQL,
                    Collections.singletonMap("dbName", dbName));
        } else {
            schemas = jdbcTemplate.queryForList(ORACLE_TABLE_SQL_FOR_SEARCH,
                    MapUtil.<String, String>builder().put("dbName", dbName)
                            .put("searchTableName", searchTableName)
                            .build());
        }

        return schemas.stream().collect(Collectors.toMap(k -> (String) k.get("TABLE_NAME"),
                v -> (String) Optional.ofNullable(v.get("COMMENTS")).orElse("")));
    }

    public static Map<String, String> getMysqlColumnSchema(String database, String tableName,
                                                           NamedParameterJdbcTemplate template) {
        if (StrUtil.isBlank(tableName) || ObjectUtil.isNull(template)) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> schemas = template.queryForList(
                MYSQL_COLUMN_SQL,
                MapUtil.<String, String>builder().put("tableName", tableName)
                        .put("database", database).build()
        );
        return schemas.stream().collect(Collectors.toMap(k -> (String) k.get("COLUMN_NAME"),
                v -> (String) v.getOrDefault("COLUMN_COMMENT", "")));
    }

    public static Map<String, String> getOracleColumnSchema(String dbName, String tableName,
                                                            NamedParameterJdbcTemplate template) {
        if (StrUtil.isBlank(tableName) || ObjectUtil.isNull(template)) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> schemas = template.queryForList(
                ORACLE_COLUMN_SQL,
                MapUtil.<String, String>builder().put("tableName", tableName)
                        .put("dbName", tableName)
                        .build()
        );
        return schemas.stream().collect(Collectors.toMap(k -> (String) k.get("COLUMN_NAME"),
                v -> (String) Optional.ofNullable(v.get("COMMENTS")).orElse("")));
    }
}
