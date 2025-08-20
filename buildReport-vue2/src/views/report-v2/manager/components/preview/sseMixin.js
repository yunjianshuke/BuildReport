import interceptors from '@/http/interceptors'
import { SSE } from 'sse.js'
const apiPrefix = process.env.VUE_APP_API_BASE_URL

class BaseSSE {
    constructor(url, name, payload = '', retry = false) {
        this.url = apiPrefix + url
        this.name = name
        this.sseSource = null
        this.sseReconnectTimer = null,
        this.payload = payload
        this.retry = retry
        this.dataBuffer = '' // 数据缓冲区
        this.chunkedData = [] // 分块数据缓冲区数组

        // 绑定事件处理方法到当前实例
        this.handleSseOpen = this.handleSseOpen.bind(this)
        this.handleSseError = this.handleSseError.bind(this)
        this.handleSseAbort = this.handleSseAbort.bind(this)
        this.handleSseHeartbeat = this.handleSseHeartbeat.bind(this)
        this.handleSseMessage = this.handleSseMessage.bind(this)
    }

    get getInfo() {
        return {
            name: this.name,
            url: this.url
        }
    }
    set setBusinessFun(fn) {
        this.businessFun = fn
    }

    set setStopCallBackFun(fn) {
        this.stopCb = fn
    }

    startSse() {
        this.stopSse()
        let sseOptions = {
            headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            },
            method: 'post',
            payload: JSON.stringify(this.payload)
        }
        sseOptions = interceptors.reqFunc(sseOptions)
        this.sseSource = new SSE(this.url, sseOptions)
        this.addSseEventListeners()
        console.log(this.name + 'SSE 重连')
    }

    stopSse() {
        this.sseReconnectTimer && clearTimeout(this.sseReconnectTimer)
        this.sseSource && this.sseSource.close()
        this.sseSource && this.removeSseEventListeners()
        this.sseSource = null
    }

    removeSseEventListeners() {
        this.sseSource.removeEventListener('message', this.handleSseHeartbeat)
        this.sseSource.removeEventListener('error', this.handleSseError)
    }
    addSseEventListeners()  {
        this.sseSource.addEventListener('open', this.handleSseOpen, { once: true })
        this.sseSource.addEventListener('message', this.handleSseHeartbeat)
        this.sseSource.addEventListener('error', this.handleSseError)
        this.sseSource.addEventListener('abort', this.handleSseAbort, { once: true })
    }

    handleSseOpen() {
        console.log(this.name + 'SSE连接建立')
    }
    handleSseError(error)  {
        console.error(this.name + 'SSE错误', error)
        setTimeout(() => {
            this.stopSse()
            if (this.stopCb) this.stopCb(error)
        }, 6 * 1000)
    }
    handleSseAbort()  {
        console.log(this.name + 'SSE已经关闭')
        this.stopSse()
    }
    handleSseMessage(event)  {
        if (this.businessFun) this.businessFun(event)
    }
    // 接到sse发来的心跳，10秒收不到重连sse
    handleSseHeartbeat(event) {
        // console.log(this.name + '心跳', event)
    
        // 将收到的数据添加到缓冲区
        if (event.data && event.data !== 'finished') {
            this.dataBuffer += event.data
        }
        
        if (event.data === 'finished') {
            let data = null
            try {
                // 解析完整数据
                data = JSON.parse(this.dataBuffer.trim())
                this.dataBuffer = '' // 清空缓冲区
                // console.log('SSE数据解析成功', data)
            } catch (e) {
                console.error('SSE数据解析失败', e)
                // 解析失败清空缓冲区
                this.dataBuffer = ''
            }
            // 处理解析后的数据
            if (!data || data?.code !== 0) {
                if (this.retry) {
                    this.sseReconnectTimer && clearTimeout(this.sseReconnectTimer)
                    this.sseReconnectTimer = setTimeout(() => {
                        this.sseSource && this.sseSource.close()
                        this.startSse()
                    }, 10 * 1000)
                } else {
                    this.sseReconnectTimer && clearTimeout(this.sseReconnectTimer)
                    this.sseReconnectTimer = setTimeout(() => {
                        this.stopSse()
                        if (this.stopCb) this.stopCb(event)
                    }, 10 * 1000)
                }
            } else {
                this.handleSseMessage(data)
                this.stopSse()
            }
        }
        
    }
}

export class PreviewSSE extends BaseSSE {
    constructor(url, name, payload, retry = false) {
        super(url, name, payload, retry)
    }
}