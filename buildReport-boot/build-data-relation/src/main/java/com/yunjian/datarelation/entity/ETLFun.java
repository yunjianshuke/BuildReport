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
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created on 2024/1/2 18:00
 *
 * @author fusheng
 * @version 1.0
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@TableName("report2_etl_fun")
public class ETLFun extends CommonColumnEntity<ETLFun> {

    private Long id;
    /**
     * 函数名称
     */
    private String funName;
    /**
     * 函数代码
     */
    private String funCode;
    /**
     * 分组描述
     */
    private String funDescription;
    /**
     * 分组启用标识,1启用，2禁用
     */
    private int funEnable;
    /**
     * 函数所属组ID
     */
    private Long groupId;
    /**
     * 介绍
     */
    private String introduce;
    /**
     * 用法
     */
    private String instruct;
    /**
     * 示例
     */
    private String example;

    /**
     * 排序
     */
    private Integer sort;


}
