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
import com.yunjian.datarelation.common.BaseOptions;
import com.yunjian.datarelation.common.DbJsonHandler;
import com.yunjian.datarelation.dto.DatasourceDTO;
import com.yunjian.datarelation.enums.DatasouceTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

/**
 * 数据源管理
 *
 * @author yujian
 **/
@Data
@TableName("report2_datasource")
@EqualsAndHashCode(callSuper = true)
public class Datasource extends CommonColumnEntity<Datasource> {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @TableField("id")
    private Long id;
    /**
     * 数据源名称
     */
    @TableField("name")
    private String name;
    /**
     * 数据源类型
     */
    @TableField("type")
    private DatasouceTypeEnum type;
    /**
     * 数据源配置
     */
    @TableField(value = "options", typeHandler = DbJsonHandler.class)
    private BaseOptions options;


    public DatasourceDTO convert() {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyProperties(this, datasourceDTO);
        return datasourceDTO;
    }
}
