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

import com.yunjian.datarelation.dto.ReportCellDTO;
import com.yunjian.reportbiz.preview.CellExpandEnum;
import com.yunjian.reportbiz.preview.ReportRender;
import com.yunjian.reportbiz.preview.wrappers.CellWrapper;
import com.yunjian.reportbiz.preview.wrappers.GroupCellWrapper;
import com.yunjian.reportbiz.preview.wrappers.ListCellWrapper;
import com.yunjian.reportbiz.preview.wrappers.SeqCellWrapper;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;

import org.springframework.beans.BeanUtils;
import reactor.function.Consumer4;

/**
 * @author yujian
 **/
public class SeqCellHandler implements CellHandler {


    private final SeqCellWrapper cell;

    public SeqCellHandler(CellWrapper cellWrapper) {
        this.cell = (SeqCellWrapper) cellWrapper;
    }

    @Override
    public void handle() {
        if (cell.getLoopSize() == null) {
            return;
        }
        ReportRender reportRender = cell.getReportRender();
        int newColumn = reportRender.getColumnOffset(cell, cell.getColumn());
        int newRow = reportRender.getRowOffset(cell, cell.getRow());
        int loopSize = 0;
        CellWrapper leftCell = cell.getLeftCell();
        if (leftCell instanceof GroupCellWrapper) {
            GroupCellWrapper gl = (GroupCellWrapper) leftCell;
            loopSize = gl.getGroupData().size();
        } else if (leftCell instanceof ListCellWrapper) {
            ListCellWrapper lw = (ListCellWrapper) leftCell;
            loopSize = lw.getData().size();
        }

        CellWrapper topCell = cell.getTopCell();
        if (topCell instanceof GroupCellWrapper) {
            GroupCellWrapper gl = (GroupCellWrapper) topCell;
            loopSize = gl.getGroupData().size();
        } else if (topCell instanceof ListCellWrapper) {
            ListCellWrapper lw = (ListCellWrapper) topCell;
            loopSize = lw.getData().size();
        }

        CellWrapper maxRowCell = cell.getReportRender().cellMap.values()
                .stream().max(Comparator.comparing(CellWrapper::getRow)).get();

        Integer totalRow = maxRowCell.getRow();
        if (Objects.nonNull(cell.getLeftCell())) {
            Integer leftCellRow = cell.getLeftCell().getRow();
            totalRow = totalRow - leftCellRow + 1;
        }
        loopSize = Math.max(loopSize, totalRow);

        int index = 0;
        Integer height = cell.getOrigin().getHeight();
        do {
            ReportCellDTO newCell = new ReportCellDTO();
            BeanUtils.copyProperties(cell.getOrigin(), newCell);

            newCell.setText(index + 1);

            reportRender.addNewCell(newRow, newColumn, newCell, height);
            if (cell.getExpand() == CellExpandEnum.HORIZONTAL) {
                newColumn++;
                // 如果向右移动，需要查看右面是否已经被挡住,
            } else {
                newRow++;
            }
            index++;
        } while (index < loopSize);
    }

}
