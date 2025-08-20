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
import com.yunjian.datarelation.dto.ReportCellDTO;
import com.yunjian.datarelation.enums.CellTypeEnum;
import com.yunjian.reportbiz.preview.CellData;
import com.yunjian.reportbiz.preview.ExpressionUtils;
import com.yunjian.reportbiz.preview.FunCellEnum;
import com.yunjian.reportbiz.preview.ReportRender;
import com.yunjian.reportbiz.preview.wrappers.CellWrapper;
import com.yunjian.reportbiz.preview.wrappers.GroupCellWrapper;
import com.yunjian.reportbiz.preview.wrappers.StaticCellWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yujian
 **/
public class StaticCellHandler implements CellHandler {

    private final CellWrapper cell;

    public StaticCellHandler(CellWrapper cellWrapper) {
        this.cell = cellWrapper;
    }

    @Override
    public void handle() {
        StaticCellWrapper staticCellWrapper = (StaticCellWrapper) cell;
        ReportRender reportRender = cell.getReportRender();
        if (staticCellWrapper.getFunIndex() < 0 && cell.isStaticAndHasParent()
                && cell.getCellType() != CellTypeEnum.MAGIC) {
            ExpressionUtils.parser(staticCellWrapper, reportRender);
            // 处理指定了分组为父格的
            reportRender.addNewCell(cell.getRow(), cell.getColumn(), cell.getOrigin(),
                    cell.getOrigin().getHeight());
            return;
        }

        // 说明是函数计算单元格
        if (staticCellWrapper.getFunIndex() >= 0) {
            if (cell.getOrigin().getNewRow() == null || cell.getOrigin().getNewColumn() == null) {
                if (cell.isGroupSumFun()) {
                    cell.getOrigin().setNewRow(cell.getRow());
                    cell.getOrigin().setNewColumn(cell.getColumn());
                } else {
                    Integer row = reportRender.getRowOffset(cell, cell.getRow());
                    Integer column = reportRender.getColumnOffset(cell, cell.getColumn());
                    cell.getOrigin().setNewRow(row);
                    cell.getOrigin().setNewColumn(column);
                }

            } else {
                cell.getOrigin().setNewRow(cell.getRow());
                cell.getOrigin().setNewColumn(cell.getColumn());
            }
            reportRender.delayCells.add(cell);
            return;
        }

        if (cell.getCellType() == CellTypeEnum.MAGIC) {
            reportRender.addNewCell(staticCellWrapper.getRow(), staticCellWrapper.getColumn(),
                    cell.getOrigin(), cell.getOrigin().getHeight());
        } else {
            Integer row = cell.getRow();
            Integer column = cell.getColumn();
            if (cell.getOrigin().getNewRow() == null || cell.getOrigin().getNewColumn() == null) {
                row = reportRender.getRowOffset(cell, cell.getRow());
                column = reportRender.getColumnOffset(cell, cell.getColumn());
                cell.getOrigin().setNewRow(row);
                cell.getOrigin().setNewColumn(column);
            }
            reportRender.addNewCell(row, column,
                    cell.getOrigin(), cell.getOrigin().getHeight());
        }


    }

}
