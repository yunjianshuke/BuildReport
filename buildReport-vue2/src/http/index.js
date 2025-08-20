'use strict'

import axios from 'axios'

import interceptors from './interceptors'

function createAxiosInstance() {
    const instance = axios.create({
        baseURL: process.env.VUE_APP_API_BASE_URL,
        timeout: 15000,
        withCredentials: false
    })
    instance.interceptors.request.use(interceptors.reqFunc, interceptors.reqErr)
    instance.interceptors.response.use(interceptors.resFunc, interceptors.resErr)
    return instance
}

export const $ajax = createAxiosInstance()

export default {
    install(Vue) {
        Vue.prototype.$ajax = createAxiosInstance()
    }
}
