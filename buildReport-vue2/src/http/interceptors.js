import { Message, MessageBox } from 'element-ui'
import Vue from 'vue'
const vm = new Vue()

const errorCode = {
    '400': '请求错误',
    '401': '请求需要身份验证',
    '402': '未知错误',
    '403': '当前操作没有权限',
    '404': '服务器找不到请求的网页',
    '405': '请求中指定的方法被禁用',
    '406': '无法使用请求的内容特性响应请求的网页',
    '408': '服务器等候请求时发生超时',
    '500': '服务器遇到错误',
    '426': '用户不存在或者密码错误',
    '501': '服务器不具备完成请求的功能',
    '502': '服务器网关错误',
    '503': '服务器目前无法使用',
    '504': '服务器网关超时',
    'default': '系统未知错误,请反馈给管理员'
}

function requestCallback() {
    return config => {
        const isToken = (config.headers || {}).isToken === false
        const token = 'xxxxxxxxxxx'
        if (token && !isToken) {
            config.headers['Authorization'] = 'Bearer ' + token// token
        }
        config.headers['client-source'] = 'PC'
        return config
    }
}

function responseCallback() {
    return res => {
        if (window.$b_Loading_Instance && res.headers && res.config.headers.isLoading === true) {
            window.$b_requestCount--
            if (window.$b_requestCount === 0) {
                window.$b_Loading_Instance.close()
                window.$b_Loading_Instance = null
            }
        }

        const businessCode = Object.prototype.hasOwnProperty.call(res.data, 'businessCode') ? res.data['businessCode'] || '' : ''
        if (res.data && Object.prototype.hasOwnProperty.call(res.data, 'code') && res.data.code !== 0) {
            const message = errorCode[businessCode] || res.data.msg || res.data.message || errorCode['default']
            if (vm.$messageBox) {
                vm.$messageBox.addErrorMessage({
                    title: '接口错误',
                    content: message,
                    read: false,
                    type: 'message'
                })
            } else {
                // Message.error(message)
                // 有的提示信息需要换行， 需要解析成html格式，对普通提示没有影响
                vm.$message({
                    dangerouslyUseHTMLString: true,
                    message: message,
                    type: 'error'
                })
            }
        }
        if (res.headers && res.headers['content-disposition']) {
            const disposition = res.headers['content-disposition']
            res.headers.fileName = disposition.slice(disposition.indexOf('filename=') + 9)
            return res
        }
        
        return res.data
    }
}

function requestError() {
    return err => {
        return Promise.reject(new Error(err))
    }
}

function responseError() {
    return err => {
        if (window.$b_Loading_Instance && err && err.config && err.config.headers && err.config.headers.isLoading === true) {
            window.$b_requestCount--
            if (window.$b_requestCount === 0 && window.$b_Loading_Instance) {
                window.$b_Loading_Instance.close()
                window.$b_Loading_Instance = null
            }
        }
        
        // 后台定义 424 针对令牌过去的特殊响应码
        if (err.response && err.response.status && err.response.status === 424) {
            MessageBox.confirm('令牌状态已过期，请点击重新登录', '系统提示', {
                confirmButtonText: '重新登录',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                // 刷新登录页面，避免多次弹框
                window.location.reload()
            }).catch(() => {})
            return
        }
        const status = err.response && Object.prototype.hasOwnProperty.call(err.response, 'status') ? err.response['status'] || '' : ''
        const message = errorCode[status] || errorCode['default']
        if (vm.$messageBox) {
            vm.$messageBox.addErrorMessage({
                title: '接口错误',
                content: message,
                read: false,
                type: 'message'
            })
        } else {
            Message.error(message)
        }
        return Promise.reject(new Error(err))
    }
}
window.interceptors = {
    reqFunc: requestCallback(),
    resFunc: responseCallback(),
    reqErr: requestError(),
    resErr: responseError()
}
export default {
    reqFunc: requestCallback(),
    resFunc: responseCallback(),
    reqErr: requestError(),
    resErr: responseError()
}
