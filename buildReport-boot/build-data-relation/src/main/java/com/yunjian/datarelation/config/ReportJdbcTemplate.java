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
package com.yunjian.datarelation.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.google.common.collect.Lists;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.common.BaseOptions;
import com.yunjian.datarelation.pool.DatasourcePoolManager;
import com.yunjian.datarelation.pool.Ds;
import com.yunjian.datarelation.pool.LogicDelete;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 修改sql的最后一道防线,再往后就都是final sql,只能暴力解决，在这里可以友好的解决
 *
 * @author yujian
 **/
@Slf4j
@SuppressWarnings("all")
public class ReportJdbcTemplate extends NamedParameterJdbcTemplate {

    /**
     * 如果sql中包含这些关键字则不进行逻辑删除填充操作,大小写敏感
     */
    private List<String> IGNORE_KEYWORD;

    private DruidDataSource dataSource;

    public ReportJdbcTemplate(DruidDataSource dataSource) {
        super(dataSource);
        this.dataSource = dataSource;
        this.IGNORE_KEYWORD = Lists.newArrayList("show tables", "information_schema");
    }


    DatasourcePoolManager datasourcePoolManager;

    @Override
    public <T> T execute(String sql, SqlParameterSource paramSource,
                         PreparedStatementCallback<T> action) throws DataAccessException {
        return super.execute(buildLogicDeleteSql(sql), paramSource, action);
    }

    @Override
    public <T> T execute(String sql, Map<String, ?> paramMap, PreparedStatementCallback<T> action)
            throws DataAccessException {
        return super.execute(buildLogicDeleteSql(sql), paramMap, action);
    }

    @Override
    public <T> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException {

        return super.execute(buildLogicDeleteSql(sql), action);

    }

    @Override
    public <T> T query(String sql, SqlParameterSource paramSource, ResultSetExtractor<T> rse)
            throws DataAccessException {
        return super.query(buildLogicDeleteSql(sql), paramSource, rse);
    }

    @Override
    public <T> T query(String sql, Map<String, ?> paramMap, ResultSetExtractor<T> rse)
            throws DataAccessException {
        return super.query(buildLogicDeleteSql(sql), paramMap, rse);
    }

    @Override
    public <T> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException {
        return super.query(buildLogicDeleteSql(sql), rse);
    }

    @Override
    public void query(String sql, SqlParameterSource paramSource, RowCallbackHandler rch)
            throws DataAccessException {
        super.query(buildLogicDeleteSql(sql), paramSource, rch);
    }

    @Override
    public void query(String sql, Map<String, ?> paramMap, RowCallbackHandler rch)
            throws DataAccessException {
        super.query(buildLogicDeleteSql(sql), paramMap, rch);
    }

    @Override
    public void query(String sql, RowCallbackHandler rch) throws DataAccessException {
        super.query(buildLogicDeleteSql(sql), rch);
    }

    @Override
    public <
            T> List<T> query(String sql, SqlParameterSource paramSource, RowMapper<T> rowMapper)
            throws DataAccessException {
        return super.query(buildLogicDeleteSql(sql), paramSource, rowMapper);
    }

    @Override
    public <T> List<T> query(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper)
            throws DataAccessException {
        return super.query(buildLogicDeleteSql(sql), paramMap, rowMapper);
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
        return super.query(buildLogicDeleteSql(sql), rowMapper);
    }

    private String buildLogicDeleteSql(String sql) {

        LogicDelete logicDelete = DatasourcePoolManager.LOGIC_DELETE_HOLDER.get();

        if (logicDelete == null) {
            return sql;
        }

        for (String keyword : IGNORE_KEYWORD) {
            if (sql.contains(keyword)) {
                return sql;
            }
        }

        if (datasourcePoolManager == null) {
            synchronized (this) {
                if (datasourcePoolManager == null) {
                    datasourcePoolManager = SpringUtil.getBean(DatasourcePoolManager.class);
                }
            }
        }

        Optional<Ds> optionalDs = datasourcePoolManager.getDatasource(
                Long.valueOf(dataSource.getName()));

        if (optionalDs.isPresent()) {
            BaseOptions options = optionalDs.get().getInstance().getOptions();
            if (StrUtil.isAllNotBlank(options.getLogicDeleteField(),
                    options.getLogicDeleteValue())) {
                logicDelete.setLogicFieldName(options.getLogicDeleteField());
                logicDelete.setValue(options.getLogicDeleteValue());
                return doBuildSql(optionalDs.get().getInstance().getId(), logicDelete, sql);
            }
        }
        log.info("sql: {}", sql);
        return sql;
    }


    public EqualsTo buildLogicDeleteSql(String tableNameOrAlias, LogicDelete logicDelete) {

        return new EqualsTo(
                new Column(tableNameOrAlias + StringPool.DOT + logicDelete.getLogicFieldName()),
                new Column(logicDelete.getValue()));
    }

    public String doBuildSql(Long datasourceId, LogicDelete logicDelete, String sql) {
        // 如果操作人手动添加了任何关于逻辑删除的字段则不自动处理
        if (!sql.contains(logicDelete.getLogicFieldName())) {
            try {
                Select select = (Select) CCJSqlParserUtil.parse(sql);
                PlainSelect ps = (PlainSelect) select.getSelectBody();

                // 如果from了多个表或left join多个表 对每个表的del_flag都进行处理
                Map<String, String> aliasMap = new HashMap<>();
                Table table = (Table) ps.getFromItem();
                String tName = table.getAlias() == null ? table.getName() : table.getAlias().getName();
                if (datasourcePoolManager.isLogicDeleteTable(datasourceId, table.getName())) {
                    EqualsTo delSql = buildLogicDeleteSql(tName, logicDelete);
                    if (ps.getWhere() != null) {
                        ps.setWhere(
                                new AndExpression(ps.getWhere(), delSql));
                    } else {
                        ps.setWhere(delSql);
                    }
                }
                if (CollUtil.isNotEmpty(ps.getJoins())) {
                    for (Join join : ps.getJoins()) {
                        Table joinTable = (Table) join.getRightItem();
                        if (datasourcePoolManager.isLogicDeleteTable(datasourceId, joinTable.getName())) {
                            EqualsTo delSql = buildLogicDeleteSql(
                                    joinTable.getAlias() == null ? joinTable.getName() : joinTable.getAlias()
                                            .getName(), logicDelete);
                            Expression first = CollUtil.getFirst(join.getOnExpressions());
                            first = new AndExpression(first, delSql);
                            join.setOnExpressions(CollUtil.newArrayList(first));
                        }
                    }
                }
                String result = ps.toString();
                log.info("填充逻辑删除之后SQL = {}", result);
                return result;
            } catch (JSQLParserException e) {
                if (e.getMessage().contains("FROM")) {
                    throw new BusinessException("1", "未选择任何字段");
                }
                // pass
                log.error("sql填充失败 sql={} ,err={}", sql, e);
                return sql;
            }
        }
        log.info("未填充逻辑删除SQL = {}", sql);
        return sql;
    }
}

