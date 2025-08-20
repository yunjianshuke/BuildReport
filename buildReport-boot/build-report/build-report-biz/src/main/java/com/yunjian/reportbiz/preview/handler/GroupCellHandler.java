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
package com.yunjian.reportbiz.preview.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.yunjian.datarelation.common.AnyData;
import com.yunjian.datarelation.dto.ReportCellDTO;
import com.yunjian.reportbiz.preview.CellData;
import com.yunjian.reportbiz.preview.CellExpandEnum;
import com.yunjian.reportbiz.preview.ExpressionUtils;
import com.yunjian.reportbiz.preview.ReportRender;
import com.yunjian.reportbiz.preview.wrappers.CellWrapper;
import com.yunjian.reportbiz.preview.wrappers.GroupCellWrapper;
import com.yunjian.reportbiz.preview.wrappers.SeqCellWrapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;

/**
 * @author yujian
 **/
public class GroupCellHandler implements CellHandler {


    private final GroupCellWrapper cell;

    /**
     * 超链接数据
     */
    private final List<List<Object>> LinkValues;


    public GroupCellHandler(CellWrapper cellWrapper) {
        this.cell = (GroupCellWrapper) cellWrapper;
        this.LinkValues = cellWrapper.getLinkData();

    }


    @Override
    public void handle() {

        ReportRender reportRender = cell.getReportRender();

        int newColumn = reportRender.getColumnOffset(cell, cell.getColumn());
        int newRow = reportRender.getRowOffset(cell, cell.getRow());
//    cell.setNewRow(newRow);
//    cell.setNewColumn(newColumn);
        Integer height = cell.getOrigin().getHeight();

        if (CollUtil.isEmpty(cell.getGroupData())) {
            ReportCellDTO newCell = new ReportCellDTO();
            BeanUtils.copyProperties(cell.getOrigin(), newCell);
            newCell.setText("");
            reportRender.addNewCell(newRow, newColumn, newCell, height);
            return;
        }

        Map<AnyData, Integer> dataMergeCountMap = cell.getDataCountMap();

        // 数据移动下标
        int index = 0;
        // 与根节点长度对齐的索引
        int lenOffset = 0;
        //  当前与根节点长度不一致则相加 直到一致为止
        int sum = 0;
        List<Integer> integers = offsetLen(cell);

        boolean formatCell = cell.isFormatCell();

        List<CellData> groupData = cell.getGroupData();
        do {
            ReportCellDTO newCell = cell.getOrigin().clone();

//      BeanUtils.copyProperties(cell.getOrigin(), newCell);
            // 这里必须想办法携带前面的
            if (formatCell) {
                newCell.setText(cell.formatDate(groupData.get(index).getValue()));
                groupData.get(index).setValue(cell.formatDate(groupData.get(index).getValue()));
            } else {
                newCell.setText(groupData.get(index).getValue());
            }

            cell.setIndex(index);

            Object parser = ExpressionUtils.parserListAndGroup(cell, reportRender, index);
            if (parser != null) {
                newCell.setText(parser);
            }

            int i = 1;
            if (dataMergeCountMap != null) {
                i = Math.max(i, dataMergeCountMap.get((AnyData) groupData.get(index).getTarget()));
            }

            if (cell.getExpand() == CellExpandEnum.HORIZONTAL) {
                newCell.setMergeColumn(0, i - 1);
            } else {
                newCell.setMergeRow(i - 1, 0);
            }
            if (CollectionUtil.isNotEmpty(LinkValues)) {
                for (int j = 0; j < newCell.getLink().getCondition().size(); j++) {
                    newCell.getLink().getCondition().get(j).setSourceValue(
                            ((AnyData) groupData.get(index).getTarget()).get(
                                    newCell.getLink().getCondition().get(j).getSourceField()));
                }
            }
            reportRender.addNewCell(newRow, newColumn, newCell, height);
            for (int x = 1; x <= i - 1; x++) {
                ReportCellDTO copyCell = newCell.clone();
                copyCell.setMergeColumn(0, 0);
                copyCell.setMergeRow(0, 0);
                if (cell.getExpand() == CellExpandEnum.HORIZONTAL) {
                    reportRender.addNewCell(newRow, newColumn + x, copyCell, height);
                } else {
                    reportRender.addNewCell(newRow + x, newColumn, copyCell, height);
                }
            }
            int offset = cell.getIndexOffSet() == null ? 0 : cell.getIndexOffSet().intValue();
            if (cell.getExpand() == CellExpandEnum.HORIZONTAL) {
                newColumn += i;

                if (integers == null) {
                    newColumn += offset;
                } else if (dataMergeCountMap == null
                        && cell.getGroupData().size() == integers.size()) {
                    newColumn += integers.get(index) - 1;
                } else if ((i + sum) == integers.get(lenOffset)) {
                    newColumn += offset;
                    lenOffset++;
                    sum = 0;
                } else {
                    sum += i;
                }


            } else {
                newRow += i;
                if (integers == null) {
                    newRow += offset;
                } else if (dataMergeCountMap == null
                        && cell.getGroupData().size() == integers.size()) {
                    newRow += (integers.get(index)) + offset - i;
                } else if ((i + sum) == integers.get(lenOffset)) {
                    newRow += offset;
                    lenOffset++;
                    sum = 0;
                } else {
                    sum += i;
                }

            }

            index++;
        } while (index < groupData.size());
    }

    /**
     * 获取单元格根节点的偏移长度
     *
     * @param cellWrapper 当前节点
     * @return 根节点长度 有序
     */
    public List<Integer> offsetLen(CellWrapper cellWrapper) {

        if (cellWrapper.isHasLeft()) {
            while (cellWrapper.getLeftCell() != null
                    && !(cellWrapper.getLeftCell() instanceof SeqCellWrapper)) {
                cellWrapper = cellWrapper.getLeftCell();
            }
        } else if (cellWrapper.isHasTop()) {
            while (cellWrapper.getTopCell() != null
                    && !(cellWrapper.getTopCell() instanceof SeqCellWrapper)) {
                cellWrapper = cellWrapper.getTopCell();
            }
        }

        if (cellWrapper.equals(cell)) {
            return null;
        }

        if (cellWrapper instanceof GroupCellWrapper) {
            Map<AnyData, Integer> dataCountMap = ((GroupCellWrapper) cellWrapper).getDataCountMap();
            if (dataCountMap == null) {
                return null;
            }
            return new LinkedList<>(dataCountMap.values());
        }
        return null;
    }

}
