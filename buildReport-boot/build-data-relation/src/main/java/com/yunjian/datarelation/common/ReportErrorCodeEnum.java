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
package com.yunjian.datarelation.common;

import lombok.Getter;

/**
 * @author yujian
 **/
@Getter
public enum ReportErrorCodeEnum {
    /**
     * 分享链接已失效
     */
    SHARE_URL_VALID("4001", "分享连接不存在或已过期");

    private final String code;
    private final String message;

    ReportErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
