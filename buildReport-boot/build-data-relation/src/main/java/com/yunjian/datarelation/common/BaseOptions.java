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

import com.alibaba.druid.DbType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.config.ReportJdbcTemplate;
import com.yunjian.datarelation.dto.DbFieldDTO;
import com.yunjian.datarelation.entity.Datasource;
import com.yunjian.datarelation.utils.SchemaUtils;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import reactor.function.Function3;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yujian
 **/
@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "driver",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MysqlOptions.class, name = "com.mysql.jdbc.Driver"),
        @JsonSubTypes.Type(value = MysqlOptions.class, name = "com.mysql.cj.jdbc.Driver"),
        @JsonSubTypes.Type(value = OracleOptions.class, name = "oracle.jdbc.driver.OracleDriver"),
        @JsonSubTypes.Type(value = PostgresqlOptions.class, name = "org.postgresql.Driver"),

})
public abstract class BaseOptions implements DataSourceAdepter {

    /**
     * 测试连接
     *
     * @return 是否成功
     */
    public abstract boolean testConnection();

    /**
     * 获取 jdbcUrl
     *
     * @return sql
     */
    public abstract String jdbcUrl();

    /**
     * 验证有效性sql
     *
     * @return sql
     */
    public abstract String validationQuerySql();

    public interface CreateGroup {

    }

    @JsonIgnore
    protected ReportJdbcTemplate template;
    /**
     * host 只是ip或域名 并非jdbcURL
     */
    @NotBlank(groups = CreateGroup.class, message = "数据源Host不能为空")
    public String host;
    /**
     * 用户名
     */
    @NotBlank(groups = CreateGroup.class, message = "数据源用户名不能为空")
    public String userName;
    /**
     * 密码
     */
    @NotBlank(groups = CreateGroup.class, message = "数据源密码不能为空")
    public String password;
    /**
     * 数据库驱动
     */
    @NotBlank(groups = CreateGroup.class, message = "数据驱动不能为空")
    public String driver;
    /**
     * 数据库名
     */
    @NotBlank(groups = CreateGroup.class, message = "数据库不能为空")
    public String database;
    /**
     * 数据库端口
     */
    @NotNull(groups = CreateGroup.class, message = "数据库端口不能为空")
    public Integer port;
    /**
     * 逻辑删除字段
     */
    public String logicDeleteField;
    /**
     * 逻辑删除字段的值
     */
    public String logicDeleteValue;


    public void refreshOpt(Datasource datasource) {
        if (datasource != null && datasource.getOptions() != null) {
            BeanUtils.copyProperties(this, datasource.getOptions());
        }
    }

    @JsonIgnore
    protected Function3<String, DbType, ResultSet, List<DbFieldDTO>> buildFieldsFunction = (tableName, type, rs) -> {
        try {
            final Map<String, String> columnSchema;
            switch (type) {
                case mysql:
                    columnSchema = SchemaUtils.getMysqlColumnSchema(database, tableName, template);
                    break;
                case oracle:
                    columnSchema = SchemaUtils.getOracleColumnSchema(database, tableName, template);
                    break;

                default:
                    throw new BusinessException("0", "不支持的数据库类型");
            }
            ResultSetMetaData metadata = rs.getMetaData();
            List<DbFieldDTO> fields = new ArrayList<>(metadata.getColumnCount());
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                String columnName = metadata.getColumnLabel(i);
                String columnTypeName = metadata.getColumnTypeName(i);
                fields.add(new DbFieldDTO(columnName, columnSchema.getOrDefault(columnName, ""),
                        columnTypeName));
            }
            return fields;
        } catch (SQLException e) {
            throw new BusinessException("0", e.getMessage());
        }
    };
}
