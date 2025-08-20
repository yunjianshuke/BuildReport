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

import com.alibaba.excel.write.handler.WorkbookWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.alibaba.excel.write.handler.context.WorkbookWriteHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 延迟合并策略：在文件完全写入后再添加合并区域，提升性能
 */
@Slf4j
public class MergeHandler implements WorkbookWriteHandler {

    private final List<CellRangeAddress> cellRangeAddressList;

    public MergeHandler(List<CellRangeAddress> cellRangeAddressList) {
        this.cellRangeAddressList = cellRangeAddressList;
    }

    /**
     * 在工作簿写入完成后执行合并操作（核心逻辑）
     * 此时所有数据已写入，添加合并区域不会干扰流式写入流程
     */
    @Override
    public void afterWorkbookDispose(WorkbookWriteHandlerContext context) {
        // 获取当前Sheet对象（默认取第一个sheet，可根据实际需求调整）
        WriteSheetHolder sheetHolder = context.getWriteContext().writeSheetHolder();
        Sheet sheet = sheetHolder.getSheet();

        // 1. 过滤有效合并区域（去重、排除无效区域）
        List<CellRangeAddress> validRegions = filterValidRegions(sheet);

        // 2. 批量添加合并区域到Sheet
        addMergedRegions(sheet, validRegions);
    }

    /**
     * 过滤无效的合并区域（去重+排除与现有区域重叠的区域）
     */
    private List<CellRangeAddress> filterValidRegions(Sheet sheet) {
        // 存储现有合并区域的唯一标识，用于快速判断重叠
        Set<String> existingRegionHashes = new HashSet<>();
        for (CellRangeAddress existing : sheet.getMergedRegions()) {
            existingRegionHashes.add(regionToString(existing));
        }

        // 存储有效区域（去重+不重叠）
        List<CellRangeAddress> validRegions = new ArrayList<>(cellRangeAddressList.size());
        Set<String> addedRegionHashes = new HashSet<>();

        for (CellRangeAddress region : cellRangeAddressList) {
            // 跳过无效区域（行/列范围不合法）
            if (!isValidRegion(region)) {
                continue;
            }

            // 生成区域的唯一标识（用于快速去重）
            String regionHash = regionToString(region);

            // 过滤重复区域和与现有区域重叠的区域
            if (!existingRegionHashes.contains(regionHash) && !addedRegionHashes.contains(regionHash)) {
                validRegions.add(region);
                addedRegionHashes.add(regionHash);
            }
        }

        return validRegions;
    }

    /**
     * 批量添加合并区域到Sheet
     */
    private void addMergedRegions(Sheet sheet, List<CellRangeAddress> regions) {
        for (CellRangeAddress region : regions) {
            sheet.addMergedRegionUnsafe(region);
        }
    }

    /**
     * 校验区域是否有效（行和列范围合法）
     */
    private boolean isValidRegion(CellRangeAddress region) {
        return region.getFirstRow() <= region.getLastRow()
                && region.getFirstColumn() <= region.getLastColumn()
                && region.getFirstRow() >= 0
                && region.getFirstColumn() >= 0;
    }

    /**
     * 将区域转为字符串唯一标识（用于快速去重和比较）
     * 格式：firstRow-lastRow-firstCol-lastCol
     */
    private String regionToString(CellRangeAddress region) {
        return String.format("%d-%d-%d-%d",
                region.getFirstRow(), region.getLastRow(),
                region.getFirstColumn(), region.getLastColumn());
    }

    @Override
    public void afterWorkbookCreate(WriteWorkbookHolder writeWorkbookHolder) {
    }

}
