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

import com.baomidou.mybatisplus.annotation.TableName;
import com.yunjian.common.base.CommonColumnEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 函数常用符号
 * @version: 1.0.0
 * @author: caolg
 * @date: 2024/01/05
 **/

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report2_etl_fun_symbol")
public class ReportEtlFunSymbol extends CommonColumnEntity<ReportEtlFunSymbol> {

    /*
     * id
     * */
    private Long id;
    /**
     * 符号名称
     */
    private String symbolName;
    /**
     * 符号代码
     */
    private String symbolCode;

    /**
     * 符号描述
     */
    private String symbolDescription;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 启用标识,1启用，2禁用
     */
    private Integer symbolEnable;

}
