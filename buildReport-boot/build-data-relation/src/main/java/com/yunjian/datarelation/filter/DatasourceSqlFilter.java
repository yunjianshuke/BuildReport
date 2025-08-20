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
package com.yunjian.datarelation.filter;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.yunjian.datarelation.pool.DatasourcePoolManager;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * 对自定义数据源做一些处理 这里没有使用spi注入,为了只对数据源管理创建的数据源做处理
 *
 * @author yujian
 **/
@Slf4j
public class DatasourceSqlFilter extends StatFilter {

    private DatasourcePoolManager datasourcePoolManager;

    public DatasourceSqlFilter() {
        super();
        // 3秒才算慢sql
        setSlowSqlMillis(3 * 1000);
    }

    @Override
    protected void statementExecuteQueryBefore(StatementProxy statement, String sql) {
        statement.setLastExecuteStartNano();
    }

    @Override
    protected void handleSlowSql(StatementProxy statementProxy) {
        final String dataSourceId = statementProxy.getSqlStat().getName();
        printSlowSql(dataSourceId, statementProxy);
    }

    private void printSlowSql(String dataSourceId, StatementProxy statementProxy) {

        if (log.isWarnEnabled()) {
            long millis = statementProxy.getLastExecuteTimeNano() / (1000 * 1000);
            Optional.ofNullable(datasourcePoolManager)
                    .orElseGet(() -> datasourcePoolManager = SpringUtil.getBean(DatasourcePoolManager.class))
                    .getDatasource(Long.valueOf(dataSourceId)).ifPresent(ds -> {
                        String lastExecSql = statementProxy.getLastExecuteSql();
                        String slowParameters = buildSlowParameters(statementProxy);
                        String msg =
                                " 数据源 【" + ds.getInstance().getName() + "】 慢SQL "
                                        + millis
                                        + " 毫秒. "
                                        + lastExecSql
                                        + " 参数 " + slowParameters;
                        log.warn(msg);
                    });

        }
    }
}
