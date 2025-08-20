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
package com.yunjian.datarelation.pool;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.common.BaseOptions;
import com.yunjian.datarelation.common.DataSourceAdepter;
import com.yunjian.datarelation.config.ReportJdbcTemplate;
import com.yunjian.datarelation.config.ReportProperties.DatasourceProperties;
import com.yunjian.datarelation.dto.DbFieldDTO;
import com.yunjian.datarelation.dto.TableDTO;
import com.yunjian.datarelation.entity.Datasource;
import com.yunjian.datarelation.filter.DatasourceSqlFilter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yujian
 **/
@Slf4j
@Getter
public class Ds implements DataSourceAdepter {

    private final Datasource instance;
    private final BaseOptions options;
    private final DruidDataSource dataSource;
    public final ReportJdbcTemplate template;
    private boolean healthStatus;


    @Override
    public List<AnyData> privew(String tableName, Integer limit) {
        try {
            return options.privew(tableName, limit);
        } catch (Exception e) {
            throw new BusinessException("1", "数据源异常,请检查数据源 【" + instance.getName() + "】 配置是否有变更");
        }

    }

    @Override
    public List<TableDTO> allTables(String tableName) {
        try {
            return options.allTables(tableName);
        } catch (Exception e) {
            throw new BusinessException("1", "数据源异常,请检查数据源 【" + instance.getName() + "】 配置是否有变更");
        }

    }

    @Override
    public List<DbFieldDTO> tableFields(String tableName) {
        try {
            return options.tableFields(tableName);
        } catch (Exception e) {
            throw new BusinessException("1", "数据源异常,请检查数据源 【" + instance.getName() + "】 配置是否有变更");
        }

    }

    protected Ds(Datasource instance, DatasourceProperties prop) {
        this.options = instance.getOptions();
        // 用这数据源主要是为了能看个监控 /druid/datasource.html
        this.dataSource = new DruidDataSource();
        this.instance = instance;
        // 基础配置 这里不要改中文名称，因为后面有其他地方会使用到
        dataSource.setName(String.valueOf(instance.getId()));
        dataSource.setUrl(options.jdbcUrl());
        dataSource.setDriverClassName(options.driver);
        dataSource.setUsername(options.userName);
        dataSource.setPassword(options.password);
        // 过滤器
        dataSource.setProxyFilters(
                Collections.singletonList(new DatasourceSqlFilter()));
        // 池化配置
        dataSource.setInitialSize(getOrDefault(prop.getInitialSize(), 5));
        dataSource.setMinIdle(getOrDefault(prop.getMinIdle(), 5));
        dataSource.setMaxActive(getOrDefault(prop.getMaxActive(), 20));
        // 获取连接等待超时的时间
        dataSource.setMaxWait(TimeUnit.SECONDS.toMillis(getOrDefault(prop.getMaxWait(), 3)));
        // 配置间隔多久进行一次检测，检测需要关闭的空闲连接
        dataSource.setTimeBetweenEvictionRunsMillis(
                TimeUnit.SECONDS.toMillis(getOrDefault(prop.getTimeBetweenEvictionRunsMillis(), 60)));
        // 配置一个连接在池中最小生存的时间
        dataSource.setMinEvictableIdleTimeMillis(
                TimeUnit.MINUTES.toMillis(getOrDefault(prop.getMinEvictableIdleTimeMillis(), 5)));
        // 配置ValidationQuery测试连接是否可用
        dataSource.setValidationQuery(options.validationQuerySql());
        dataSource.setValidationQueryTimeout(1000);
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        try {
            log.info("初始化数据库连接池 = {}", instance.getName());
            dataSource.init();
            healthStatus = true;
        } catch (Exception e) {
            log.error("初始化数据库连接池失败 " + JSONUtil.toJsonPrettyStr(options), e);
            healthStatus = false;
        }
        template = new ReportJdbcTemplate(dataSource);
        options.setTemplate(template);
    }

    public Consumer<Boolean> changeStatus = (s) -> this.healthStatus = s;

    private Integer getOrDefault(Integer i, Integer defaultValue) {
        if (ObjectUtil.isNotNull(i) && i > 0) {
            return i;
        }
        return defaultValue;
    }
}
