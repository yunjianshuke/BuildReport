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


/**
 * @author 201410135010
 */
public enum ErrorCodeEnum {

    SERVER_ERROR("1", "服务器繁忙,请稍后再试！"),
    OPERATION_ERROR("2", "参数错误！"),
    LOGIN_FAILURE("100001", "用户名或密码错误"),
    TIMEOUT("100002", "超时"),
    UPHONE_DUPLICATE("100005", "用户手机号码已存在"),
    CRM_PHONE_NOT_EXIST("100006", "预留手机号码不存在"),
    UPHONE_NOT_EXIST("100007", "用户手机号码不存在"),
    SIGN_WRONG("100008", "报文签名错误"),
    AUTH_FAILURE("100009", "登录超时或没有登录"),
    EMPTY_PRAMETER("100010", "参数为空"),
    UNPASS_MESSCHANGE("100011", "客户变更未通过"),
    SEND_CODE_FAIL("100012", "发送验证码失败"),
    GET_CER_FAIL("100013", "数字证书获取失败"),
    LOGOUT_FAILURE("100013", "注销失败"),
    UPHONE_ISBIND("100015", "绑定失败,用户手机号已被绑定"),
    OLD_PWD_ERROR("100016", "旧密码输入错误"),
    OLD_PWD_ERR_OVER("100033", "已超过每日错误次数上限，请明天再试试吧"),
    RE_PWD_ERROR("100034", "新密码两次输入不一致"),

    SIGN_CON_AGR_ERROR("100039", "已经签订用户保密协议！"),
    BANKCODEERROR("100025", "银行卡类型不支持"),
    //验证码相关错误
    VERIFY_CODE_ERROR("100003", "验证码错误"),
    VERIFY_CODE_FAILED("100004", "验证码已失效"),
    VERIFY_CODE_SEND_FAILURE("400001", "验证码发送失败"),
    VERIFY_CODE_SEND_OVER("400002", "已超过每日申请次数上限，请明天再试试吧。"),
    VERIFY_CODE_CHECK_OVER("400003", "已超过错误次数上限，请稍后重新发送验证码"),
    VERIFY_CODE_ESCAPE_ERROR("400004", "未进行验证码校验操作"),
    VERIFY_CODE_SEND_INTERVAL("400005", "验证码申请时间间隔过短(间隔时间1分钟)。"),

    APPNT_PHONE_ERROR("010000", "手机号码格式错误。"),

    IMG_TYPR_ERROR("100034", "上传图片格式不正确"),
    IMG_SIZE_ERROR("100035", "请上传文件大小不超过2M的图片"),

    UPDATE_PARAM_ERROR("1", "更新失败，参数错误"),
    UPDATE_DATA_ERROR("1", "更新失败，更新数据不存在或已删除"),
    DELETE_PARAM_ERROR("1", "删除失败，参数错误"),
    UPDATE_HOLD_PARAM_ERROR("1", "暂存更新失败，参数错误"),
    UPDATE_HOLD_DATA_ERROR("1", "暂存更新失败，更新数据不存在或已删除"),
    UNIQUE_PARAM_ERROR("1", "缺少唯一条件"),
    CODE_UNIQUE_PARAM_ERROR("1", "编码已经存在"),
    DATA_UNIQUE_PARAM_ERROR("1", "数据已经存在"),
    LOW_CODE_SQL_RELATIONS_ERROR("1", "sql关系不能为空 请修改"),
    LOW_CODE_NO_TABLE_ERROR("1", "请检查sql！表可能不存在..."),
    LOW_CODE_DATA_AUTHORITY_ERROR("1", "列表开启数据权限错误！请修改sql并添加相应字段"),

    USER_INFORMATION_EMPTY_ERROR("1", "供应商用户信息为空"),
    REGISTER_PHONE_EXIST_ERROR("1", "注册手机号已存在"),
    PHONE_CODE_ERROR("1", "验证码错误,请重新填写"),
    CODES_FREQUENTLY_SENT_ERROR("1", "验证码发送过频繁"),
    SEND_CODE_TYPE_ERROR("1", "发送验证码类型错误"),
    REGISTER_PHONE_IS_NOT_EXIST_ERROR("1", "手机号未注册");
    public String errCode;
    public String errMsg;

    private ErrorCodeEnum(String errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public static ErrorCodeEnum getInstance(String errCode) {
        for (ErrorCodeEnum ece : values()) {
            if (ece.errCode == errCode)
                return ece;
        }
        return null;
    }

    public String getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

}
