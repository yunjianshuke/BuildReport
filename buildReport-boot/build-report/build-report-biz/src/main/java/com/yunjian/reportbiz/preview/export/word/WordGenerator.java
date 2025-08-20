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
package com.yunjian.reportbiz.preview.export.word;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.yunjian.datarelation.dto.ReportCellDTO;
import com.yunjian.datarelation.utils.InterruptedUtils;
import com.yunjian.reportbiz.preview.export.FileGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

/**
 * @author yujian
 **/
@Slf4j
public class WordGenerator extends FileGenerator {

    public WordGenerator(List<ReportCellDTO> data, JSONArray styles) {
        this.data = data;
        this.styles = styles;
    }

    private List<ReportCellDTO> data;
    private JSONArray styles;

    @Data
    @AllArgsConstructor
    class Merge {

        int row;
        int column;
        int offset;
    }

    public void downloadWord(AtomicBoolean flag, OutputStream outputStream) throws IOException {
        data = fillEmptyData(data);

        try (XWPFDocument wordDoc = new XWPFDocument()) {

            XWPFTable table = wordDoc.createTable(maxRow + 1, maxColumn + 1);
            CTTblWidth ctTblWidth = table.getCTTbl().addNewTblPr().addNewTblW();
            ctTblWidth.setType(STTblWidth.DXA);
            ctTblWidth.setW(BigInteger.valueOf(4940));
            CTTblGrid ctTblGrid = table.getCTTbl().addNewTblGrid();
            for (int c = 0; c < maxColumn + 1; c++) {
                ctTblGrid.addNewGridCol().setW(BigInteger.valueOf(4000 / maxColumn + 1));
            }

            List<Merge> columnMerge = new ArrayList<>();
            List<Merge> rowMerge = new ArrayList<>();
            for (ReportCellDTO cell : data) {
                InterruptedUtils.checkInterrupted("导出Word数据时中断");
                XWPFTableCell wordCell = table.getRow(cell.getRow()).getCell(cell.getColumn());

                Integer row = CollUtil.getLast(cell.getMerge());
                Integer column = CollUtil.getFirst(cell.getMerge());
                if (column != null && row != null) {
                    if (column > row && column > 0) {
                        columnMerge.add(new Merge(cell.getRow(), cell.getColumn(), column));
                    } else if (row > 0) {
                        rowMerge.add(new Merge(cell.getRow(), cell.getColumn(), row));
                    }
                }
                CTP ctP = (wordCell.getCTTc().sizeOfPArray() == 0) ? wordCell.getCTTc().addNewP()
                        : wordCell.getCTTc().getPArray(0);
                XWPFRun run = wordCell.getParagraph(ctP).createRun();
                if (cell.getStyle() != null) {
                    JSONObject styleJson = styles.getJSONObject(Integer.parseInt(cell.getStyle()));
                    if (styleJson != null) {
                        if (!styleJson.containsKey("border")) {
                            CTTcPr tcPr = wordCell.getCTTc().addNewTcPr();
                            CTTcBorders tcBorders = tcPr.addNewTcBorders();
                            tcBorders.addNewBottom().setVal(STBorder.NONE);
                            tcBorders.addNewLeft().setVal(STBorder.NONE);
                            tcBorders.addNewRight().setVal(STBorder.NONE);
                            tcBorders.addNewTop().setVal(STBorder.NONE);
                        } else {
                            CTTcPr tcPr = wordCell.getCTTc().addNewTcPr();
                            CTTcBorders tcBorders = tcPr.addNewTcBorders();
                            JSONObject border = styleJson.getJSONObject("border");
                            if (!border.containsKey("bottom")) {
                                tcBorders.addNewBottom().setVal(STBorder.NONE);
                            }
                            if (!border.containsKey("right")) {
                                tcBorders.addNewRight().setVal(STBorder.NONE);
                            }
                            if (!border.containsKey("top")) {
                                tcBorders.addNewTop().setVal(STBorder.NONE);
                            }
                            if (!border.containsKey("left")) {
                                tcBorders.addNewLeft().setVal(STBorder.NONE);
                            }
                        }
                        if (styleJson.containsKey("align")) {
                            String align = styleJson.getStr("align");
                            if ("center".equals(align)) {
                                wordCell.getParagraph(ctP).setAlignment(ParagraphAlignment.CENTER);
                            } else if ("left".equals(align)) {
                                wordCell.getParagraph(ctP).setAlignment(ParagraphAlignment.LEFT);
                            } else if ("right".equals(align)) {
                                wordCell.getParagraph(ctP).setAlignment(ParagraphAlignment.RIGHT);
                            }
                        }
                        if (styleJson.containsKey("font")) {
                            JSONObject font = styleJson.getJSONObject("font");
                            if (font.containsKey("bold")) {
                                run.setBold(true);
                            }
                            if (font.containsKey("size")) {
                                Short fontSize = font.getShort("size");
                                run.setFontSize(fontSize);
                            }
                            if (font.containsKey("name")) {
                                run.setFontFamily(font.getStr("name"));
                            }
                        }

                        if (styleJson.containsKey("color")) {
                            String bgcolor = styleJson.getStr("color");
                            run.setColor(bgcolor.substring(1));
                        }

                        if (styleJson.containsKey("bgcolor")) {
                            String bgcolor = styleJson.getStr("bgcolor");
                            wordCell.getCTTc().addNewTcPr().addNewShd().setFill(bgcolor.substring(1));
                        }

                    }
                } else {
                    CTTcPr tcPr = wordCell.getCTTc().addNewTcPr();
                    CTTcBorders tcBorders = tcPr.addNewTcBorders();
                    tcBorders.addNewBottom().setVal(STBorder.NONE);
                    tcBorders.addNewLeft().setVal(STBorder.NONE);
                    tcBorders.addNewRight().setVal(STBorder.NONE);
                    tcBorders.addNewTop().setVal(STBorder.NONE);
                }

                if (cell.getText() == null) {
                    run.setText("");
                } else {
                    run.setText(cell.getText().toString());

                }

            }

            for (Merge merge : columnMerge) {
                mergeCellsVertically(table, merge.getColumn(), merge.getRow(),
                        merge.getOffset() + merge.getRow());
            }
            for (Merge merge : rowMerge) {
                mergeCellsHorizontal(table, merge.getRow(), merge.getColumn(),
                        merge.getOffset() + merge.getColumn());

            }
            flag.getAndSet(true);
            InterruptedUtils.checkInterrupted("导出Word数据时中断");
            try (OutputStream out = outputStream) {
                wordDoc.write(outputStream);
                out.flush();
            }

        } catch (Exception e) {
            throw e;
        }

    }

    /**
     * @Description: 跨列合并
     */
    public void mergeCellsHorizontal(XWPFTable table, int row, int fromCell, int toCell) {
        for (int cellIndex = fromCell; cellIndex <= toCell; cellIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(cellIndex);
            if (cellIndex == fromCell) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /**
     * @Description: 跨行合并
     * @see http://stackoverflow.com/questions/24907541/row-span-with-xwpftable
     */
    public void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            if (rowIndex == fromRow) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

}
