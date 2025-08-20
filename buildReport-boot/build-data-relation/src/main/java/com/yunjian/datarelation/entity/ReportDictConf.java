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

import com.baomidou.mybatisplus.annotation.*;
import com.yunjian.common.base.CommonColumnEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yujian
 **/
@TableName(value = "report2_dict_conf")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportDictConf extends CommonColumnEntity<ReportDictConf> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @TableField("id")
    private Long id;

    /**
     * 字典名称
     */
    private String dictName;
    /**
     * 字典所在数据源
     */
    private Long datasourceId;
    /**
     * 字典表名称
     */
    private String tableName;
    /**
     * 真实值字段
     */
    private String dictValueField;
    /**
     * 显示值字段
     */
    private String dictLabelField;
    /**
     * 默认展示
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String defaultLabel;

}
