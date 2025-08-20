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
package com.yunjian.reportbiz.preview;

/**
 * @author yujian
 **/
public enum CellExpandEnum {
    /**
     * 纵向
     */
    VERTICAL(1),
    /**
     * 横向
     */
    HORIZONTAL(2);

    final Integer type;

    CellExpandEnum(Integer type) {
        this.type = type;
    }

    public static CellExpandEnum of(Integer type) {
        for (CellExpandEnum value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }
}
