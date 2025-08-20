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
package com.yunjian.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @description: 业务异常类
 * @author: zhc
 * @date: 2022/8/1$
 **/
@Getter
@Setter
@NoArgsConstructor
public class BusinessException extends RuntimeException {
    String code;
    String msg;

    public BusinessException(ErrorCodeEnum errEnum) {
        super();
        this.code = errEnum.getErrCode();
        this.msg = errEnum.getErrMsg();
    }

    public BusinessException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
