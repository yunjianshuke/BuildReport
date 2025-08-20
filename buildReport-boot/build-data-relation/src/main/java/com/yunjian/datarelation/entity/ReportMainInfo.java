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
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 报表主体信息表
 *
 * @TableName report_main_info
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "report2_main_info")
@Data
public class ReportMainInfo extends CommonColumnEntity<ReportMainInfo> implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long reportId;

    /**
     * 报表名称
     */
    private String name;

    /**
     * 报表组件
     */
    private String componentJson;
    /**
     * 条件查询
     */
    private String filterComponents;

    /**
     * 分组ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long groupId;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}
