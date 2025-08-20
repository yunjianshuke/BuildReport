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
package com.yunjian.reportbiz.preview.export.pdf;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.yunjian.datarelation.dto.ReportCellDTO;
import com.yunjian.datarelation.utils.InterruptedUtils;
import com.yunjian.reportbiz.preview.export.FileGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;


/**
 * @author yujian
 **/
@Slf4j
public class PdfGenerator extends FileGenerator {


    public PdfGenerator(List<ReportCellDTO> data, JSONArray styles, JSONObject cols) {
        this.data = data;
        this.styles = styles;
        this.cols = cols;
    }

    private List<ReportCellDTO> data;
    private JSONArray styles;
    private JSONObject cols;


    public void downloadPdf(AtomicBoolean flag, OutputStream outputStream) throws IOException {
        // 填充空白单元格
        data = fillEmptyData(data);
        try (Document doc = PdfUtils.createDocument(outputStream)) {
            doc.setMargins(1F, 1F, 1F, 1F);
            Table table = PdfUtils.createTable(maxColumn);
            table.setBorder(Border.NO_BORDER);
            table
                .setTextAlignment(TextAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.TOP);
            // 作为默认字体使用 必须在循环体外定义
            PdfFont font = PdfFontFactory.createRegisteredFont("SIMHEI");

            // 提前先提前先提前先收集所有合并区域
            Set<String> mergedArea = new HashSet<>();
            for (ReportCellDTO preCell : data) {
                Integer rowMerge = CollUtil.getFirst(preCell.getMerge());
                Integer colMerge = CollUtil.getLast(preCell.getMerge());
                if (rowMerge == null || colMerge == null) {
                    continue;
                }

                // 计算合并区域范围
                int startRow = preCell.getRow();
                int startCol = preCell.getColumn();
                int endRow = startRow + rowMerge;  // 行合并结束位置
                int endCol = startCol + colMerge;  // 列合并结束位置

                // 记录所有被合并的单元格（行_列格式）
                for (int r = startRow; r <= endRow; r++) {
                    for (int c = startCol; c <= endCol; c++) {
                        // 跳过起始单元格（第一个单元格需要渲染）
                        if (r == startRow && c == startCol) {
                            continue;
                        }
                        mergedArea.add(r + "_" + c);
                    }
                }
            }

            for (ReportCellDTO cell : data) {
                System.out.println(JSONUtil.toJsonStr(cell));
                InterruptedUtils.checkInterrupted("导出Pdf数据时中断");

                // 判断当前单元格是否在合并区域内（被合并的单元格）
                String cellKey = cell.getRow() + "_" + cell.getColumn();
                if (mergedArea.contains(cellKey)) {
                    log.info("跳过合并区域单元格: {}", JSONUtil.toJsonStr(cell));
                    continue;
                }

                // 保留原有样式处理逻辑
                JSONObject styleArrayObject = null;
                if (cell.getStyle() != null) {
                    styleArrayObject = styles.getJSONObject(Integer.parseInt(cell.getStyle()));
                }

                Integer column = CollUtil.getFirst(cell.getMerge());
                Integer row = CollUtil.getLast(cell.getMerge());

                Integer width = null;
                if (cols != null) {
                    JSONObject jsonObject = cols.getJSONObject(String.valueOf(cell.getColumn()));
                    if (jsonObject != null && jsonObject.containsKey("width")) {
                    }
                }

                Cell pdfCell = PdfUtils.createTableCell(row, column,
                    String.valueOf(cell.getText() == null ? "" : cell.getText()), styleArrayObject, font,
                    width);
                pdfCell.setPadding(2F);
                table.addCell(pdfCell);
            }
            flag.getAndSet(true);
            doc.add(table);
            InterruptedUtils.checkInterrupted("导出Pdf数据时中断");
        } catch (Exception e) {
            log.error("pdf export", e);
        }

    }
}
