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
package com.yunjian.reportbiz.preview.export.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.yunjian.datarelation.dto.ReportCellDTO;
import com.yunjian.datarelation.utils.InterruptedUtils;
import com.yunjian.reportbiz.preview.export.FileGenerator;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * @author yujian
 **/
@Slf4j
public class ExcelGenerator extends FileGenerator {

    public ExcelGenerator(List<ReportCellDTO> data, JSONArray styles) {
        this.data = data;
        this.styles = styles;
    }

    private List<ReportCellDTO> data;
    private JSONArray styles;

    public void downloadExcel(AtomicBoolean flag, OutputStream outputStream) {
        data = fillEmptyData(data);
        List<List<Object>> rows = new ArrayList<>();
        List<CellRangeAddress> cellRangeAddresses = new ArrayList<>();
        Map<String, JSONObject> styleMap = new HashMap<>();
        int columnIndex = 0;
        for (int i = 0; i <= maxRow; i++) {
            InterruptedUtils.checkInterrupted("导出Excel数据时中断");
            ArrayList<Object> columns = new ArrayList<>();

            for (int j = columnIndex; j <= columnIndex + maxColumn; j++) {
                ReportCellDTO reportCellDTO = data.get(j);
                JSONObject styleArrayObject = null;
                if (reportCellDTO.getStyle() != null) {
                    styleArrayObject = styles.getJSONObject(Integer.parseInt(reportCellDTO.getStyle()));
                    styleMap.put(reportCellDTO.getRow() + "_" + reportCellDTO.getColumn(), styleArrayObject);
                }

                Integer row = CollUtil.getLast(reportCellDTO.getMerge());
                Integer column = CollUtil.getFirst(reportCellDTO.getMerge());
                if (column != null && row != null) {

                    if (row > 0) {
                        // 横向合并
                        CellRangeAddress cellAddresses = new CellRangeAddress(reportCellDTO.getRow(),
                                reportCellDTO.getRow(),
                                reportCellDTO.getColumn(),
                                reportCellDTO.getColumn() + row);

                        cellRangeAddresses.add(cellAddresses);
                    }
                    if (column > 0) {
                    //纵向合并
                        CellRangeAddress cellAddresses = new CellRangeAddress(reportCellDTO.getRow(),
                                reportCellDTO.getRow() + column,
                                reportCellDTO.getColumn(),
                                reportCellDTO.getColumn());
                        cellRangeAddresses.add(cellAddresses);
                    }
                }
                columns.add(String.valueOf(data.get(j).getText()));
            }
            columnIndex += maxColumn + 1;
            rows.add(columns);
        }

        flag.getAndSet(true);
        InterruptedUtils.checkInterrupted("导出Excel数据完成时中断");
        EasyExcel.write(outputStream)
                .useDefaultStyle(false)
                .autoCloseStream(true)
                .sheet("sheet1")
                .registerWriteHandler(new MergeHandler(cellRangeAddresses))
                .registerWriteHandler(new StyleHandler(styleMap, new HashMap<>()))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .doWrite(rows);
    }
}
