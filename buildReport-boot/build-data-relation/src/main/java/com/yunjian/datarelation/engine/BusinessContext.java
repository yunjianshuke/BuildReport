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
package com.yunjian.datarelation.engine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.db.Page;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.enums.SqlLike;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunjian.common.exception.BusinessException;
import com.yunjian.datarelation.cache.DictCache;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.common.PreviewData;
import com.yunjian.datarelation.dto.BusinessDatasetDTO;
import com.yunjian.datarelation.dto.ReportInfoDTO;
import com.yunjian.datarelation.engine.pojo.DataFilter;
import com.yunjian.datarelation.engine.pojo.JoinField;
import com.yunjian.datarelation.engine.pojo.SqlField;
import com.yunjian.datarelation.engine.pojo.TableAndField;
import com.yunjian.datarelation.entity.ReportBusinessNode;
import com.yunjian.datarelation.entity.ReportBusinessNodeRelation;
import com.yunjian.datarelation.enums.CellComparatorEnum;
import com.yunjian.datarelation.enums.DataTypeEnum;
import com.yunjian.datarelation.enums.DynamicDateEnum;
import com.yunjian.datarelation.pool.DatasourcePoolManager;
import com.yunjian.datarelation.pool.Ds;
import com.yunjian.datarelation.utils.InterruptedUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.SelectUtils;

/**
 * @author yujian
 **/
@Data
@Slf4j
public class BusinessContext {

    /**
     * 节点id -> 节点实体
     */
    private final Map<String, ReportBusinessNode> nodeMap;
    /**
     * 节点关系 next节点id -> 实体
     */
    private final Map<String, ReportBusinessNodeRelation> relationMap;
    /**
     * 选择了字段的节点
     */
    private final Map<String, List<SqlField>> selectedNodeMap;
    /**
     * 别名map 节点id -> 表别名
     */
    private final Map<String, String> aliasMap;
    /**
     * 数据源管理
     */
    private final DatasourcePoolManager datasourcePoolManager;
    /**
     * 业务线对应的逻辑树
     */
    private final BusinessTreeNode tree;
    private final ObjectMapper objectMapper;
    /**
     * 子查询 -> 结果
     */
    private final Map<Select, List<Map<String, Object>>> subPrimarySelects;

    /**
     * 起始节点对应的主业务查询
     */
    private Select primarySelect;
    private String primarySelectStr;
    private String primaryCountSelect;
    /**
     * 其他无法join的独立查询
     */
    private Map<Select, List<JoinField>> forkSelectMap;
    private AtomicInteger incr;

    private Page page;
    private String tableName;
    private Long dataSourceId;
    private BusinessDatasetDTO businessDatasetDTO;
    private List<DataFilter> dataFilterList;
    private Map<String, Long> fieldDictMap;
    private DictCache dictCache;

    public BusinessContext(DictCache dictCache, DatasourcePoolManager datasourcePoolManager,
                           List<ReportBusinessNode> nodes,
                           List<ReportBusinessNodeRelation> relationList, Map<String, List<SqlField>> selectedNodeMap,
                           BusinessTreeNode tree, Page page) {
        this.selectedNodeMap = selectedNodeMap;
        this.tree = tree;
        this.dictCache = dictCache;
        this.datasourcePoolManager = datasourcePoolManager;
        this.nodeMap = nodes.stream()
                .collect(Collectors.toMap(ReportBusinessNode::getCellId, Function.identity()));
        this.relationMap = relationList.stream()
                .collect(
                        Collectors.toMap(ReportBusinessNodeRelation::getTargetCellNodeId, Function.identity()));
        this.forkSelectMap = new LinkedHashMap<>();
        this.incr = new AtomicInteger();
        this.aliasMap = new HashMap<>();
        this.page = page;
        this.objectMapper = SpringUtil.getBean(ObjectMapper.class);
        this.subPrimarySelects = new LinkedHashMap<>();
        this.fieldDictMap = new HashMap<>();

    }

    public BusinessContext(DatasourcePoolManager datasourcePoolManager,
                           Long dataSourceId,
                           BusinessDatasetDTO selectedField,
                           List<DataFilter> dataFilters,
                           Page page) {
        this.datasourcePoolManager = datasourcePoolManager;
        this.page = page;
        this.dataSourceId = dataSourceId;
        this.dataFilterList = dataFilters;
        this.businessDatasetDTO = selectedField;
        this.nodeMap = new HashMap<>();
        this.relationMap = new HashMap<>();
        this.selectedNodeMap = new HashMap<>();
        this.aliasMap = new HashMap<>();
        this.tree = new BusinessTreeNode();
        this.objectMapper = SpringUtil.getBean(ObjectMapper.class);
        this.subPrimarySelects = new LinkedHashMap<>();
        this.forkSelectMap = new LinkedHashMap<>();
        this.incr = new AtomicInteger();
    }

    /**
     * 遍历树,处理sql的join和不可join sql的存储
     *
     * @param node 起始节点
     */
    public void buildAllSql(ReportBusinessNode node) {
        if (node == null) {
            throw new BusinessException("1", "关系数据结构错误，无法获取node");
        }
        Table table = new Table(node.getTableName()).withAlias(
                new Alias(generateAlias(node.getCellId())));
        primarySelect = createSelect(table);
        if (tree.isSelected()) {
            addSelectItem(primarySelect, table, node, tree.getSqlField());
        } else {
            throw new BusinessException("1", "未选择任何字段");
        }
        for (BusinessTreeNode nextNode : tree.getNextNodes()) {
            if (!nextNode.isDel()) {
                buildJoin(primarySelect, table, node, nextNode);
            }
        }
        PlainSelect select = (PlainSelect) primarySelect.getSelectBody();
        Limit limit = new Limit();
        limit.setRowCount(new LongValue(page.getPageSize()));
        limit.setOffset(new LongValue(0));
        select.setLimit(limit);
    }


    /**
     * 左关联主表.关联字段 -> 子表.关联字段 = 多个匹配的话是多个值
     *
     * @param joinFieldValues 关联的字段
     * @return 匹配的数据
     */
    public Map<String, Map<String, List<AnyData>>> joinOtherOriginTable(
            Map<String, List<Object>> joinFieldValues) {
        Map<String, Map<String, List<AnyData>>> mergeData = new HashMap<>();

        forkSelectMap.forEach((subSelect, forkField) -> {
            PlainSelect ps = (PlainSelect) subSelect.getSelectBody();
            List<List<Expression>> ins = new ArrayList<>(forkField.size());
            for (JoinField joinField : forkField) {
                String mergeKey = underLine(joinField.getSourceTableName(),
                        joinField.getSourceColumnName());
                // 获取主表中关系字段集合
                List<Object> inData = joinFieldValues.get(mergeKey);
                if (CollUtil.isNotEmpty(inData)) {
                    // 过滤 + 去重 减少无用的in
                    List<Expression> expressions = inData.stream()
                            .filter(Objects::nonNull)
                            .filter(x -> !(x instanceof JSONNull))
                            .distinct()
                            .map(x -> new StringValue(String.valueOf(x)))
                            .collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(expressions)) {
                        ins.add(expressions);
                    }
                }

            }


            Expression where = ps.getWhere();

            List<Map<String, Object>> subSqlResult = new ArrayList<>();
            List<InExpression> inExpressionList = new ArrayList<>();
            if (CollUtil.isNotEmpty(ins)) {
                for (int index = 0; index < ins.size(); index++) {
                    InterruptedUtils.checkInterrupted("查询业务线子节点时线程被中断");
                    List<Expression> expressions = ins.get(index);
                    ExpressionList expressionList = new ExpressionList(expressions);
                    InExpression inExpression = new InExpression(
                            new Column(new Table(forkField.get(index).getTargetTableName()),
                                    forkField.get(index).getTargetColumnName()),
                            expressionList);
                    inExpressionList.add(inExpression);
                }

                if (where != null) {
                    try {
                        for (InExpression inExpression : inExpressionList) {
                            Expression expression = CCJSqlParserUtil.parseExpression(ps.getWhere().toString());
                            ps.setWhere(new AndExpression(expression, inExpression));
                        }

                    } catch (JSQLParserException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    for (InExpression inExpression : inExpressionList) {
                        if (ps.getWhere() == null) {
                            ps.setWhere(inExpression);
                        } else {
                            Expression expression = null;
                            try {
                                expression = CCJSqlParserUtil.parseExpression(ps.getWhere().toString());
                                ps.setWhere(new AndExpression(expression, inExpression));
                            } catch (JSQLParserException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                }
                Ds subDs = datasourcePoolManager.getDatasource(
                        CollUtil.getFirst(forkField).getDatasourceId()).orElseThrow(
                        () -> new BusinessException("0",
                                "无法进行计算,子数据源Id=" + toNode(tree.getCellId()).getDatasourceId()
                                        + "不存在"));
                subSqlResult.addAll(subDs.template.queryForList(ps.toString(),
                                Collections.emptyMap())
                        .stream().peek(data -> {
                            // 将所有需要的给字表in的字段分组放好
                            for (Entry<String, List<Object>> entry : joinFieldValues.entrySet()) {
                                entry.getValue().add(data.get(entry.getKey()));
                            }
                        }).collect(Collectors.toList()));
            }

            if (subPrimarySelects.containsKey(subSelect)) {
                subPrimarySelects.put(subSelect, subSqlResult);
            }

            if (!subSqlResult.isEmpty()) {

                for (JoinField joinField : forkField) {
                    String mergeKey = underLine(joinField.getSourceTableName(),
                            joinField.getSourceColumnName());
                    Map<String, List<AnyData>> data = subSqlResult
                            .stream().map(AnyData::new)
                            .collect(
                                    Collectors.groupingBy(x -> String.valueOf(x.get(
                                            underLine(joinField.getTargetTableName(), joinField.getTargetColumnName())
                                    ))));
                    Map<String, List<AnyData>> dataMap = mergeData.get(mergeKey);
                    if (dataMap != null) {
                        data.forEach((k, v) -> {
                            for (AnyData anyData : dataMap.getOrDefault(k, Collections.emptyList())) {
                                for (AnyData newData : v) {
                                    anyData.putAll(newData);
                                }
                            }
                        });
                    } else {
                        mergeData.put(mergeKey, data);
                    }

                }

            }
        });
        return mergeData;
    }

    /**
     * 开始执行
     *
     * @return 结果
     */
    public PreviewData start() {
        // 查询主业务
        ReportBusinessNode node = toNode(tree.getCellId());
        // 将逻辑树转为sql形式
        buildAllSql(node);
        // 分别处理主表和其他源的sql,获取返回值后，根据关系组装在一起
        Map<String, List<Object>> joinFieldValues = new HashMap<>(forkSelectMap.size());
        // 其他非同源的查询
        for (Entry<Select, List<JoinField>> entry : forkSelectMap.entrySet()) {
            for (JoinField joinField : entry.getValue()) {
                String field =
                        underLine(joinField.getSourceTableName(), joinField.getSourceColumnName());
                joinFieldValues.put(field, new ArrayList<>());
            }
        }

        // 其实有多个主表的概念，默认第一个节点延伸的是主表
        String sql = primarySelect.toString();
        Ds ds = datasourcePoolManager.getDatasource(node.getDatasourceId()).orElseThrow(
                () -> new BusinessException("0",
                        "无法进行计算,数据源Id=" + node.getDatasourceId() + "不存在"));

        List<AnyData> primaryData = ds.template.queryForList(sql, Collections.emptyMap()).stream()
                .map(AnyData::new)
                .peek(data -> {
                    // 将所有需要的给字表in的字段分组放好
                    for (Entry<String, List<Object>> entry : joinFieldValues.entrySet()) {
                        entry.getValue().add(data.get(entry.getKey()));
                    }
                })
                .collect(Collectors.toList());

        InterruptedUtils.checkInterrupted("查询业务线计算主节点时线程被中断");

        // 对所有子表in 由于可能是一对多的情况，所以拿到的结果只取匹配的第一项
        Map<String, Map<String, List<AnyData>>> mergeData = joinOtherOriginTable(joinFieldValues);

        // 处理别名
        Map<String, TableAndField> map = new HashMap<>(selectedNodeMap.size());
        for (Entry<String, List<SqlField>> entry : selectedNodeMap.entrySet()) {
            String alias = aliasMap.get(entry.getKey());
            ReportBusinessNode reportBusinessNode = nodeMap.get(entry.getKey());
            if (reportBusinessNode != null) {
                for (SqlField sqlField : entry.getValue()) {
                    map.put(underLine(alias, sqlField.getName()),
                            new TableAndField(reportBusinessNode.getAliasName(), sqlField.getName()));
                }
            }
        }
        // 处理子链的数据合并和膨胀处理
        subPrimarySelects.forEach((subPrimarySelect, primaryResults) -> {
            List<AnyData> subMore = new ArrayList<>();
            for (Map<String, Object> primaryDatum : primaryResults) {
                InterruptedUtils.checkInterrupted();
                addFieldToJson(subMore, new AnyData(primaryDatum), mergeData::get);
            }
            List<JoinField> joinField = forkSelectMap.get(subPrimarySelect);
            for (JoinField field : joinField) {
                final String mergeKey = underLine(field.getSourceTableName(),
                        field.getSourceColumnName());
                Map<String, List<AnyData>> data = subMore
                        .stream()
                        .collect(
                                Collectors.groupingBy(x -> String.valueOf(x.get(
                                        underLine(field.getTargetTableName(), field.getTargetColumnName())
                                ))));
                mergeData.put(mergeKey, data);
            }


        });
        boolean export = false;
        if (page instanceof ReportInfoDTO) {
            ReportInfoDTO reportInfoDTO = (ReportInfoDTO) page;
            export = reportInfoDTO.isExport();
        }
        // 处理主链
        List<AnyData> more = new ArrayList<>();

        for (AnyData primaryDatum : primaryData) {
            // 给primaryDatum添加子表数据
            InterruptedUtils.checkInterrupted();
            addFieldToJson(more, primaryDatum, mergeData::get);
        }
        // 处理响应格式
        List<AnyData> previewData = more.stream()
                .map(data -> {
                    AnyData anyData = new AnyData();
                    for (String fieldKey : data.keySet()) {
                        TableAndField tableAndField = map.get(fieldKey);
                        if (tableAndField != null) {
                            String aliasName = underLine(tableAndField.getAliasTaleName(), tableAndField.getFieldName());
                            // 处理字典映射
                            Long dictId = fieldDictMap.get(fieldKey);
                            if (dictId != null) {
                                Object mapping = dictCache.mapping(dictId, data.getObj(fieldKey));
                                anyData.putOnce(aliasName, mapping);
                            } else {
                                anyData.putOnce(aliasName, data.getObj(fieldKey));
                            }
                        }
                    }
                    return anyData;
                }).collect(Collectors.toList());
        return new PreviewData(previewData, null);

    }

    /**
     * 子数据向前整合的逻辑,遇到一对多会膨胀多条后再合并
     *
     * @param more         最新的结果集合
     * @param primaryDatum 主表数据
     * @param supplier     根据数据字段 + 值获取对应的子数据集
     */
    public void addFieldToJson(List<AnyData> more, AnyData primaryDatum,
                               Function<String, Map<String, List<AnyData>>> supplier) {
        // 这里需要copy一份,因为外部在做循环,无法边新增边

        Map<String, List<AnyData>> expansionMap = new HashMap<>();
        new AnyData(primaryDatum).forEach((primaryField, primaryValue) -> {
            Map<String, List<AnyData>> targetFieldsMap = supplier.apply(primaryField);
            if (targetFieldsMap != null && primaryValue != null && !(primaryValue instanceof JSONNull)) {
                List<AnyData> targetFields = targetFieldsMap.getOrDefault(String.valueOf(primaryValue),
                        Collections.emptyList());

                if (!targetFields.isEmpty()) {
                    int expansionCount = 1;

                    int max = targetFields.size();
                    if (max > 1) {
                        expansionCount = max;
                    }

                    if (expansionCount == 1) {
                        for (AnyData targetField : targetFields) {
                            targetField.forEach((k, v) -> {
                                if (!primaryDatum.containsKey(k)) {
                                    primaryDatum.putOnce(k, v);
                                }
                            });
                        }
                    } else {
                        List<AnyData> anyData = expansionMap.get(String.valueOf(primaryValue));
                        if (anyData != null) {
                            anyData.addAll(targetFields);
                        } else {
                            expansionMap.put(String.valueOf(primaryValue), targetFields);
                        }

                    }
                }
            }
        });

        if (CollUtil.isNotEmpty(expansionMap)) {
            List<AnyData> datas = new ArrayList<>();

            for (Entry<String, List<AnyData>> entry : expansionMap.entrySet()) {
                List<AnyData> v = entry.getValue();
                if (CollUtil.isNotEmpty(datas)) {
                    List<AnyData> temp = new ArrayList<>();
                    for (AnyData data : datas) {
                        for (AnyData anyData : v) {
                            AnyData copy = new AnyData(data);
                            anyData.forEach((k0, v0) -> {
                                if (!copy.containsKey(k0)) {
                                    copy.putOnce(k0, v0);
                                }
                            });
                            temp.add(copy);
                        }
                    }
                    datas = temp;
                } else {
                    for (AnyData data : v) {
                        AnyData primary = new AnyData(primaryDatum);
                        data.forEach((k0, v0) -> {
                            if (!primary.containsKey(k0)) {
                                primary.putOnce(k0, v0);
                            }
                        });
                        datas.add(primary);
                    }
                }

            }
            more.addAll(datas);


        } else {
            more.add(new AnyData(primaryDatum));
        }

    }


    public void addWhereItem(Select select, Table table, List<DataFilter> DataFilters) {
        if (CollUtil.isNotEmpty(DataFilters)) {
            for (DataFilter field : DataFilters) {
                addSelectItem(select, table, new Field(field.getName()));
                Object defaultValue = field.getDefaultValue();
                if (ObjectUtil.isEmpty(defaultValue)) {
                    continue;
                }
                addWhere(select, table, field);
            }
        }
    }

    public List<AnyData> startBySingleTable() {
        Table table = new Table(businessDatasetDTO.getTableName()).withAlias(
                new Alias(generateAlias(businessDatasetDTO.getCellId())));
        Select select = createSelect(table);
        addSelectItem(select, table, null, businessDatasetDTO.getSqlFieldList());
        addWhereItem(select, table, dataFilterList);

        String sql = select.toString();
        Ds ds = datasourcePoolManager.getDatasource(dataSourceId).orElseThrow(
                () -> new BusinessException("0",
                        "无法进行计算,数据源Id=" + dataSourceId + "不存在"));
        List<AnyData> primaryData = ds.template.queryForList(sql, Collections.emptyMap()).stream()
                .map(AnyData::new)
                .collect(Collectors.toList());

        Map<String, String> map = new HashMap<>();
        SelectBody selectBody = select.getSelectBody();
        PlainSelect plainSelect = (PlainSelect) selectBody;
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        selectItems.forEach(selectItem -> {
            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
            map.put(selectExpressionItem.getAlias().getName(),
                    selectExpressionItem.getExpression(Column.class).getColumnName());
        });

        return primaryData.stream()
                .map(data -> {
                    AnyData anyData = new AnyData();
                    for (String fieldKey : data.keySet()) {
                        anyData.putOnce(map.get(fieldKey), data.getObj(fieldKey));
                    }
                    return anyData;
                }).collect(Collectors.toList());
    }

    /**
     * 拼接下划线
     *
     * @param a left str
     * @param b right str
     * @return str
     */
    public String underLine(String a, String b) {
        return a + StringPool.UNDERSCORE + b;
    }

    /**
     * 创建select
     *
     * @param table 表
     * @return select
     */
    public Select createSelect(Table table) {
        PlainSelect body = new PlainSelect().addSelectItems(Collections.emptyList())
                .withFromItem(table);
        return new Select().withSelectBody(body);
    }

    /**
     * 循环添加查询项
     */
    public void addSelectItem(Select select, Table table, ReportBusinessNode node, List<SqlField> sqlField) {
        Map<String, DataFilter> businessDefaultValueMap = new HashMap<>();
        if (ObjectUtil.isNotNull(node)) {
            String componentJson = node.getComponentJson();
            JSONArray fields = JSONUtil.parseArray(componentJson);
            businessDefaultValueMap = fields.stream()
                    .map(field -> (JSONObject) field)
                    .filter(field -> field.containsKey("defaultValue") && !StringPool.EMPTY.equals(field.get("defaultValue")))
                    .collect(Collectors.toMap(field -> field.getStr("name"), field -> {
                        DataFilter dataFilter = new DataFilter();
                        dataFilter.setName(field.getStr("name"));
                        dataFilter.setType(field.getStr("type"));
                        dataFilter.setCondition(CellComparatorEnum.EQUAL.getCode());
                        dataFilter.setDefaultValue(field.get("defaultValue"));
                        return dataFilter;
                    }));
        }

        if (CollUtil.isNotEmpty(sqlField)) {
            for (SqlField field : sqlField) {
                addSelectItem(select, table, new Field(field.getName(), field.getDictConfId()));
                DataFilter dataFilter = null;
                boolean sqlFieldDataFilterExist = field.getDataFilter() != null && ObjectUtil.isNotEmpty(
                        field.getDataFilter().getDefaultValue());
                // 优先使用数据集配置的默认条件
                if (sqlFieldDataFilterExist) {
                    dataFilter = field.getDataFilter();
                } else if (businessDefaultValueMap.get(field.getName()) != null) {
                    dataFilter = businessDefaultValueMap.get(field.getName());
                }
                // 每次使用后删除
                businessDefaultValueMap.remove(field.getName());

                if (dataFilter != null && !"".equals(dataFilter.getDefaultValue())) {
                    addWhere(select, table, dataFilter);
                }
            }
            // 如果还有剩余的，则添加
            businessDefaultValueMap.forEach((fieldName, dataFilter) -> addWhere(select, table, dataFilter));
        } else {
            businessDefaultValueMap.forEach((fieldName, dataFilter) -> addWhere(select, table, dataFilter));
        }

    }

    /**
     * 添加默认值筛选条件
     *
     * @param select     查询
     * @param table      表
     * @param dataFilter 过滤条件
     */
    public void addWhere(Select select, Table table, DataFilter dataFilter) {
        SelectBody selectBody = select.getSelectBody();
        PlainSelect plainSelect = (PlainSelect) selectBody;
        String field = dataFilter.getName();
        Object value = dataFilter.getDefaultValue();
        CellComparatorEnum cellComparatorEnum = CellComparatorEnum.of(dataFilter.getCondition());
        DataTypeEnum dataTypeEnum = DataTypeEnum.of(dataFilter.getType());
        boolean nullBetween =
                value instanceof JSONArray && ((JSONArray) value).stream().anyMatch(""::equals);

        if (!nullBetween && value != null && !"".equals(value)) {
            if (plainSelect.getWhere() == null) {
                plainSelect.setWhere(getExpression(table, field, value, cellComparatorEnum, dataTypeEnum));
            } else {
                plainSelect.setWhere(new AndExpression(plainSelect.getWhere(),
                        getExpression(table, field, value, cellComparatorEnum, dataTypeEnum)));
            }
        }

    }

    private Expression getExpression(Table table, String field, Object value,
                                     CellComparatorEnum cellComparatorEnum, DataTypeEnum type) {
        Column column = new Column(table, field);
        Expression typeValue = type.getTypeValue(value);

        switch (cellComparatorEnum) {
            case DYNAMIC_DATE:
                String cost = (String) value;
                DynamicDateEnum dynamicDateEnum = DynamicDateEnum.of(cost);
                Between bw = new Between();
                bw.setLeftExpression(column);
                LocalDateTime now = LocalDateTime.now();
                bw.setBetweenExpressionEnd(type.getTypeValue((now)));
                switch (dynamicDateEnum) {
                    case WEEK:
                        bw.setBetweenExpressionStart(type.getTypeValue((now.minusWeeks(1))));
                        break;
                    case MONTH:
                        bw.setBetweenExpressionStart(type.getTypeValue((now.minusMonths(1))));
                        break;
                    case QUARTER:
                        bw.setBetweenExpressionStart(type.getTypeValue((now.minusMonths(3))));
                        break;
                    case YEAR:
                        bw.setBetweenExpressionStart(type.getTypeValue((now.minusYears(1))));
                        break;
                    default:
                        throw new BusinessException("1", "未知类型");
                }
                return bw;
            case EQUAL:
                return new EqualsTo(column, typeValue);
            case NOT_EQUAL:
                return new NotEqualsTo(column, typeValue);
            case GREATER_THAN:
                GreaterThan gt = new GreaterThan();
                gt.setLeftExpression(column);
                gt.setRightExpression(typeValue);
                return gt;
            case LESS_THAN:
                MinorThan mt = new MinorThan();
                mt.setLeftExpression(column);
                mt.setRightExpression(typeValue);
                return mt;
            case GREATER_THAN_OR_EQUAL:
                GreaterThanEquals gte = new GreaterThanEquals();
                gte.setLeftExpression(column);
                gte.setRightExpression(typeValue);
                return gte;
            case LESS_THAN_OR_EQUAL:
                MinorThanEquals mte = new MinorThanEquals();
                mte.setLeftExpression(column);
                mte.setRightExpression(typeValue);
                return mte;
            case LIKE:
                LikeExpression likeExpression = new LikeExpression();
                likeExpression.setLeftExpression(column);
                likeExpression.setRightExpression(
                        type.getTypeValue(SqlUtils.concatLike(String.valueOf(value), SqlLike.DEFAULT)));
                return likeExpression;
            case NOT_LIKE:
                LikeExpression notLikeExpression = new LikeExpression();
                notLikeExpression.setLeftExpression(column);
                notLikeExpression.setRightExpression(
                        type.getTypeValue(SqlUtils.concatLike(String.valueOf(value), SqlLike.DEFAULT)));
                notLikeExpression.setNot(true);
                return notLikeExpression;
            case BETWEEN:
                List<Object> values = (List<Object>) value;
                Between between = new Between();
                between.setLeftExpression(column);
                between.setBetweenExpressionStart(type.getTypeValue(values.get(0)));
                between.setBetweenExpressionEnd(type.getTypeValue(values.get(1)));
                return between;
            case YEAR:
                EqualsTo equalsTo = new EqualsTo();
                //YEAR(column)
                net.sf.jsqlparser.expression.Function yearFunction = new net.sf.jsqlparser.expression.Function();
                yearFunction.setName("YEAR");
                yearFunction.setParameters(new ExpressionList(column));
                equalsTo.setLeftExpression(yearFunction);
                equalsTo.setRightExpression(new StringValue(String.valueOf(value)));
                return equalsTo;
            case MONTH:
                net.sf.jsqlparser.expression.Function dateFormatFunction = new net.sf.jsqlparser.expression.Function();
                dateFormatFunction.setName("DATE_FORMAT");
                ExpressionList parameters = new ExpressionList();
                parameters.addExpressions(column, new StringValue("%Y-%m"));
                dateFormatFunction.setParameters(parameters);
                EqualsTo dayEqualsTo = new EqualsTo();
                dayEqualsTo.setLeftExpression(dateFormatFunction);
                dayEqualsTo.setRightExpression(new StringValue(String.valueOf(value)));
                return dayEqualsTo;
            case DAY:
                dateFormatFunction = new net.sf.jsqlparser.expression.Function();
                dateFormatFunction.setName("DATE_FORMAT");
                parameters = new ExpressionList();
                parameters.addExpressions(column, new StringValue("%Y-%m-%d"));
                dateFormatFunction.setParameters(parameters);
                dayEqualsTo = new EqualsTo();
                dayEqualsTo.setLeftExpression(dateFormatFunction);
                dayEqualsTo.setRightExpression(new StringValue(String.valueOf(value)));
                return dayEqualsTo;
            case IN:
                values = (List<Object>) value;
                List<Expression> ins = values.stream()
                        .filter(Objects::nonNull)
                        .filter(x -> !(x instanceof JSONNull))
                        .distinct()
                        .map(x -> new StringValue(String.valueOf(x)))
                        .collect(Collectors.toList());
                ExpressionList el = new ExpressionList(ins);
                return new InExpression(column, el);
            case NOT_IN:
                if (value instanceof Collection) {
                    Collection<Object> values1 = (Collection) value;
                    List<Expression> notIns = values1.stream()
                            .filter(Objects::nonNull)
                            .filter(x -> !(x instanceof JSONNull))
                            .distinct()
                            .map(x -> new StringValue(String.valueOf(x)))
                            .collect(Collectors.toList());
                    InExpression inExpression = new InExpression(column, new ExpressionList(notIns));
                    inExpression.setNot(true);
                    return inExpression;
                }
            default:
                throw new BusinessException("0", "不支持的类型" + cellComparatorEnum);

        }
    }

    /**
     * 添加查询项
     */
    public void addSelectItem(Select select, Table table, Field field) {
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            SelectExpressionItem sei = (SelectExpressionItem) selectItem;
            Column expression = (Column) sei.getExpression();
            Table existTable = expression.getTable();
            String existColumnName = expression.getColumnName();
            if (existTable != null && existColumnName != null &&
                    existTable.getAlias().getName().equals(table.getAlias().getName())
                    && existColumnName.equals(field.name)) {
                return;
            }
        }
        final String alias = underLine(table.getAlias().getName(), field.name);
        SelectExpressionItem selectExpressionItem = new SelectExpressionItem();
        selectExpressionItem.setExpression(new Column(table, field.name));
        selectExpressionItem.setAlias(new Alias(alias));
        if (field.hasDict()) {
            fieldDictMap.computeIfAbsent(alias, k -> field.dictId);
        }
        plainSelect.addSelectItems(selectExpressionItem);


    }

    /**
     * 表别名生成
     */
    public String generateAlias(String cellId) {
        String alias = "t" + incr.incrementAndGet();
        aliasMap.put(cellId, alias);
        return alias;
    }

    public List<JoinField> jsonArrayToJsonField(String json) {
        List<JoinField> fields;
        try {
            fields = objectMapper.readValue(json,
                    new TypeReference<List<JoinField>>() {
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return fields;
    }

    /**
     * 添加join
     *
     * @param select    原始查询
     * @param leftTable 左表
     * @param prevNode  上节点
     * @param nextNode  下节点
     */
    public void buildJoin(Select select, Table leftTable, ReportBusinessNode prevNode,
                          BusinessTreeNode nextNode) {
        ReportBusinessNode node = toNode(nextNode.getCellId());
        Select currentSelect = select;
        Table rightTable;
        if (Objects.equals(node.getDatasourceId(), prevNode.getDatasourceId())) {
            // 可以join
            rightTable = new Table(node.getTableName()).withAlias(
                    new Alias(generateAlias(node.getCellId())));
            Join join = new Join();
            // 添加right table
            join.withRightItem(rightTable);
            join.setLeft(true);
            // 添加right table.field
            if (nextNode.isSelected()) {
                addSelectItem(select, rightTable, node, nextNode.getSqlField());
            }
            // 添加 left join rightTable on leftTable.field = rightTable.field
            ReportBusinessNodeRelation nodeRelation = relationMap.get(nextNode.getCellId());
            if (nodeRelation.getSourceCellNodeId().equals(prevNode.getCellId())) {
                List<JoinField> fields = this.jsonArrayToJsonField(nodeRelation.getComponentJson());
                List<EqualsTo> eqs = fields.stream()
                        .map(field -> new EqualsTo(new Column(leftTable, field.getSourceColumnName()),
                                new Column(rightTable, field.getTargetColumnName())))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(eqs)) {
                    EqualsTo equalsTo = eqs.get(0);
                    Join joinSetting = SelectUtils.addJoin(select, rightTable, equalsTo);
                    joinSetting.setLeft(true);
                    for (int j = 1; j < eqs.size(); j++) {
                        EqualsTo eq = eqs.get(j);
                        Expression first = CollUtil.getFirst(joinSetting.getOnExpressions());
                        AndExpression andExpression = new AndExpression(first, eq);
                        joinSetting.setOnExpressions(CollUtil.newArrayList(andExpression));
                    }
                }
            }

        } else {
            // 不可以join
            rightTable = new Table(node.getTableName()).withAlias(
                    new Alias(generateAlias(node.getCellId())));
            currentSelect = createSelect(rightTable);
            ReportBusinessNodeRelation nodeRelation = relationMap.get(nextNode.getCellId());
            if (nodeRelation.getSourceCellNodeId().equals(prevNode.getCellId())) {
                List<JoinField> fields = jsonArrayToJsonField(nodeRelation.getComponentJson());
                for (JoinField field : fields) {
                    // 当前数据源id
                    field.setDatasourceId(node.getDatasourceId());
                    // 左表别名
                    field.setSourceTableName(leftTable.getAlias().getName());
                    // 当前表别名
                    field.setTargetTableName(rightTable.getAlias().getName());
                    // 当前节点所选择的字段
                    field.setSqlField(nextNode.getSqlField());

                    if (nextNode.isSelected()) {
                        addSelectItem(currentSelect, rightTable, node, nextNode.getSqlField());
                    }
                    if (select != primarySelect) {
                        //从当前节点之后如果有任何一个不可join的则需要 分段整合数据
                        subPrimarySelects.put(select, Collections.emptyList());
                    }
                    addSelectItem(currentSelect, rightTable, new Field(field.getTargetColumnName()));
                    // 该字段必须被带出来 因为后面要拿这个字段集合到fork select去in
                    addSelectItem(select, leftTable, new Field(field.getSourceColumnName()));
                }
                // 存储独立查询
                forkSelectMap.putIfAbsent(currentSelect, fields);
            }
        }
        // 如果不是叶子节点 则继续递归
        if (!nextNode.isLeaf()) {
            for (BusinessTreeNode nextNodeNextNode : nextNode.getNextNodes()) {
                if (!nextNodeNextNode.isDel()) {
                    buildJoin(currentSelect, rightTable, node, nextNodeNextNode);
                }
            }
        }
    }

    /**
     * cell id 转node
     *
     * @param cellId cell id
     * @return node
     */
    public ReportBusinessNode toNode(String cellId) {
        return nodeMap.get(cellId);
    }

    @AllArgsConstructor
    @Data
    public static class Field {
        private String name;
        private Long dictId;

        public Field(String field) {
            this.name = field;
        }

        public boolean hasDict() {
            return dictId != null;
        }

    }


}
