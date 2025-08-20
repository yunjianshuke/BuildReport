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
package com.yunjian.reportbiz.preview.wrappers;

import cn.hutool.core.util.StrUtil;
import com.yunjian.datarelation.dto.ReportCellDTO;
import com.yunjian.datarelation.enums.CellTypeEnum;
import com.yunjian.reportbiz.preview.CellExpandEnum;
import com.yunjian.reportbiz.preview.ReportRender;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.function.Consumer4;

/**
 * @author yujian
 **/
@Data
@Slf4j
public class CellWrapper implements Cloneable {

    static Map<String, DateTimeFormatter> cacheFormatter = new HashMap<>();
    /**
     * 用于计算当前是第几个元素
     */
    private int index;
    private Integer number;

    /**
     * 是否是最上面的一行
     */
    private boolean hasTop;
    /**
     * 是否是最左一列
     */
    private boolean hasLeft;
    /**
     * 原始行
     */
    private Integer row;
    /**
     * 原始列
     */
    private Integer column;
    /**
     * 方向
     */
    private CellExpandEnum expand;
    /**
     * 是否是静态单元格
     */
    private CellTypeEnum cellType;
    /**
     * 左侧原始单元格
     */
    private CellWrapper leftCell;
    /**
     * 上面原始单元格
     */
    private CellWrapper topCell;
    /**
     * 原始JSON数据
     */
    private ReportCellDTO origin;

    /**
     * 分组后剩余的数量
     */
    private Integer groupSize;
    /**
     * 所需数据
     */
    private List<Object> data;
    /**
     * 下钻数据
     */
    private List<List<Object>> linkData;

    private List<Object> parentData;


    private List<Integer> groupLen;

    private String formatTemplate;

    private ReportRender reportRender;

    private Integer groupFunIndex;
    private Boolean groupTotal;

    /**
     * 是不是分组的根节点
     *
     * @return boolean
     */
    public boolean isGroupRoot() {
        return topCell == null && leftCell == null;
    }


    public boolean isStaticAndHasParent() {
        return this instanceof StaticCellWrapper && (leftCell != null || topCell != null);
    }

    public boolean isFormatCell() {
        return StrUtil.isNotBlank(formatTemplate);
    }

    public boolean isGroupSum() {
        return groupTotal != null && groupTotal;
    }

    public boolean isGroupSumFun() {
        return groupTotal != null && groupTotal;
    }

    public Object formatDate(Object value) {

        DateTimeFormatter formatter = cacheFormatter.get(formatTemplate);

        try {
            if (value instanceof Number) {
                Number numberValue = (Number) value;
                int length = numberValue.toString().length();
                if (length == 10 || length == 13) {
                    // 是时间戳可以被格式化
                    Instant instant = Instant.ofEpochMilli(numberValue.longValue());
                    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    return dateTime.format(getFormatter(formatter, formatTemplate));
                }
            }
            if (value instanceof LocalDate) {
                LocalDate dateValue = (LocalDate) value;
                return dateValue.format(getFormatter(formatter, formatTemplate));
            }
            if (value instanceof LocalDateTime) {
                LocalDateTime localDateValue = (LocalDateTime) value;
                return localDateValue.format(getFormatter(formatter, formatTemplate));
            }
            if (value instanceof Date) {
                Date oldVersionDate = (Date) value;
                long epochMilli = oldVersionDate.getTime();
                Instant instant = Instant.ofEpochMilli(epochMilli);
                LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                return dateTime.format(getFormatter(formatter, formatTemplate));
            }
        } catch (Exception e) {
            log.error("格式化失败:", e);
        }
        return value;

    }

    public DateTimeFormatter getFormatter(DateTimeFormatter formatter, String formatTemplate) {
        if (formatter == null) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatTemplate);
            cacheFormatter.put(formatTemplate, dateTimeFormatter);
            return dateTimeFormatter;
        }
        return formatter;
    }

    @Override
    public CellWrapper clone() {
        try {
            CellWrapper clone = (CellWrapper) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
