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
import cn.hutool.json.JSONObject;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.yunjian.reportbiz.preview.ReportRender;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

/**
 * @author yujian
 * @description
 * @create 2024-09-13 13:54
 **/
@RequiredArgsConstructor
public class StyleHandler implements CellWriteHandler {

    private final Map<String, JSONObject> styleMap;
    private final Map<String, XSSFCellStyle> cellStyleMap;

    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        Cell cell = context.getCell();
        Sheet sheetAt = context.getWriteWorkbookHolder().getWorkbook().getSheetAt(0);
        Integer rowIndex = cell.getRowIndex();
        Integer columnIndex = cell.getColumnIndex();
        Workbook workbook = cell.getSheet().getWorkbook();
        JSONObject styleJson = styleMap.get(rowIndex + "_" + columnIndex);

        if (styleJson == null) {
            // 由于单元格是复制的不知道应该是什么样式 如果是处于合并单元格那么复制首个单元格的样式
            // 比如 A1:J1 从 B1 - J1 的样式继承于A1的样式
            for (CellRangeAddress mergedRegion : sheetAt.getMergedRegions()) {
                boolean inRange = mergedRegion.isInRange(cell);
                if (inRange) {
                    List<Integer> integers = ReportRender.expr2xy(
                            mergedRegion.formatAsString().split(":")[0]);
                    Integer column = CollUtil.getFirst(integers);
                    Integer row = CollUtil.getLast(integers);
                    styleJson = styleMap.get(row + "_" + column);
                    break;
                }
            }
        }

        if (styleJson != null) {
            XSSFCellStyle cStyle = cellStyleMap.get(styleJson.toJSONString(0));
            if (cStyle == null) {
                cStyle = (XSSFCellStyle) context.getWriteWorkbookHolder().getWorkbook()
                        .createCellStyle();
                if (styleJson.containsKey("border")) {
                    JSONObject border = styleJson.getJSONObject("border");
                    if (border.containsKey("bottom")) {
                        cStyle.setBorderBottom(BorderStyle.THIN);
                    }
                    if (border.containsKey("right")) {
                        cStyle.setBorderRight(BorderStyle.THIN);
                    }
                    if (border.containsKey("top")) {
                        cStyle.setBorderTop(BorderStyle.THIN);
                    }
                    if (border.containsKey("left")) {
                        cStyle.setBorderLeft(BorderStyle.THIN);
                    }
                }
                if (styleJson.containsKey("align")) {
                    String align = styleJson.getStr("align");
                    if ("center".equals(align)) {
                        cStyle.setAlignment(HorizontalAlignment.CENTER);
                        cStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    } else if ("left".equals(align)) {
                        cStyle.setAlignment(HorizontalAlignment.LEFT);
                    } else if ("right".equals(align)) {
                        cStyle.setAlignment(HorizontalAlignment.RIGHT);
                    }
                }
                if (styleJson.containsKey("font")) {
                    JSONObject font = styleJson.getJSONObject("font");
                    XSSFFont f = (XSSFFont) workbook.createFont();
                    if (font.containsKey("bold")) {
                        f.setBold(true);
                    }
                    if (font.containsKey("size")) {
                        Short fontSize = font.getShort("size");
                        f.setFontHeightInPoints(fontSize);
                    }
                    if (font.containsKey("name")) {
                        f.setFontName(font.getStr("name"));
                    }
                    cStyle.setFont(f);
                }

                if (styleJson.containsKey("color")) {
                    String bgcolor = styleJson.getStr("color");
                    XSSFFont f = (XSSFFont) workbook.createFont();
                    f.setBold(cStyle.getFont().getBold());
                    f.setColor(new XSSFColor(Color.decode(bgcolor)));
                    cStyle.setFont(f);
                }

                if (styleJson.containsKey("bgcolor")) {
                    String bgcolor = styleJson.getStr("bgcolor");
                    cStyle.setFillForegroundColor(new XSSFColor(Color.decode(bgcolor)));
                    cStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                }
                cellStyleMap.put(styleJson.toJSONString(0), cStyle);
            }
            context.getFirstCellData().setOriginCellStyle(cStyle);

        }
    }


}
