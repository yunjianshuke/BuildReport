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

import com.yunjian.common.exception.BusinessException;
import lombok.Getter;


/**
 * Created on 2024/7/22 下午8:10
 *
 * @author fusheng
 * @version 1.0
 */
@Getter
public enum CellComparatorEnum {


    DYNAMIC_DATE("DYNAMIC_DATE", "动态时间范围"),


    /**
     * 等于
     */
    EQUAL("EQ", "等于"),
    /**
     * 不等于
     */
    NOT_EQUAL("NE", "不等于"),
    /**
     * 大于
     */
    GREATER_THAN("GT", "大于"),
    /**
     * 大于等于
     */
    GREATER_THAN_OR_EQUAL("GE", "大于等于"),
    /**
     * 小于
     */
    LESS_THAN("LT", "小于"),
    /**
     * 小于等于
     */
    LESS_THAN_OR_EQUAL("LE", "小于等于"),
    /**
     * 包含
     */
    LIKE("LIKE", "包含"),
    /**
     * 不包含
     */
    NOT_LIKE("NOT_LIKE", "不包含"),
    /**
     * 为空
     */
    IS_NULL("NU", "为空"),
    /**
     * 不为空
     */
    NOT_NULL("NN", "不为空"),
    /**
     * 列表内
     */
    IN("IN", "列表内"),
    /**
     * 列表外
     */
    NOT_IN("NI", "列表外"),
    /**
     * 范围
     */
    BETWEEN("BETWEEN", "范围"),
    /**
     * 等于年
     */
    YEAR("YEAR", "等于年"),
    /**
     * 等于月
     */
    MONTH("MONTH", "等于月"),
    /**
     * 等于年月日
     */
    DAY("DAY", "等于年月日"),
    ;
    /**
     * 符号
     */
    private final String code;
    /**
     * 描述
     */
    private final String desc;

    CellComparatorEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CellComparatorEnum of(String code) {
        for (CellComparatorEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new BusinessException("0", "不支持的筛选方式" + code);
    }

}
