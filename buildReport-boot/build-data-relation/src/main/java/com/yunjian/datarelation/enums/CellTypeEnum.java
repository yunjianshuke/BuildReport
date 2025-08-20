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
package com.yunjian.datarelation.enums;

import lombok.Getter;

/**
 * @author yujian
 **/
@Getter
public enum CellTypeEnum {
    /**
     * 魔法单元格 只为了延续样式
     */
    MAGIC(-1),
    /**
     * 静态
     */
    STATIC(0),
    /**
     * 分组
     */
    GROUP(1),
    /**
     * 列表
     */
    LIST(2),
    /**
     * 图片
     */
    IMAGE(5),
    /**
     * 表达式
     */
    EXPRESSION(6),
    /**
     * 序号生成器
     */
    SEQUENCE(7);

    private Integer type;

    CellTypeEnum(Integer type) {
        this.type = type;
    }

    public static CellTypeEnum of(Integer type) {
        for (CellTypeEnum value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return MAGIC;
    }
}
