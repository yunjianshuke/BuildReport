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

import com.yunjian.reportbiz.preview.CellExpandEnum;
import com.yunjian.reportbiz.preview.wrappers.CellWrapper;
import lombok.RequiredArgsConstructor;

/**
 * @author yujian
 **/
@RequiredArgsConstructor
public abstract class AbstractCellHandler implements CellHandler {

    protected final Integer pageSize;

    public Integer getRow(CellWrapper cell) {
        int newRow = cell.getRow();
        CellWrapper top = cell.getTopCell();
        do {
            if (top.getExpand() == CellExpandEnum.VERTICAL) {
                newRow += pageSize - 1;
            }
            top = top.getTopCell();
        } while (top != null);
        return newRow == 0 ? cell.getRow() : newRow;
    }

    public Integer getColumn(CellWrapper cell) {
        int newColumn = cell.getColumn();
        CellWrapper left = cell.getLeftCell();
        do {
            if (left.getExpand() == CellExpandEnum.HORIZONTAL) {
                newColumn += pageSize - 1;
            }
            left = left.getLeftCell();
        } while (left != null);
        return newColumn == 0 ? cell.getColumn() : newColumn;
    }


}
