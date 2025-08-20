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
package com.yunjian.reportbiz.preview;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.common.PreviewData;
import com.yunjian.datarelation.dto.ConditionDTO;
import com.yunjian.datarelation.dto.ReportCellDTO;
import com.yunjian.datarelation.enums.CellTypeEnum;
import com.yunjian.datarelation.utils.InterruptedUtils;
import com.yunjian.reportbiz.preview.handler.CellHandler;
import com.yunjian.reportbiz.preview.handler.HandlerFactory;
import com.yunjian.reportbiz.preview.wrappers.CellWrapper;
import com.yunjian.reportbiz.preview.wrappers.CellWrapperFactory;
import com.yunjian.reportbiz.preview.wrappers.GroupCellWrapper;
import com.yunjian.reportbiz.preview.wrappers.ListCellWrapper;
import com.yunjian.reportbiz.preview.wrappers.SeqCellWrapper;
import com.yunjian.reportbiz.preview.wrappers.StaticCellWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import reactor.function.Consumer3;

/**
 * @author yujian
 **/
@Slf4j
public class ReportRender {

    private static final char[] ALPHABETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private static final int ROW = 0, COLUMN = 1;
    Map<String, PreviewData> datasetNameMap;
    private final JSONObject originJsonObject;
    private final JSONObject dataObject;
    private final JSONArray dataFilter;
    private final JSONObject rows;
    public final JSONObject newRows;
    private final AtomicInteger rowLenAtomic;
    private final AtomicInteger columnLenAtomic;
    public final Map<String, CellWrapper> cellMap;
    /**
     * 控制单元格列的偏移
     */
    private final Map<Integer, CellWrapper> rowPushMap;
    /**
     * 控制单元格行的偏移
     */
    private final Map<Integer, CellWrapper> columnPushMap;
    /**
     * 有父格的列表和静态单元格
     */
    private final List<CellWrapper> childList;
    public final List<CellWrapper> delayCells;
    private final Integer columnLen;
    private final Integer rowLen;


    public ReportRender(String json, String dataFilter, Integer pageSize,
                        Map<String, PreviewData> datasetNameMap) {
        this.datasetNameMap = datasetNameMap;
        newRows = new JSONObject();
        this.dataFilter = new JSONArray(dataFilter);
        dataObject = new JSONObject();
        rowLenAtomic = new AtomicInteger();
        columnLenAtomic = new AtomicInteger();
        cellMap = new LinkedHashMap<>();
        rowPushMap = new LinkedHashMap<>();
        columnPushMap = new LinkedHashMap<>();
        childList = new LinkedList<>();
        delayCells = new LinkedList<>();
        originJsonObject = JSONUtil.parseObj(json);
        columnLen = originJsonObject.getJSONObject("cols").getInt("len");
        rows = originJsonObject.getJSONObject("rows");
        rowLen = rows.getInt("len");

        cellRender(rows,
                (row, column, cell) -> {
                    CellTypeEnum cellTypeEnum = CellTypeEnum.of(cell.getType());
                    CellWrapper cellWrapper = CellWrapperFactory.createCellWrapper(cellTypeEnum);
                    //基础属性
                    cellWrapper.setRow(row);
                    cellWrapper.setLinkData(new ArrayList<>());
                    cellWrapper.setColumn(column);
                    cellWrapper.setCellType(CellTypeEnum.of(cell.getType()));
                    cellWrapper.setExpand(CellExpandEnum.of(cell.getExpand()));
                    cellWrapper.setOrigin(cell);
                    cellWrapper.setFormatTemplate(cell.getFormat());
                    //父格属性
                    cellWrapper.setTopCell(getParentCell(cell.getAxisX()));
                    cellWrapper.setLeftCell(getParentCell(cell.getAxisY()));
                    cellWrapper.setHasTop(cellWrapper.getTopCell() != null);
                    cellWrapper.setHasLeft(cellWrapper.getLeftCell() != null);
                    cellWrapper.setReportRender(this);
                    cellWrapper.setGroupTotal(cell.getGroupTotal());

                    if (cellWrapper instanceof SeqCellWrapper) {
                        cellWrapper.setData(Collections.emptyList());
                        addPushMap(row, column, cellWrapper);
                        cellMap.put(cellKey(row, column), cellWrapper);
                    }
                    if (cellWrapper instanceof ListCellWrapper) {
                        PreviewData data = datasetNameMap.get(
                                cellWrapper.getOrigin().getDataset());
                        List<AnyData> datas = Collections.emptyList();
                        if (data != null) {
                            datas = data.getData();
                        }
                        cellWrapper.setData(
                                datas.stream().map(x -> x.get(cellWrapper.getOrigin().getKey())).collect(
                                        Collectors.toList()));

                        if (ObjectUtil.isNotEmpty(cellWrapper.getOrigin().getLink())) {
                            List<ConditionDTO> conditionDTOList = cellWrapper.getOrigin().getLink()
                                    .getCondition();

                            List<Object> conditionData = new ArrayList<>();
                            for (ConditionDTO conditionDTO : conditionDTOList) {
                                PreviewData anyDataList = datasetNameMap.get(
                                        conditionDTO.getSourceDatasetName());
                                if (anyDataList != null) {
                                    for (AnyData anyData : anyDataList.getData()) {
                                        conditionData.add(anyData.get(conditionDTO.getSourceField()));
                                    }
                                }
                            }
                            cellWrapper.getLinkData().add(conditionData);
                        }
                        addPushMap(row, column, cellWrapper);
                        cellMap.put(cellKey(row, column), cellWrapper);

                    }
                    if (cellWrapper instanceof GroupCellWrapper) {

                        GroupCellWrapper groupCellWrapper = (GroupCellWrapper) cellWrapper;
                        // 如果当前分组单元格没有父格那么它就是起始单元格
                        if (cellWrapper.isGroupRoot()) {
                            PreviewData data = datasetNameMap.get(
                                    cellWrapper.getOrigin().getDataset());
                            if (data != null) {
                                // 起始单元格不需要计算直接将它的数据放进去就行
                                List<CellData> groupData = data.getData().stream()
                                        .map(x -> new CellData(x.get(cellWrapper.getOrigin().getKey()), x))
                                        .collect(Collectors.collectingAndThen(
                                                Collectors.toMap(CellData::getValue, a -> a, (a, b) -> a),
                                                x -> new LinkedList<>(x.values())));
                                groupCellWrapper.setGroupData(groupData);
                                groupCellWrapper.setGroupLen(new LinkedList<>());
                                groupCellWrapper.setIndexOffSet(new AtomicInteger());
                                groupCellWrapper.getOrigin().setContext(Arrays.asList(0, 0));
                            }

                        } else {
                            if (groupCellWrapper.getIndexOffSet() == null) {
                                groupCellWrapper.setIndexOffSet(new AtomicInteger());
                            }
                            // 如果不是起始单元格 那么需要先计算子节点的数据
                            PreviewData data = datasetNameMap.get(
                                    cellWrapper.getOrigin().getDataset());
                            if (data != null) {
                                List<CellData> groupData = data.getData().stream()
                                        .map(x -> new CellData(x.get(cellWrapper.getOrigin().getKey()), x))
                                        .collect(Collectors.toList());
                                groupCellWrapper.setGroupData(groupData);
                                // 是否有上父格
                                groupSubData(cellWrapper.getTopCell(), cellWrapper);
                                // 是否有左父格
                                groupSubData(cellWrapper.getLeftCell(), cellWrapper);
                            }

                        }

                        // 填充下钻数据
                        if (ObjectUtil.isNotEmpty(cellWrapper.getOrigin().getLink())) {
                            List<ConditionDTO> conditionDTOList = cellWrapper.getOrigin().getLink()
                                    .getCondition();

                            List<Object> conditionData = new ArrayList<>();
                            for (ConditionDTO conditionDTO : conditionDTOList) {
                                PreviewData anyDataList = datasetNameMap.get(
                                        conditionDTO.getSourceDatasetName());
                                List<Object> linkData = anyDataList.getData().stream()
                                        .map(x -> x.get(conditionDTO.getSourceField()))
                                        .distinct().collect(
                                                Collectors.toList());
                                conditionData.addAll(linkData);

                            }
                            cellWrapper.getLinkData().add(conditionData);
                        }
                        addPushMap(row, column, cellWrapper);
                        cellMap.put(cellKey(row, column), cellWrapper);

                    }
                    if (cellWrapper instanceof StaticCellWrapper) {
                        StaticCellWrapper staticCellWrapper = (StaticCellWrapper) cellWrapper;

                        if (!staticCellWrapper.getFun().isEmpty()) {
                            // 这是一个函数表达式单元格
                            List<List<Integer>> cellXy = staticCellWrapper.getFun()
                                    .stream()
                                    .map(ReportRender::expr2xy)
                                    .collect(Collectors.toList());
                            staticCellWrapper.setCellXy(cellXy);

                            childList.add(cellWrapper);
                        } else {
                            // 如果列表的父格是分组 那么需要先按父格去重分组
                            // 要复制若干个列表cell
                            if (cellWrapper.getLeftCell() == null && cellWrapper.getTopCell() == null) {
                                childList.add(cellWrapper);
                            } else {
                                if (cellWrapper.getCellType() == CellTypeEnum.MAGIC) {
                                    childList.add(cellWrapper);
                                } else {
                                    if (cellWrapper.getLeftCell() instanceof GroupCellWrapper) {
                                        GroupCellWrapper leftCell = (GroupCellWrapper) cellWrapper.getLeftCell();
                                        if (leftCell.getIndexOffSet() != null) {
                                            int max = Math.max(leftCell.getIndexOffSet().intValue(),
                                                    cellWrapper.getRow() - leftCell.getRow());
                                            leftCell.getIndexOffSet().set(max);
                                            childList.add(cellWrapper);
                                        }

                                    } else if (cellWrapper.getTopCell() instanceof GroupCellWrapper) {
                                        GroupCellWrapper topCell = (GroupCellWrapper) cellWrapper.getTopCell();
                                        if (topCell.getIndexOffSet() != null) {
                                            int max = Math.max(topCell.getIndexOffSet().intValue(),
                                                    cellWrapper.getColumn() - topCell.getColumn());
                                            topCell.getIndexOffSet().set(max);
                                            childList.add(cellWrapper);
                                        }

                                    } else if (cellWrapper.getLeftCell() instanceof ListCellWrapper || cellWrapper.getTopCell() instanceof ListCellWrapper) {
                                        childList.add(cellWrapper);
                                    } else {
                                        cellMap.put(cellKey(row, column), cellWrapper);
                                    }
                                }
                            }
                        }
                    }
                });

        cellMap.forEach((k, v) -> {
            if (v instanceof SeqCellWrapper) {
                SeqCellWrapper v1 = (SeqCellWrapper) v;
                v1.setTopCell(getParentCell(v1.getOrigin().getAxisX()));
                v1.setLeftCell(getParentCell(v1.getOrigin().getAxisY()));
                v1.setHasTop(v1.getTopCell() != null);
                v1.setHasLeft(v1.getLeftCell() != null);
                CellWrapper targetCell;
                if (v1.isHasLeft()) {
                    targetCell = v1.getLeftCell();
                } else if (v1.isHasTop()) {
                    targetCell = v1.getTopCell();
                } else {
                    return;
                }
                if (targetCell instanceof ListCellWrapper) {
                    v1.setLoopSize(targetCell.getData().size());
                } else if (targetCell instanceof GroupCellWrapper) {
                    GroupCellWrapper targetGroupCell = (GroupCellWrapper) targetCell;
                    v1.setLoopSize(targetGroupCell.getGroupData().size());
                } else {
                    v1.setLoopSize(0);
                }
            }

            if (v instanceof GroupCellWrapper) {
                GroupCellWrapper v1 = (GroupCellWrapper) v;
                Map<CellData, List<CellData>> dataMergeCountMap = v1.getDataMergeCountMap();
                if (dataMergeCountMap == null) {
                    // 说明没有子节点了
                    // 先找父节点
                    if (v1.isHasLeft()) {
                        if (v1.getLeftCell() instanceof GroupCellWrapper) {
                            GroupCellWrapper leftCell = (GroupCellWrapper) v1.getLeftCell();
                            while (leftCell.isHasLeft() && leftCell.getLeftCell() instanceof GroupCellWrapper
                                    && leftCell.getDataCountMap() != null) {
                                GroupCellWrapper gcw = (GroupCellWrapper) leftCell.getLeftCell();
                                Map<AnyData, Integer> dataCountMap = leftCell.getDataCountMap();
                                gcw.getDataMergeCountMap().forEach((keyObject, values) -> {
                                    int sum = 0;
                                    for (Object value : values) {
                                        CellData cd = (CellData) value;
                                        sum = sum + dataCountMap.getOrDefault((AnyData) cd.getTarget(), 1);

                                        gcw.getDataCountMap().put((AnyData) keyObject.getTarget(), sum);
                                    }
                                });
                                leftCell = gcw;
                            }
                        }

                    }
                    if (v1.isHasTop() && v1.getTopCell() instanceof GroupCellWrapper) {
                        GroupCellWrapper topCell = (GroupCellWrapper) v1.getTopCell();
                        while (topCell.isHasTop() && topCell.getTopCell() instanceof GroupCellWrapper
                                && topCell.getDataCountMap() != null) {
                            GroupCellWrapper gcw = (GroupCellWrapper) topCell.getTopCell();
                            Map<AnyData, Integer> dataCountMap = topCell.getDataCountMap();
                            gcw.getDataMergeCountMap().forEach((keyObject, values) -> {
                                int sum = 0;
                                for (Object value : values) {
                                    CellData cd = (CellData) value;
                                    sum = sum + dataCountMap.getOrDefault((AnyData) cd.getTarget(), 1);

                                    gcw.getDataCountMap().put((AnyData) keyObject.getTarget(), sum);
                                }
                            });
                            topCell = gcw;
                        }
                    }
                }
            }
        });
        boolean hasGroup = childList.stream().filter(x -> x.isGroupSum() || x.isGroupSumFun())
                .count() > 0;
        Integer minGroupRowCell = hasGroup ? cellMap.values()
                .stream().filter(x -> x instanceof GroupCellWrapper && ((GroupCellWrapper) x).getDataCountMap() != null)
                .min(Comparator.comparing(x -> ((GroupCellWrapper) x).getDataCountMap().size()))
                .map(x -> ((GroupCellWrapper) x).getDataCountMap().size())
                .get() - 1 : 0;

        for (CellWrapper cellWrapper : childList) {
            Integer row = cellWrapper.getRow();
            Integer column = cellWrapper.getColumn();
            if (cellWrapper.getLeftCell() != null) {
                groupSubData(cellWrapper.getLeftCell(), cellWrapper);
                if (cellWrapper.getLeftCell() instanceof ListCellWrapper) {
                    ListCellWrapper leftCell = (ListCellWrapper) cellWrapper.getLeftCell();
                    for (int index = 0; index < leftCell.getData().size(); index++) {
                        CellWrapper clone = cellWrapper.clone();
                        clone.setRow(row + index);
                        clone.setIndex(index);
                        // 将复制的单元格加入待循环列表
                        cellMap.put(cellKey(clone.getRow(), clone.getColumn()), clone);
                        // 加入移动列表
                        addPushMap(clone.getRow(), clone.getColumn(), clone);
                    }
                }
                if (cellWrapper.getLeftCell() instanceof GroupCellWrapper) {
                    GroupCellWrapper leftCell = (GroupCellWrapper) cellWrapper.getLeftCell();
                    if (!cellWrapper.getOrigin().getExpression()) {
                        while (leftCell.isHasLeft() && leftCell.getLeftCell() instanceof GroupCellWrapper) {
                            leftCell = (GroupCellWrapper) leftCell.getLeftCell();
                        }
                    } else {
                        while (leftCell.isHasLeft() && leftCell.getLeftCell() instanceof GroupCellWrapper) {
                            if (leftCell.getDataCountMap() != null) {
                                break;
                            }
                            leftCell = (GroupCellWrapper) leftCell.getLeftCell();
                        }
                    }

                    Map<AnyData, Integer> dataCountMap = leftCell.getDataCountMap();
                    int index = 0;
                    int j = cellWrapper.getRow() - 1;
                    if (dataCountMap != null) {

                        for (Entry<AnyData, Integer> entry : dataCountMap.entrySet()) {
                            // 8 17 24
                            // 2 8 9 7
                            if (cellWrapper.isGroupSum() || cellWrapper.isGroupSumFun()) {
                                CellWrapper clone = cellWrapper.clone();
                                ReportCellDTO cloneOrigin = cellWrapper.getOrigin().clone();
                                clone.setOrigin(cloneOrigin);
                                j += entry.getValue();
                                clone.setRow(j);
                                clone.setGroupFunIndex(entry.getValue());
                                j += 1;
                                clone.setIndex(0);
                                // 将复制的单元格加入待循环列表
                                cellMap.put(cellKey(clone.getRow(), clone.getColumn()), clone);
                                // 加入移动列表
                                addPushMap(clone.getRow(), clone.getColumn(), clone);
                                continue;
                            }

                            for (Integer valueindex = 1; valueindex <= entry.getValue(); valueindex++) {
                                CellWrapper clone = cellWrapper.clone();
                                clone.setRow(cellWrapper.getRow() + index);
                                clone.setIndex(index);
                                // 将复制的单元格加入待循环列表
                                cellMap.put(cellKey(clone.getRow(), clone.getColumn()), clone);
                                // 加入移动列表
                                addPushMap(clone.getRow(), clone.getColumn(), clone);
                                index++;
                            }
                        }
                        if (!cellWrapper.isGroupSum() && !cellWrapper.isGroupSumFun()) {
                            for (int i = 0; i < minGroupRowCell; i++) {
                                CellWrapper clone = cellWrapper.clone();
                                clone.setRow(cellWrapper.getRow() + index);
                                clone.setIndex(index);
                                // 将复制的单元格加入待循环列表
                                cellMap.put(cellKey(clone.getRow(), clone.getColumn()), clone);
                                // 加入移动列表
                                addPushMap(clone.getRow(), clone.getColumn(), clone);
                                index++;
                            }
                        }
                    } else {
                        // 父格只有自己 或者是分组的最后一项
                        // 将复制的单元格加入待循环列表
                        CellWrapper clone = cellWrapper.clone();
                        clone.setIndex(index);
                        cellMap.put(cellKey(clone.getRow(), clone.getColumn()), clone);
                        // 加入移动列表
                        addPushMap(clone.getRow(), clone.getColumn(), clone);
                    }


                }
            } else if (cellWrapper.getTopCell() != null) {
                groupSubData(cellWrapper.getTopCell(), cellWrapper);
                if (cellWrapper.getTopCell() instanceof ListCellWrapper) {
                    ListCellWrapper leftCell = (ListCellWrapper) cellWrapper.getTopCell();
                    for (int index = 1; index <= leftCell.getData().size(); index++) {
                        CellWrapper clone = cellWrapper.clone();
                        clone.setColumn(index + column);
                        clone.setIndex(index);
                        // 将复制的单元格加入待循环列表
                        cellMap.put(cellKey(clone.getRow(), clone.getColumn()), clone);
                        // 加入移动列表
                        addPushMap(clone.getRow(), clone.getColumn(), clone);
                    }
                }
                if (cellWrapper.getTopCell() instanceof GroupCellWrapper) {
                    GroupCellWrapper topCell = (GroupCellWrapper) cellWrapper.getTopCell();

                    while (topCell.isHasTop() && topCell.getTopCell() instanceof GroupCellWrapper) {
                        topCell = (GroupCellWrapper) topCell.getTopCell();
                    }

                    Map<AnyData, Integer> dataCountMap = topCell.getDataCountMap();
                    int index = 0;
                    int j = cellWrapper.getColumn() - 1;
                    if (dataCountMap != null) {
                        for (Entry<AnyData, Integer> entry : dataCountMap.entrySet()) {
                            if (cellWrapper.isGroupSum() || cellWrapper.isGroupSumFun()) {
                                CellWrapper clone = cellWrapper.clone();
                                ReportCellDTO cloneOrigin = cellWrapper.getOrigin().clone();
                                clone.setOrigin(cloneOrigin);
                                j += entry.getValue();
                                clone.setColumn(j);
                                clone.setGroupFunIndex(entry.getValue());
                                j += 1;
                                clone.setIndex(0);
                                // 将复制的单元格加入待循环列表
                                cellMap.put(cellKey(clone.getRow(), clone.getColumn()), clone);
                                // 加入移动列表
                                addPushMap(clone.getRow(), clone.getColumn(), clone);
                                continue;
                            }
                            for (Integer valueindex = 1; valueindex <= entry.getValue(); valueindex++) {
                                CellWrapper clone = cellWrapper.clone();
                                clone.setColumn(index + cellWrapper.getColumn());
                                clone.setIndex(index);
                                // 将复制的单元格加入待循环列表
                                cellMap.put(cellKey(clone.getRow(), clone.getColumn()), clone);
                                // 加入移动列表
                                addPushMap(clone.getRow(), clone.getColumn(), clone);
                                index++;
                            }
                        }
                        if (!cellWrapper.isGroupSum() && !cellWrapper.isGroupSumFun()) {
                            for (int i = 0; i < minGroupRowCell; i++) {
                                CellWrapper clone = cellWrapper.clone();
                                clone.setColumn(index + cellWrapper.getColumn());
                                clone.setIndex(index);
                                // 将复制的单元格加入待循环列表
                                cellMap.put(cellKey(clone.getRow(), clone.getColumn()), clone);
                                // 加入移动列表
                                addPushMap(clone.getRow(), clone.getColumn(), clone);
                                index++;
                            }
                        }
                    } else {
                        // 父格只有自己 或者是分组的最后一项
                        // 将复制的单元格加入待循环列表
                        CellWrapper clone = cellWrapper.clone();
                        clone.setIndex(index);
                        cellMap.put(cellKey(clone.getRow(), clone.getColumn()), clone);
                        // 加入移动列表
                        addPushMap(clone.getRow(), clone.getColumn(), clone);
                    }

                }
            } else {
                CellWrapper clone = cellWrapper.clone();
                clone.setIndex(0);
                Integer rowOffset = getRowOffset(clone, clone.getRow());
                Integer columnOffset = getColumnOffset(clone, clone.getColumn());
                clone.setRow(rowOffset);
                clone.getOrigin().setNewColumn(columnOffset);
                clone.getOrigin().setNewRow(rowOffset);
                clone.setColumn(columnOffset);
                cellMap.put(cellKey(clone.getRow(), clone.getColumn()), clone);
                // 加入移动列表
                addPushMap(clone.getRow(), clone.getColumn(), clone);
            }
        }


    }


    public void addPushMap(Integer row, Integer column, CellWrapper cellWrapper) {
        if (cellWrapper.getExpand() == CellExpandEnum.HORIZONTAL) {
            columnPushMap.put(column, cellWrapper);
        } else if (cellWrapper.getExpand() == CellExpandEnum.VERTICAL) {
            rowPushMap.put(row, cellWrapper);
        }
    }


    public void groupSubData(CellWrapper parentCell, CellWrapper currentCell) {
        boolean currentCellIsGroup = currentCell instanceof GroupCellWrapper;
        if (parentCell != null && parentCell.getCellType() == CellTypeEnum.GROUP) {
            GroupCellWrapper groupTopCell = (GroupCellWrapper) parentCell;
            List<CellData> currentGroupData = Collections.emptyList();
            if (currentCell instanceof GroupCellWrapper) {
                GroupCellWrapper currentGroupCell = (GroupCellWrapper) currentCell;
                currentGroupCell.setIndexOffSet(groupTopCell.getIndexOffSet());
                currentGroupData = currentGroupCell.getGroupData();
            }
            currentCell.setGroupLen(new LinkedList<>());
            // 拿出父节点的数据和当前节点数据循环比对
            List<CellData> groupData = groupTopCell.getGroupData();

            if (groupData == null) {
                return;
            }

            List<String> sortedList = new LinkedList<>();

            if (parentCell.getExpand() == CellExpandEnum.VERTICAL) {
                CellWrapper tempCell = parentCell;

                do {
                    sortedList.add(tempCell.getOrigin().getKey());
                } while (tempCell.isHasLeft() && (tempCell = tempCell.getLeftCell()) != null);

            } else {
                CellWrapper tempCell = parentCell;
                do {
                    sortedList.add(tempCell.getOrigin().getKey());
                } while (tempCell.isHasTop() && (tempCell = tempCell.getTopCell()) != null);
            }

            List<CellData> sortData = new LinkedList<>();

            Map<String, List<CellData>> collect = currentGroupData.stream()
                    .collect(Collectors.groupingBy(x -> generateKey((AnyData) x.getTarget(), sortedList)));

            // 循环父节点的数据 10000
            for (CellData groupDatum : groupData) {
                InterruptedUtils.checkInterrupted("处理分组时中断");
                // 根据父节点筛选后剩下的就是当前数据对应的子数据集合
                AnyData target = (AnyData) groupDatum.getTarget();
                String parentKey = generateKey(target, sortedList);
                List<CellData> cellData = collect.getOrDefault(parentKey, Collections.emptyList());

                List<CellData> groupSub = cellData.stream()
                        .collect(Collectors.collectingAndThen(
                                Collectors.toMap(CellData::getValue, a -> a, (a, b) -> a),
                                x -> new LinkedList<>(x.values())));

                sortData.addAll(groupSub);

                if (currentCellIsGroup) {
                    // 将父格的数据与当前节点的数据集合关联后放到父节点
                    Map<CellData, List<CellData>> dataMergeCountMap = ((GroupCellWrapper) parentCell).getDataMergeCountMap();
                    Map<AnyData, Integer> dataCountMap = ((GroupCellWrapper) parentCell).getDataCountMap();
                    if (dataMergeCountMap == null) {
                        dataMergeCountMap = new LinkedHashMap<>();
                        dataMergeCountMap.put(groupDatum, groupSub);
                        ((GroupCellWrapper) parentCell).setDataMergeCountMap(dataMergeCountMap);
                    } else {
                        if (dataMergeCountMap.getOrDefault(groupDatum, Collections.emptyList())
                                .size() <= groupSub.size()) {
                            dataMergeCountMap.put(groupDatum, groupSub);
                        }
                    }
                    // 将父格的数据与当前节点的数据集合关联后数据的数量放到父节点
                    if (dataCountMap == null) {
                        dataCountMap = new LinkedHashMap<>();
                        dataCountMap.put((AnyData) groupDatum.getTarget(), groupSub.size());
                        ((GroupCellWrapper) parentCell).setDataCountMap(dataCountMap);
                    } else {
                        if (dataCountMap.getOrDefault((AnyData) groupDatum.getTarget(), 0) <= groupSub.size()) {
                            dataCountMap.put((AnyData) groupDatum.getTarget(), groupSub.size());
                        }
                    }
                    currentCell.getGroupLen().add(groupSub.size());
                }

            }
            if (currentCellIsGroup) {
                GroupCellWrapper currentCell1 = (GroupCellWrapper) currentCell;
                currentCell1.setGroupData(sortData);
                currentCell1.setMax(sortData.size());
            }

        }
    }

    private static String generateKey(AnyData target, List<String> sortedList) {
        StringBuilder keyBuilder = new StringBuilder();
        for (String key : sortedList) {
            keyBuilder.append(target.get(key));
        }
        return keyBuilder.toString();
    }


    /**
     * @param x 行
     * @param y 列
     * @return excel坐标
     */
    public static String xy2expr(int x, int y) {
        StringBuilder str = new StringBuilder();
        int cindex = x;
        while (cindex >= ALPHABETS.length) {
            cindex /= ALPHABETS.length;
            cindex -= 1;
            str.append(ALPHABETS[cindex % ALPHABETS.length]);
        }
        int last = x % ALPHABETS.length;
        str.append(ALPHABETS[last]);
        return str.toString() + (y + 1);
    }


    public static Integer indexAt(String str) {
        int ret = 0;
        for (int i = 0; i != str.length(); ++i) {
            ret = 26 * ret + (int) str.charAt(i) - 64;
        }
        return ret - 1;
    }

    public static List<Integer> expr2xy(String expr) {
        try {
            String x = "";
            String y = "";
            for (int i = 0; i < expr.length(); i += 1) {
                if (expr.charAt(i) >= '0' && expr.charAt(i) <= '9') {
                    y += expr.charAt(i);
                } else {
                    x += expr.charAt(i);
                }
            }

            return Arrays.asList(indexAt(x), Integer.parseInt(y, 10) - 1);
        } catch (Exception e) {
            return null;
        }

    }

    public JSONObject render() {
        if (rowLenAtomic.get() == 0) {
            rowLenAtomic.set(rowLen);
        }
        if (columnLenAtomic.get() == 0) {
            columnLenAtomic.set(columnLen);
        }
        List<String> merges = originJsonObject.getBeanList("merges", String.class);
        if (merges == null) {
            merges = new ArrayList<>();
        }
        // 有序循环原始结构单元格,从左到右 从上到下
        for (CellWrapper cellWrapper : cellMap.values()) {
            InterruptedUtils.checkInterrupted("渲染时线程被中断");
            final CellHandler handler = HandlerFactory.createHandler(cellWrapper);
            if (handler != null) {
                // 渲染单元格
                handler.handle();
            }
        }
        // 处理延迟计算单元格
        for (CellWrapper delayCell : delayCells) {
            StaticCellWrapper staticCellWrapper = (StaticCellWrapper) delayCell;
            FunCellEnum funPoint = FunCellEnum.of(
                    StaticCellWrapper.FUNCTION[staticCellWrapper.getFunIndex()]);

            if (funPoint != null) {

                List<List<Integer>> cellXy = staticCellWrapper.getCellXy();

                if (cellXy.size() >= 2) {
                    // 涉及到多个单元格计算 根据不同场景需要复制单元格
                    List<Integer> startXy = CollUtil.getFirst(cellXy);
                    List<Integer> endXy = CollUtil.getLast(cellXy);
                    Integer startColumn = CollUtil.getFirst(startXy);
                    Integer startRow = CollUtil.getLast(startXy);
                    Integer endColumn = CollUtil.getFirst(endXy);
                    Integer endStart = CollUtil.getFirst(endXy);
                    if (endColumn > startColumn && endStart > startRow) {

                    }

                } else {
                    // 单个单元格聚合
                    List<Integer> xy = CollUtil.getFirst(cellXy);
                    if (xy == null) {
                        continue;
                    }
                    Integer column = CollUtil.getFirst(xy);
                    Integer row = CollUtil.getLast(xy);
                    CellWrapper cellWrapper = getByCellMap(row, column);

                    if (delayCell.isGroupSumFun()) {
                        List<Object> objects = new ArrayList<>();
                        if (cellWrapper != null && cellWrapper.isHasLeft() && delayCell.getGroupFunIndex() != null) {
                            for (Integer i = 1; i <= delayCell.getGroupFunIndex(); i++) {
                                int offset = delayCell.getRow() - i;
                                ReportCellDTO leftCell = getByCellOfNewRows(offset,
                                    cellWrapper.getColumn());
                                if (leftCell != null && leftCell.getText() != null) {
                                    objects.add(leftCell.getText());
                                }
                            }
                        } else if (delayCell.getGroupFunIndex() != null) {
                            for (Integer i = 1; i <= delayCell.getGroupFunIndex(); i++) {
                                int offset = delayCell.getColumn() - i;
                                ReportCellDTO topCell = getByCellOfNewRows(cellWrapper.getRow(),
                                        offset);
                                if (topCell != null && topCell.getText() != null) {
                                    objects.add(topCell.getText());
                                }
                            }
                        }
                        if (!objects.isEmpty()) {
                            delayCell.getOrigin().setText(funPoint.processSingle(objects));
                        } else {
                            delayCell.getOrigin().setText(funPoint.processSingle(cellWrapper.getData()));
                        }
                        addNewCell(delayCell.getOrigin().getNewRow(), delayCell.getOrigin().getNewColumn(), delayCell.getOrigin(), delayCell.getOrigin().getHeight());
                        continue;
                    }

                    if (cellWrapper instanceof GroupCellWrapper) {
                        GroupCellWrapper groupCellWrapper = (GroupCellWrapper) cellWrapper;
                        List<CellData> collect = groupCellWrapper.getGroupData();
                        List<Object> val = collect.stream().map(CellData::getValue)
                                .collect(Collectors.toList());
                        delayCell.getOrigin().setText(funPoint.processSingle(val));
                    } else {
                        int index = delayCell.getRow() - cellWrapper.getRow();
                        List<Object> objects = new ArrayList<>();
                        if (cellWrapper.isHasLeft()) {
                            for (int i = 1; i <= index; i++) {
                                int nrow = delayCell.getRow() - i;
                                ReportCellDTO leftCell = getByCellOfNewRows(nrow,
                                        cellWrapper.getColumn());
                                if (leftCell != null && leftCell.getText() != null) {
                                    objects.add(leftCell.getText());
                                }
                            }
                        } else {
                            for (int i = 1; i <= index; i++) {
                                int ncolumn = delayCell.getColumn() - i;
                                ReportCellDTO topCell = getByCellOfNewRows(
                                        cellWrapper.getRow(), ncolumn);
                                if (topCell != null && topCell.getText() != null) {
                                    objects.add(topCell.getText());
                                }
                            }
                        }
                        if (!objects.isEmpty()) {
                            delayCell.getOrigin().setText(funPoint.processSingle(objects));
                        } else {
                            delayCell.getOrigin().setText(funPoint.processSingle(cellWrapper.getData()));
                        }

                    }
                    addNewCell(delayCell.getOrigin().getNewRow(), delayCell.getOrigin().getNewColumn(), delayCell.getOrigin(), delayCell.getOrigin().getHeight());
                }
            }
        }
        // 添加合并单元格
        List<String> finalMerges = merges;
        cellRender(newRows, (row, column, cell) -> {
            if (CollUtil.isNotEmpty(
                    cell.getMerge())) {
                List<Integer> merge = cell.getMerge();
                String x, y;
                if (CellExpandEnum.of(cell.getExpand()) == CellExpandEnum.VERTICAL) {
                    // 纵向
                    x = xy2expr(column, row);
                    int newRow = CollUtil.getFirst(merge) + row;
                    y = xy2expr(column, newRow);
                } else {
                    x = xy2expr(column, row);
                    int newColumn = CollUtil.getLast(merge) + column;
                    y = xy2expr(newColumn, row);
                }
                if (!x.equals(y)) {
                    finalMerges.add(x + ":" + y);
                }
            }
        });

        // 将新的rows放进来
        originJsonObject.set("rows", newRows);
        originJsonObject.set("merges", merges);
        // 变更最大行和列
        originJsonObject.getJSONObject("cols").set("len", columnLenAtomic.get());
        newRows.set("len", rowLenAtomic.get() + 1);
        dataObject.set("tableInfo", originJsonObject);
        handleDataFilter(dataFilter);
        dataObject.set("dataFilter", dataFilter);
        return dataObject;
    }

    private void handleDataFilter(JSONArray dataFilter) {

    }

    public CellWrapper getByCellMap(int row, int column) {
        return cellMap.get(cellKey(row, column));
    }

    public ReportCellDTO getByCellOfNewRows(int row, int column) {
        JSONObject rows = newRows.getJSONObject(String.valueOf(row));
        if (rows != null) {
            JSONObject cell = rows.getJSONObject("cells")
                    .getJSONObject(String.valueOf(column));
            return JSONUtil.toBean(cell, ReportCellDTO.class);
        }
        return null;
    }

    public String getByCellOfNewRowsByDatasetNameMap(int row, int column, int index) {
        JSONObject rowsData = rows.getJSONObject(String.valueOf(row));
        if (rowsData != null) {
            JSONObject cell = rowsData.getJSONObject("cells")
                    .getJSONObject(String.valueOf(column));
            String dataset = (String) cell.get("dataset");
            String key = (String) cell.get("key");
            String result = Convert.toStr(datasetNameMap.get(dataset).getData().get(index).get(key));
            return result;
        }
        return null;
    }


    public void addNewCell(Integer row, Integer column, ReportCellDTO cell, Integer height) {


        if (cell.getContext() == null && cell.getStyle() == null) {
            return;
        }

        final String columnStr = String.valueOf(column);
        final String rowStr = String.valueOf(row);
        newRows.computeIfAbsent(rowStr, k -> new JSONObject());
        if (column >= columnLenAtomic.intValue()) {
            columnLenAtomic.incrementAndGet();
        }
        if (row >= rowLenAtomic.intValue()) {
            rowLenAtomic.incrementAndGet();
        }
        JSONObject rowJson = newRows.getJSONObject(rowStr);
        rowJson.computeIfAbsent("cells", k -> new JSONObject());
        if (height != null && height > 0) {
            rowJson.computeIfAbsent("height", k -> height);
        }
//    cell.setNewRow(row);
//    cell.setNewColumn(column);
        rowJson.getJSONObject("cells").set(columnStr, new JSONObject(cell));

    }


    public Integer getColumnOffset(CellWrapper cellWrapper, Integer column) {
        int offset = 0;
        for (int i = 1; i <= column; i++) {
            CellWrapper left = columnPushMap.get(column - i);
            if (left != null) {
                switch (left.getCellType()) {
                    case SEQUENCE:
                    case LIST:
                        offset += left.getData().size();
                        break;
                    case GROUP:
                        GroupCellWrapper groupCellWrapper = (GroupCellWrapper) left;
                        Map<AnyData, Integer> dataCountMap = groupCellWrapper.getDataCountMap();
                        if (groupCellWrapper.getGroupData() == null || groupCellWrapper.getGroupData().isEmpty()) {
                            offset += 1;
                            break;
                        }
                        if (dataCountMap != null) {
                            Integer max = dataCountMap.values().stream()
                                    .reduce(0, Integer::sum);
                            offset += max + (groupCellWrapper.getIndexOffSet() == null ? 0
                                    : groupCellWrapper.getIndexOffSet().intValue() * (
                                    groupCellWrapper.getGroupLen().size() - 1));
                        } else {
                            offset += groupCellWrapper.getGroupData().size() + (
                                    groupCellWrapper.getIndexOffSet() == null ? 0
                                            : groupCellWrapper.getIndexOffSet().intValue()
                                            * (groupCellWrapper.getGroupLen().size() - 1));
                        }

                        break;
                    case STATIC:
                        offset = left.getIndex() + 1;
                        break;
                    default:
                        break;
                }
            } else {
                offset += 1;
            }
        }
        return offset;
    }

    public Integer getRowOffset(CellWrapper cellWrapper, Integer row) {
        int offset = 0;
        for (int i = 1; i <= row; i++) {
            CellWrapper top = rowPushMap.get(row - i);
            if (top != null) {
                switch (top.getCellType()) {
                    case SEQUENCE:
                    case LIST:
                        offset += top.getData().size();
                        break;
                    case GROUP:
                        GroupCellWrapper groupCellWrapper = (GroupCellWrapper) top;
                        if (groupCellWrapper.getGroupData() == null || groupCellWrapper.getGroupData().isEmpty()) {
                            offset += 1;
                            break;
                        }
                        Map<AnyData, Integer> dataCountMap = groupCellWrapper.getDataCountMap();
                        if (dataCountMap != null) {
                            Integer max = dataCountMap.values().stream()
                                    .reduce(0, Integer::sum);
                            offset += max + (groupCellWrapper.getIndexOffSet() == null ? 0
                                    : groupCellWrapper.getIndexOffSet().intValue() * (
                                    groupCellWrapper.getGroupLen().size() - 1));
                        } else {
                            offset += groupCellWrapper.getGroupData().size() + (
                                    groupCellWrapper.getIndexOffSet() == null ? 0
                                            : groupCellWrapper.getIndexOffSet().intValue()
                                            * (groupCellWrapper.getGroupLen().size() - 1));
                        }

                        break;
                    case STATIC:
                        offset = top.getIndex() + 1;
                        break;
                    default:
                        break;
                }
            } else {
                offset += 1;
            }
        }
        return offset;
    }

    /**
     * 获取当前单元格的父格优先找左边的
     *
     * @return 父格
     */
    public CellWrapper getParentCell(List<Integer> axis) {

        if (CollUtil.isNotEmpty(axis) && axis.size() == 2) {
            Integer row = CollUtil.getLast(axis);
            Integer column = CollUtil.getFirst(axis);
            return cellMap.get(cellKey(row, column));
        }
        return null;
    }


    public String cellKey(Integer row, Integer column) {
        return row + StringPool.COMMA + column;
    }

    /**
     * 循环所有单元格 先行 -> 行中每一列
     *
     * @param rows         原始报表json
     * @param cellConsumer 消费cell
     */
    public static void cellRender(JSONObject rows,
                                  Consumer3<Integer, Integer, ReportCellDTO> cellConsumer) {
        // 单元格配置
        List<ReportCellDTO> cellList = new LinkedList<>();
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);

        try {
            for (Entry<String, Object> row : rows.entrySet()) {
                if (row.getValue() instanceof JSONObject) {
                    // 当前行
                    final String rowNumber = row.getKey();
                    JSONObject json = (JSONObject) row.getValue();
                    // 当前列
                    JSONObject cells = json.getJSONObject("cells");
                    Integer height = json.getInt("height");
                    for (Entry<String, Object> cell : cells.entrySet()) {
                        final String cellNumber = cell.getKey();
                        String jsonStr = objectMapper.writeValueAsString(cell.getValue());
                        ReportCellDTO cellInfo = objectMapper.readValue(
                                jsonStr,
                                ReportCellDTO.class);
                        if (cellInfo.getExpression() == null) {
                            cellInfo.setExpression(false);
                        }
                        if (cellInfo.getText() instanceof JSONNull) {
                            cellInfo.setText(null);
                        }
                        cellInfo.setHeight(height);
                        cellInfo.setRow(Integer.parseInt(rowNumber));
                        cellInfo.setColumn(Integer.parseInt(cellNumber));
                        String coordinate = xy2expr(Integer.parseInt(cellNumber), Integer.parseInt(rowNumber));
                        cellInfo.setCoordinate(coordinate);
                        cellList.add(cellInfo);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            log.error("渲染单元格异常", e);
        }

        cellList.stream()
                .sorted(Comparator.comparing(ReportCellDTO::getRow).thenComparing(ReportCellDTO::getColumn))
                .forEach(cell -> cellConsumer.accept(cell.getRow(), cell.getColumn(),
                        cell));


    }

}
