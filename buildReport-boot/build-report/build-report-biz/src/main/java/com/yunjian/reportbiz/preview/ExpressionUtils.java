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
package com.yunjian.reportbiz.preview;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONNull;
import com.yunjian.datarelation.dto.ReportCellDTO;
import com.yunjian.datarelation.dto.Variable;
import com.yunjian.datarelation.jexl.AdvancedConversionEngine;
import com.yunjian.reportbiz.preview.wrappers.CellWrapper;
import com.yunjian.reportbiz.preview.wrappers.GroupCellWrapper;
import com.yunjian.reportbiz.preview.wrappers.ListCellWrapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yujian
 * @description
 * @create 2024-10-09 10:10
 **/
public class ExpressionUtils {

    public static Object parser(CellWrapper cellWrapper, ReportRender reportRender) {
        if (Boolean.TRUE.equals(cellWrapper.getOrigin().getExpression())) {

            String expression = cellWrapper.getOrigin().getExpressionText();
            if (StrUtil.isBlank(expression)) {
                return "";
            }

            AdvancedConversionEngine advancedConversionEngine = SpringUtil.getBean(
                    AdvancedConversionEngine.class);
            // 抽取所有参数
            Set<List<String>> variables = advancedConversionEngine.getVariables(expression);

            // 将参数转为01坐标
            List<Variable> xys = variables.stream()
                    .filter(vars -> !NumberUtil.isNumber(CollUtil.getFirst(vars)))
                    .map(vars -> {
                        String key = CollUtil.getFirst(vars);
                        List<Integer> rowColumn = ReportRender.expr2xy(key.toUpperCase());
                        if (rowColumn == null) {
                            return null;
                        }
                        return new Variable(key, CollUtil.getLast(rowColumn), CollUtil.getFirst(rowColumn));
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Map<String, Object> tempalteParamMap = new HashMap<>(xys.size());

            for (Variable xy : xys) {
                ReportCellDTO valueCell = reportRender.getByCellOfNewRows(cellWrapper.getRow(), xy.getColumn());
                if (valueCell != null) {
                    Object value = valueCell.getText();
                    if (value instanceof JSONNull) {
                        tempalteParamMap.put(xy.getKey(), null);
                    } else {
                        tempalteParamMap.put(xy.getKey(), safeConvert(value));
                    }
                } else {
                    tempalteParamMap.put(xy.getKey(), "");
                }
            }

            String convert = advancedConversionEngine.convert(tempalteParamMap,
                    cellWrapper.getOrigin().getExpressionText());

            cellWrapper.getOrigin().setText(convert);
            return convert;
        }
        return null;
    }

    public static Object parserListAndGroup(CellWrapper cellWrapper, ReportRender reportRender, Integer index) {
        if (Boolean.TRUE.equals(cellWrapper.getOrigin().getExpression())) {

            String expression = cellWrapper.getOrigin().getExpressionText();
            if (StrUtil.isBlank(expression)) {
                return "";
            }

            AdvancedConversionEngine advancedConversionEngine = SpringUtil.getBean(
                    AdvancedConversionEngine.class);
            // 抽取所有参数
            Set<List<String>> variables = advancedConversionEngine.getVariables(expression);
            String coordinate = cellWrapper.getOrigin().getCoordinate();

            // 将参数转为01坐标
            List<Variable> xys = variables.stream()
                    .filter(vars -> !NumberUtil.isNumber(CollUtil.getFirst(vars)))
                    .map(vars -> {
                        String key = CollUtil.getFirst(vars);
                        List<Integer> rowColumn = ReportRender.expr2xy(key.toUpperCase());
                        if (rowColumn == null) {
                            return null;
                        }
                        return new Variable(key, CollUtil.getLast(rowColumn), CollUtil.getFirst(rowColumn));
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Map<String, Object> tempalteParamMap = new HashMap<>(xys.size());

            for (Variable xy : xys) {
                if (xy.getKey().equals(coordinate)) {
                    String text = "";
                    if (cellWrapper instanceof GroupCellWrapper) {
                        GroupCellWrapper groupCellWrapper = (GroupCellWrapper) cellWrapper;
                        List<CellData> groupData = groupCellWrapper.getGroupData();
                        CellData cellData = groupData.get(index);
                        text = Convert.toStr(cellData.getValue());
                    } else if (cellWrapper instanceof ListCellWrapper) {
                        text = reportRender.getByCellOfNewRowsByDatasetNameMap(xy.getRow(), xy.getColumn(), index);
                    }
                    if (!StrUtil.isEmpty(text)) {
                        tempalteParamMap.put(xy.getKey(), safeConvert(text));
                    } else {
                        tempalteParamMap.put(xy.getKey(), "");
                    }
                } else {
                    ReportCellDTO valueCell = reportRender.getByCellOfNewRows(cellWrapper.getRow() + index, xy.getColumn());
                    if (valueCell != null) {
                        Object value = valueCell.getText();
                        if (value instanceof JSONNull) {
                            tempalteParamMap.put(xy.getKey(), null);
                        } else {
                            tempalteParamMap.put(xy.getKey(), safeConvert(value));
                        }
                    } else {
                        tempalteParamMap.put(xy.getKey(), "");
                    }
                }
            }

            String convert = advancedConversionEngine.convert(tempalteParamMap,
                    cellWrapper.getOrigin().getExpressionText());

            cellWrapper.getOrigin().setText(convert);
            return convert;
        }
        return null;
    }

    private static Object safeConvert(Object obj) {
        if (obj == null) {
            return "";
        }
        if (NumberUtil.isNumber(obj + "")) {
            return new BigDecimal(obj + "");
        }
        return obj;
    }
}
