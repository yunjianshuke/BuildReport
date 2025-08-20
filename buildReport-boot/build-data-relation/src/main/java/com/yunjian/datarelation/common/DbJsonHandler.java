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

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.enums.DatasouceTypeEnum;
import lombok.SneakyThrows;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author yujian
 **/
@MappedTypes({BaseOptions.class})
public class DbJsonHandler extends BaseTypeHandler<BaseOptions> {

    private static final Map<Integer, Class<? extends BaseOptions>> DRIVER_MAP;
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        DRIVER_MAP = MapBuilder.<Integer, Class<? extends BaseOptions>>create()
                .put(DatasouceTypeEnum.MYSQL.getType(), MysqlOptions.class)
                .put(DatasouceTypeEnum.ORACLE.getType(), OracleOptions.class)
                .put(DatasouceTypeEnum.POSTGRESQL.getType(), PostgresqlOptions.class)
                .build();
        OBJECT_MAPPER = SpringUtil.getBean(ObjectMapper.class);
    }

    @SneakyThrows({JsonProcessingException.class})
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, BaseOptions parameter,
                                    JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, OBJECT_MAPPER.writeValueAsString(parameter));
    }

    @Override
    public BaseOptions getNullableResult(ResultSet rs, String columnName) throws SQLException {

        String jsonString = rs.getString(columnName);
        try {
            return OBJECT_MAPPER.readValue(jsonString, getDriver(rs));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BaseOptions getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String jsonString = rs.getString(columnIndex);
        try {
            return OBJECT_MAPPER.readValue(jsonString, getDriver(rs));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BaseOptions getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        throw new BusinessException("0", "不支持的实现方式");
    }


    public Class<? extends BaseOptions> getDriver(ResultSet rs) throws SQLException {
        final Integer type = rs.getInt("type");

        if (ObjectUtil.isNull(type)) {
            throw new IllegalArgumentException("数据库类型");
        }

        return DRIVER_MAP.get(type);
    }
}
