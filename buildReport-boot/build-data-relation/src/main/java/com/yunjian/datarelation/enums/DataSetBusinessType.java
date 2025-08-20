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
 * Created on 2024/8/21 上午10:17
 *
 * @author fusheng
 * @version 1.0
 */
@Getter
public enum DataSetBusinessType {
    DESIGN(0, "设计器"),

    FILTER(1, "筛选器"),
    ;

    private final Integer code;
    private final String name;


    DataSetBusinessType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static DataSetBusinessType getEnumByCode(Integer code) {
        for (DataSetBusinessType value : DataSetBusinessType.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

}
