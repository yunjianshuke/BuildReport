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
package com.yunjian.reportbiz.preview.export;

import cn.hutool.json.JSONUtil;
import com.yunjian.datarelation.dto.ReportCellDTO;
import com.yunjian.datarelation.enums.CellTypeEnum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;


/**
 * @author yujian
 **/
@Slf4j
public class FileGenerator {

    public int maxRow;
    public int maxColumn;

    public List<ReportCellDTO> fillEmptyData(List<ReportCellDTO> data) {

        ArrayList<ReportCellDTO> newData = new ArrayList<>(data);

        int tempRow = 0;
        int tempColumn = 0;
        for (ReportCellDTO datum : data) {
            tempRow = Math.max(datum.getRow(), tempRow);
            tempColumn = Math.max(datum.getColumn(), tempColumn);
        }
        maxColumn = tempColumn;
        maxRow = tempRow;

        log.info("最大row {} 最大column {}", maxRow, maxColumn);

        Map<String, ReportCellDTO> cellMap = data.stream()
                .collect(Collectors.toMap(x -> x.getRow() + "_" + x.getColumn(), x -> x));

        for (int rowIndex = 0; rowIndex <= maxRow; rowIndex++) {
            for (int columnIndex = 0; columnIndex <= maxColumn; columnIndex++) {
                if (!cellMap.containsKey(rowIndex + "_" + columnIndex)) {
                    ReportCellDTO fillEmptyCell = new ReportCellDTO();
                    fillEmptyCell.setRow(rowIndex);
                    fillEmptyCell.setColumn(columnIndex);
                    fillEmptyCell.setHeight(-1);
                    fillEmptyCell.setText("");
                    fillEmptyCell.setType(CellTypeEnum.STATIC.getType());
                    log.info("新增 row {} column {}", rowIndex, columnIndex);
                    newData.add(fillEmptyCell);
                }
            }
        }
        // log.info("导出填充数据 {}", JSONUtil.toJsonStr(newData));
        return newData.stream()
                .sorted(Comparator.comparing(ReportCellDTO::getRow).thenComparing(ReportCellDTO::getColumn))
                .collect(Collectors.toList());
    }
}
