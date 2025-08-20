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
package com.yunjian.datarelation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yunjian.common.base.CommonColumnEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据集表
 *
 * @TableName report_data_set
 */
@TableName(value = "report2_data_set")
@Data
public class ReportDataSet extends CommonColumnEntity<ReportDataSet> implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 报表id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long reportId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long relationId;
    /**
     * 数据集类型，0是数据集，1是单表
     */
    private Integer type;
    /**
     * 数据集类型。0是设计器，1是筛选器
     */
    private Integer businessType;


    /**
     * 数据集名称
     */
    private String name;

    private String dataFilter;

    /**
     * 数据集字段
     */
    private String fieldJsonArray;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
