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
import com.yunjian.common.base.CommonColumnEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 业务关系分组表
 *
 * @TableName report2_business_group
 */
@TableName(value = "report2_business_group")
@Data
public class ReportBusinessGroup extends CommonColumnEntity<ReportBusinessGroup> implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务分组名
     */
    private String bizGroupName;

    /**
     * 排序字段
     */
    private Integer seq;

    /**
     * 父ID
     */
    private Long parentId;


    /**
     * 组编号
     */
    private String bizGroupCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
