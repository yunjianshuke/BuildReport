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
import net.sf.jsqlparser.expression.*;

/**
 * Created on 2024/7/22 下午8:24
 *
 * @author fusheng
 * @version 1.0
 */
@Getter
public enum DataTypeEnum {
    /**
     * 字符串
     */
    STRING("VARCHAR", "字符串"),
    TEXT("TEXT", "文本"),
    LONGTEXT("LONGTEXT", "文本"),
    BIGTEXT("BIGTEXT", "文本"),
    CHAR("CHAR", "字符"),
    /**
     * 数字
     */
    DECIMAL("DECIMAL", "数字"),
    /**
     * 长整型
     */
    LONG("BIGINT", "长整型"),
    INT("INT", "整数"),
    TINYINT("TINYINT", "小整数"),
    BIT("BIT", "位"),
    /**
     * 日期
     */
    DATE("date", "日期"),
    /**
     * 时间
     */
    TIME("time", "时间"),
    /**
     * 日期时间
     */
    DATETIME("datetime", "日期时间"),
    ;

    private String type;
    private String desc;

    DataTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static DataTypeEnum of(String type) {
        for (DataTypeEnum value : values()) {
            if (value.type.equalsIgnoreCase(type)) {
                return value;
            }
        }
        throw new BusinessException("0", "不支持的值类型" + type);
    }

    public Expression getTypeValue(Object value) {
        switch (this) {
            case TIME:
                return new TimeValue(String.valueOf(value));
            case DECIMAL:
                return new DoubleValue(String.valueOf(value));
            case INT:
            case BIT:
            case TINYINT:
            case LONG:
                return new LongValue(String.valueOf(value));
            default:
                return new StringValue(String.valueOf(value));
        }
    }
}
