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
package com.yunjian.datarelation.dto;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yunjian.datarelation.config.CustomIntegerDeserializer;
import com.yunjian.datarelation.config.CustomIntegerSerializer;

import java.util.List;

import lombok.Data;

/**
 * 报表单元格信息表
 *
 * @author yujian
 */
@Data
public class ReportCellDTO implements Cloneable {

    private int row;

    private int column;

    /**
     * 原始行
     */
    private Integer newRow;
    /**
     * 原始列
     */
    private Integer newColumn;

    private String coordinate;

    private ImageProp image;

    /**
     * 当前单元格所使用的字段
     */
    private String key;
    /**
     * 数据集名
     */
    private String dataset;

    /**
     * 单元格名称
     */
    private Object text;
    /**
     * 是否开启小计
     */
    private Boolean groupTotal;

    /**
     * 是否是表达式
     */
    @JsonSerialize(using = CustomIntegerSerializer.class)
    private Boolean expression;
    /**
     * 表达式内容
     */
    private String expressionText;

    /**
     * 单元格类型，0是静态，1是分组，2是列表
     */
    private Integer type;

    /**
     * 扩展方向,0是无，1是纵向，2是横向
     */
    private Integer expand;

    /**
     * 字段内容 上下文  [水平x类型(0 无,1 默认,2 自定义)）,垂直y类型（0 无,1 默认,2 自定义）]
     */
    private List<Integer> context;
    /**
     * 是否可编辑
     */
    @JsonDeserialize(using = CustomIntegerDeserializer.class)
    private Integer editable;

    /**
     * 水平上下文 水平父格坐标
     */
    private List<Integer> axisX;

    /**
     * 垂直上下文 垂直父格坐标
     */
    private List<Integer> axisY;
    /**
     * 合并单元格, 先行后列  [0:1]
     */
    private List<Integer> merge;
    /**
     * 样式
     */
    private String style;

    private Integer height;

    private String format;

    private LinkDataDTO link;

    // ----------------------------------------------  自定义参数   -----------------------------

    public void setMergeRow(int row, int column) {
        JSONArray mergeJson = JSONUtil.parseArray(merge);
        mergeJson.set(0, row);

        if (CollUtil.isEmpty(merge)) {
            mergeJson.set(1, column);

        }
        merge = mergeJson.toList(Integer.class);
    }

    public void setMergeColumn(int row, int column) {
        JSONArray mergeJson = JSONUtil.parseArray(merge);
        mergeJson.set(1, column);

        if (CollUtil.isEmpty(merge)) {
            mergeJson.set(0, row);
        }
        merge = mergeJson.toList(Integer.class);
    }

    @Override
    public ReportCellDTO clone() {
        try {
            ReportCellDTO clone = (ReportCellDTO) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
