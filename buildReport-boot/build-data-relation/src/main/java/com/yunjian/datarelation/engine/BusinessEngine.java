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
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunjian.datarelation.cache.DictCache;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.common.PreviewData;
import com.yunjian.datarelation.dto.BusinessDatasetDTO;
import com.yunjian.datarelation.engine.pojo.DataFilter;
import com.yunjian.datarelation.engine.pojo.SqlField;
import com.yunjian.datarelation.entity.ReportBusinessNode;
import com.yunjian.datarelation.entity.ReportBusinessNodeRelation;
import com.yunjian.datarelation.pool.DatasourcePoolManager;
import com.yunjian.datarelation.service.ReportBusinessNodeRelationService;
import com.yunjian.datarelation.service.ReportBusinessNodeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;

/**
 * @author yujian
 **/

@RequiredArgsConstructor
public class BusinessEngine {

    private final ReportBusinessNodeService businessNodeService;
    private final ReportBusinessNodeRelationService reportBusinessNodeRelationService;
    private final DatasourcePoolManager datasourcePoolManager;
    private final DictCache dictCache;


    public void fillSelectedField(List<DataFilter> dataFilters,
                                  Map<String, List<SqlField>> selectedNodeMap) {
        // 先判断查询条件的数据集是否在选择的数据集中
        if (CollUtil.isNotEmpty(dataFilters)) {
            dataFilters.forEach(item -> {
                String cellId = item.getCellId();
                if (!selectedNodeMap.containsKey(cellId)) {
                    // 如果没有选择的数据集中没有这个数据集，那么就需要将这个数据集加入到选择的数据集中
                    SqlField sqlField = new SqlField();
                    BeanUtils.copyProperties(item, sqlField);
                    sqlField.setDataFilter(item);
                    ArrayList<SqlField> arrayList = new ArrayList<>();
                    arrayList.add(sqlField);
                    selectedNodeMap.put(cellId, arrayList);
                } else {
                    // 如果选择的数据集中有这个数据集，那么就需要查看查询字段是否在选择的字段中
                    List<SqlField> sqlFields = selectedNodeMap.get(cellId);
                    for (SqlField sqlField : sqlFields) {
                        if (sqlField.getName().equals(item.getName())) {
                            sqlField.setDataFilter(item);
                        }
                    }
                    if (sqlFields.stream()
                            .noneMatch(sqlField -> sqlField.getName().equals(item.getName()))) {
                        // 如果没有在选择的字段中，那么就需要将这个字段加入到选择的字段中
                        SqlField sqlField = new SqlField();
                        BeanUtils.copyProperties(item, sqlField);
                        sqlField.setDataFilter(item);
                        sqlFields.add(sqlField);
                    }
                }
            });
        }
    }

    public List<AnyData> runSingleTable(Long dataSourceId, BusinessDatasetDTO selectedField,
                                        Page reportInfoDTO, List<DataFilter> dataFilters) {
        BusinessContext businessContext = new BusinessContext(datasourcePoolManager, dataSourceId,
                selectedField, dataFilters, reportInfoDTO);
        return businessContext.startBySingleTable();
    }

// 0 拿到所有的关系
// 1 根据选择的字段 拿到对应结构 根据cellId拿到所有勾选字段的表
// 分页是强制选项，除了打印均需要分页

    public PreviewData run(Long relationId, List<BusinessDatasetDTO> selectedFieldList,
                           List<DataFilter> dataFilters, Page page) {
        // 当前业务线所有节点
        List<ReportBusinessNodeRelation> relationList = reportBusinessNodeRelationService.list(
                Wrappers.<ReportBusinessNodeRelation>lambdaQuery()
                        .eq(ReportBusinessNodeRelation::getRelationId, relationId));
        // 当前已经选择的节点和节点所选的字段
        Map<String, List<SqlField>> selectedNodeMap = selectedFieldList.stream().collect(
                Collectors.toMap(BusinessDatasetDTO::getCellId, BusinessDatasetDTO::getSqlFieldList));
        // 填充过滤条件字段
        fillSelectedField(dataFilters, selectedNodeMap);
        // 获取主要数据
        List<ReportBusinessNode> nodes = businessNodeService.list(
                Wrappers.<ReportBusinessNode>lambdaQuery()
                        .eq(ReportBusinessNode::getRelationId, relationId));

        // current node -> next node
        Map<String, List<ReportBusinessNodeRelation>> targetCellMap = relationList.stream()
                .collect(Collectors.groupingBy(ReportBusinessNodeRelation::getSourceCellNodeId));

        String rootNodeCellId = findRootNodeCellId(relationList);
        if (selectedFieldList.size() == 1) {
            rootNodeCellId = CollUtil.getFirst(selectedFieldList).getCellId();
        }

        if (StrUtil.isBlank(rootNodeCellId)) {
            return new PreviewData(Collections.emptyList(), 0);
        }

        // 组装完整的业务树
        BusinessTreeNode rootNode = new BusinessTreeNode();
        rootNode.setCellId(rootNodeCellId);
        rootNode.setSqlField(selectedNodeMap.get(rootNodeCellId));
        rootNode.setNextNodes(buildNextTree(rootNode, targetCellMap, selectedNodeMap));
        // 枝剪
        lopper(rootNode);

        BusinessContext businessContext = new BusinessContext(dictCache, datasourcePoolManager, nodes,
                relationList, selectedNodeMap, rootNode, page);

        return businessContext.start();

    }

    /**
     * 计算边缘节点在哪，只保留可达的节点
     *
     * @param node root
     */
    public void lopper(BusinessTreeNode node) {
        List<BusinessTreeNode> nextNodes = node.getNextNodes();
        boolean noSelected = !node.isSelected();
        boolean nextIsEmpty = CollUtil.isEmpty(nextNodes);
        boolean changed = false;
        node.setDel(false);
        if (noSelected && nextIsEmpty) {
            changed = true;
            // 如果当前节点未被选择,并且子节点为空，那么当前节点需要被枝剪
            node.setDel(true);
        }

        if (node.isSelected()) {
            changed = true;
            node.setDel(false);
        }

        if (!nextIsEmpty) {
            int delCount = 0;
            int size = nextNodes.size();
            for (BusinessTreeNode nextNode : nextNodes) {
                lopper(nextNode);
                if (nextNode.isDel()) {
                    delCount++;
                }
            }
            if (!changed) {
                node.setDel(delCount == size);
            }

        }


    }


    /**
     * 获取root节点
     *
     * @param relationList 关系集合
     * @return cellId
     */
    public String findRootNodeCellId(List<ReportBusinessNodeRelation> relationList) {
        Set<String> notRootCellIds = relationList.stream()
                .map(ReportBusinessNodeRelation::getTargetCellNodeId).collect(Collectors.toSet());
        // 主节点
        String rootNodeCellId = "";
        for (ReportBusinessNodeRelation reportBusinessNodeRelation : relationList) {
            if (!notRootCellIds.contains(reportBusinessNodeRelation.getSourceCellNodeId())) {
                rootNodeCellId = reportBusinessNodeRelation.getSourceCellNodeId();
                break;
            }
        }
        return rootNodeCellId;
    }

    /**
     * 递归拼接树
     *
     * @param prevNode      prevNode
     * @param targetCellMap 关系map
     * @return 子树
     */
    public List<BusinessTreeNode> buildNextTree(
            BusinessTreeNode prevNode,
            Map<String, List<ReportBusinessNodeRelation>> targetCellMap,
            Map<String, List<SqlField>> selectedNodeMap) {
        final String prevNodeCellId = prevNode.getCellId();
        List<ReportBusinessNodeRelation> children = targetCellMap.get(prevNodeCellId);

        if (CollUtil.isNotEmpty(children)) {
            List<BusinessTreeNode> values = new ArrayList<>();
            for (ReportBusinessNodeRelation child : children) {
                BusinessTreeNode childNode = new BusinessTreeNode();
                childNode.setCellId(child.getTargetCellNodeId());
                childNode.setPrevNodeCellId(prevNode.getCellId());
                childNode.setSqlField(selectedNodeMap.get(child.getTargetCellNodeId()));

                childNode.setNextNodes(buildNextTree(
                        childNode,
                        targetCellMap, selectedNodeMap));
                values.add(childNode);
            }
            return values;
        }
        return Collections.emptyList();
    }


}
