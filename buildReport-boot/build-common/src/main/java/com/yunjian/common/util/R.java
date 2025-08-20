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
package com.yunjian.common.util;

import com.yunjian.common.constant.CommonConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 响应信息主体
 *
 * @param <T>
 * @author build
 */
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "响应信息主体")
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    @ApiModelProperty(value = "返回标记：成功标记=0，失败标记=1")
    private int code;

    @Getter
    @Setter
    @ApiModelProperty(value = "返回标记：自定义code")
    private String businessCode;

    @Getter
    @Setter
    @ApiModelProperty(value = "返回信息")
    private String msg;

    @Getter
    @Setter
    @ApiModelProperty(value = "数据")
    private T data;

    public static <T> R<T> ok() {
        return restResult(null, CommonConstants.SUCCESS, null, null);
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, CommonConstants.SUCCESS, null, null);
    }

    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, CommonConstants.SUCCESS, null, msg);
    }

    public static <T> R<T> ok(T data, String subCode, String msg) {
        return restResult(data, CommonConstants.SUCCESS, subCode, msg);
    }

    public static <T> R<T> failed() {
        return restResult(null, CommonConstants.FAIL, null, null);
    }

    public static <T> R<T> failed(String msg) {
        return restResult(null, CommonConstants.FAIL, null, msg);
    }

//	public static <T> R<T> failed(T data) {
//		return restResult(data, CommonConstants.FAIL,null, null);
//	}

//	public static <T> R<T> failed(T data, String msg) {
//		return restResult(data, CommonConstants.FAIL,null, msg);
//	}

    public static <T> R<T> failed(String subCode, String msg) {
        return restResult(null, CommonConstants.FAIL, subCode, msg);
    }

    private static <T> R<T> restResult(T data, int code, String businessCode, String msg) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setBusinessCode(businessCode);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public boolean isOk() {
        return this.code == CommonConstants.SUCCESS;
    }

}
