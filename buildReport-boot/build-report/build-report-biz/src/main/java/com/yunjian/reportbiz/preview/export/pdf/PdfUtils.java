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

import cn.hutool.json.JSONObject;
import com.itextpdf.kernel.colors.WebColors;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author yujian
 **/
public class PdfUtils {


    /**
     * 创建空白页
     *
     * @param outputStream 输出io
     * @return pdf
     */
    protected static Document createDocument(OutputStream outputStream) {
        PdfWriter pdfWriter = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        pdfDocument.addNewPage();
        return new Document(pdfDocument, PageSize.A4);
    }

    /**
     * 创建表格
     *
     * @return 返回表格
     */
    protected static Table createTable(int column) {
        return new Table(UnitValue.createPercentArray(column + 1)).useAllAvailableWidth();
    }

    @SuppressWarnings("all")
    protected static Cell createTableCell(Integer row, Integer column, String text, JSONObject style,
                                          PdfFont pdfFont, Integer width)
            throws IOException {
        Cell cell;
        if (row == null || column == null) {
            cell = new Cell();
        } else {
            cell = new Cell(column + 1, row + 1);
        }
        if (width != null) {
            cell.setWidth(width);
        }

        Paragraph paragraph = new Paragraph(text);
        if (!"".equals(text)) {
            paragraph.setFont(pdfFont);
            paragraph.setFontSize(10f);
        }

        if (style != null) {
            if (style.containsKey("align")) {
                String align = style.getStr("align");
                if ("center".equals(align)) {
                    cell.setTextAlignment(TextAlignment.CENTER);
                } else if ("left".equals(align)) {
                    cell.setTextAlignment(TextAlignment.LEFT);
                } else if ("right".equals(align)) {
                    cell.setTextAlignment(TextAlignment.RIGHT);
                }
            }

            if (!style.containsKey("border")) {
                cell.setBorder(Border.NO_BORDER);
            } else {
                JSONObject border = style.getJSONObject("border");
                if (border.size() < 4) {
                    if (border.containsKey("bottom")) {
                        cell.setBorderBottom(new SolidBorder(0.5F));
                    } else {
                        cell.setBorderBottom(Border.NO_BORDER);
                    }
                    if (border.containsKey("right")) {
                        cell.setBorderRight(new SolidBorder(0.5F));
                    } else {
                        cell.setBorderRight(Border.NO_BORDER);
                    }
                    if (border.containsKey("top")) {
                        cell.setBorderTop(new SolidBorder(0.5F));
                    } else {
                        cell.setBorderTop(Border.NO_BORDER);
                    }
                    if (border.containsKey("left")) {
                        cell.setBorderLeft(new SolidBorder(0.5F));
                    } else {
                        cell.setBorderLeft(Border.NO_BORDER);
                    }
                }
            }
            if (style.containsKey("font")) {
                JSONObject font = style.getJSONObject("font");
                if (font.containsKey("name")) {
                    String fontName = font.getStr("name");
                    if (PdfFontFactory.isRegistered(fontName.toUpperCase())) {
                        PdfFont f = PdfFontFactory.createRegisteredFont(fontName.toUpperCase());
                        paragraph.setFont(f);
                    }
                }
                if (font.containsKey("bold")) {
                    paragraph.setBold();
                }
                if (font.containsKey("size")) {
                    Integer fontSize = font.getInt("size");
                    paragraph.setFontSize(fontSize);
                }
            }
            if (style.containsKey("bgcolor")) {
                String bgcolor = style.getStr("bgcolor");
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor));
            }
            if (style.containsKey("color")) {
                String color = style.getStr("color");
                cell.setFontColor(WebColors.getRGBColor(color));
            }
        } else {
            cell.setBorder(Border.NO_BORDER);
        }

        cell.add(paragraph);
        return cell;
    }
}
