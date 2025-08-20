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
import com.yunjian.datarelation.dto.ReportCellDTO;
import com.yunjian.reportbiz.preview.CellExpandEnum;
import com.yunjian.reportbiz.preview.ExpressionUtils;
import com.yunjian.reportbiz.preview.ReportRender;
import com.yunjian.reportbiz.preview.wrappers.CellWrapper;

import java.util.List;

import org.springframework.beans.BeanUtils;

/**
 * @author yujian
 **/
public class ListCellHandler implements CellHandler {


    private final List<Object> values;
    private final List<List<Object>> LinkValues;

    private final CellWrapper cell;

    public ListCellHandler(CellWrapper cellWrapper) {
        this.cell = cellWrapper;
        this.values = cellWrapper.getData();
        this.LinkValues = cellWrapper.getLinkData();
    }

    @Override
    public void handle() {
        if (CollUtil.isEmpty(values)) {
            return;
        }
        ReportRender reportRender = cell.getReportRender();
        int newColumn = reportRender.getColumnOffset(cell, cell.getColumn());
        int newRow = reportRender.getRowOffset(cell, cell.getRow());
        int index = 0;
        Integer height = cell.getOrigin().getHeight();
        boolean formatCell = cell.isFormatCell();
        do {
            cell.setIndex(index);
            ReportCellDTO newCell = new ReportCellDTO();
            BeanUtils.copyProperties(cell.getOrigin(), newCell);
            if (formatCell) {
                newCell.setText(cell.formatDate(values.get(index)));
            } else {
                newCell.setText(values.get(index));

            }
            Object parser = ExpressionUtils.parserListAndGroup(cell, reportRender, index);
            if (parser != null) {
                newCell.setText(parser);
            }
            if (CollectionUtil.isNotEmpty(LinkValues)) {
                for (int i = 0; i < newCell.getLink().getCondition().size(); i++) {
                    newCell.getLink().getCondition().get(i).setSourceValue(LinkValues.get(i).get(index));
                }
            }
            reportRender.addNewCell(newRow, newColumn, newCell, height);
            if (cell.getExpand() == CellExpandEnum.HORIZONTAL) {
                newColumn++;
                // 如果向右移动，需要查看右面是否已经被挡住,
            } else {
                newRow++;
            }
            index++;
        } while (index < values.size());
    }
}
