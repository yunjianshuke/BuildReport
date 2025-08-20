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

import com.yunjian.datarelation.common.BaseOptions;
import com.yunjian.datarelation.common.BaseOptions.CreateGroup;
import com.yunjian.datarelation.entity.Datasource;
import com.yunjian.datarelation.enums.DatasouceTypeEnum;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author yujian
 **/

@Data
public class DatasourceDTO {

    private Long id;
    /**
     * 数据源名称
     */
    @NotBlank(groups = CreateGroup.class, message = "数据源名称不能为空")
    private String name;
    /**
     * 数据源类型
     */
    @NotNull(groups = CreateGroup.class, message = "数据库类型不能为空")
    private DatasouceTypeEnum type;
    /**
     * 可用状态
     */
    private Boolean status;
    /**
     * 数据源名称
     */
    @Valid
    private BaseOptions options;


    public Datasource convert() {
        Datasource ds = new Datasource();
        BeanUtils.copyProperties(this, ds);
        return ds;
    }
}
