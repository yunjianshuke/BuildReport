<!-- eslint-disable no-debugger -->
<template>
    <div v-loading="loading" class="designer-preview">
        <div ref="designerV2Preview" class="designer-v2-preview" />
        <data-filter
            v-if="showDataFilter" key="designerV2Preview" :params="params" @close="onDataFilterClose"
            @search="handleSearch"
        />
        <!-- <el-pagination class="pageContainer" :background="false" :page-size="size" :current-page="page"
                       :page-sizes="[15, 30, 50, 100, 500]" :total="total" layout="sizes, prev, next" prev-text="上一页"
                       next-text="下一页" @size-change="handleSizeChange" @current-change="handleCurrentChange"
        /> -->
        <report-preview-dialog ref="reportPreviewDialog" />
        <div v-if="shareErrorInfo" class="share-error-info">{{ shareErrorInfo }}</div>
    </div>
</template>

<script>
import DataFilter from '../data-filter/index'
import Spreadsheet from '../x-spreadsheet/src'
import { cssPrefix } from '../x-spreadsheet/src/config'
import zhCn from '../x-spreadsheet/src/locale/zh-cn'
Spreadsheet.locale('zh-cn', zhCn)
import { PreviewSSE } from './sseMixin'

export default {
    name: 'ReportPreview',
    components: {
        DataFilter,
        ReportPreviewDialog: () => import('./dialog.vue')
    },
    props: {
        previewUrl: {
            type: String,
            default: '/reportInfo/preview/async'
        },
        previewUrlsync: {
            type: String,
            default: '/reportInfo/preview/sync'
        }
    },
    data() {
        return {
            page: 1,
            size: 15,
            total: 100,
            params: [],
            showDataFilter: false,
            loading: false,
            shareErrorInfo: ''
        }
    },
    beforeDestroy() {
        this.sheet && this.sheet.destroy()
        this.sheet = null
        this.previewIns && this.previewIns.stopSse() && (this.previewIns = null)
        this.printIns && this.printIns.stopSse() && (this.printIns = null)
        this.downloadIns && this.downloadIns.stopSse() && (this.downloadIns = null)
    },
    methods: {
        addSheetEventListeners() {
            const obliqueInput = this.$refs.designerV2Preview.querySelector(`.${cssPrefix}-oblique-input`)
            let currentRi = 0
            let currentCi = 0
            this.sheet.on('cell-selected', (cell, ri, ci) => {
                currentRi = ri
                currentCi = ci
                if (obliqueInput) {
                    obliqueInput.value = cell ? cell.text : ''
                }
            })
            this.sheet.on('change', () => {
                const cell = this.sheetData.getCell(currentRi, currentCi)
                if (obliqueInput) {
                    obliqueInput.value = cell ? cell.text : ''
                }
            })
            this.sheet.on('filter', () => {
                this.triggerFilter()
            })
            this.sheet.on('openLink', link => {
                this.$refs.reportPreviewDialog.open({ reportId: link.targetReport, condition: link.condition, source: 'manager' })
            })
            this.sheet.on('export', type => {
                this.export(type)
            })
            this.sheet.on('print', () => {
                this.onPrint()
            })
        },
        // 打开弹窗，每次初始化配置以应用配置更新
        open(data) {
            const that = this
            setTimeout(() => {
                if (!this.sheet) {
                    this.sheet = new Spreadsheet(this.$refs.designerV2Preview, {
                        mode: 'preview',
                        showBottomBar: false,
                        view: {
                            height: () => that.$refs.designerV2Preview.clientHeight,
                            width: () => that.$refs.designerV2Preview.clientWidth
                        }
                    })
                    this.sheetObj = this.sheet.sheet
                    this.addSheetEventListeners()
                }
                this.paramsData = data
                this.loadData()
            }, 0)
        },

        loadData() {
            // 同步
            if (this.paramsData.source === 'editPreview') {
                // 编辑窗口的预览
                this.syncPreviewEdit()
            } else {
                // 来自报表管理菜单
                this.syncPreview()
            }
            
        },
        // 编辑窗口的预览
        syncPreviewEdit() {
            this.loading = true
            const url = `${this.previewUrlsync}`
            const data = Object.assign({}, this.paramsData)
            if (data.filterComponents) {
                data.filterComponents = JSON.stringify(data.filterComponents)
            }
            if (this.params.length) {
                data.filterComponents = JSON.stringify(this.params)
            }
            this.previewIns = new PreviewSSE(url, 'preview', data)
            this.previewIns.setBusinessFun = res => {
                console.log('预览数据', res)
                if (res.code === 0) {
                    this.total = res.data.total
                    this.sheet.loadData(res.data.tableInfo)
                    this.sheetData = this.sheet.datas[0]
                    this.sheetObj.trigger('change')
                    if (this.params.length === 0 && res.data.filterComponents) {
                        this.params = typeof res.data.filterComponents === 'string' ? JSON.parse(res.data.filterComponents) : res.data.filterComponents
                        this.params.forEach(item => {
                            item.dateFormat = this.dateFormat(item.type, item.fields[0].condition)
                            item.dateType = this.dateType(item.type, item.fields[0].condition)
                            return item
                        })
                    }
                    this.params.filter(item => { return item.component.type }).length && !this.sheetObj.toolbar.filterEl.active() && this.sheetObj.toolbar.filterEl.click()
                    this.shareErrorInfo = ''
                } else if (res.businessCode === '4001') {
                    this.shareErrorInfo = res.msg
                }
                this.loading = false
            }
            this.previewIns.setStopCallBackFun = e => {
                this.handelStopCallBack(e)
            }
            this.previewIns.startSse()
        },
        // 执行同步SSE预览
        syncPreview() {
            console.log('执行同步SSE预览')
            if (!this.paramsData.reportId) return this.$message.error('缺少报表ID')
            this.loading = true
            let url = `${this.previewUrlsync}/${this.paramsData.reportId}`
            const data = Object.assign({}, this.paramsData)
            if (data.filterComponents) {
                data.filterComponents = JSON.stringify(data.filterComponents)
            }
            if (this.params.length) {
                data.filterComponents = JSON.stringify(this.params)
            }
            this.previewIns = new PreviewSSE(url, 'preview', data)
            this.previewIns.setBusinessFun = res => {
                console.log('预览数据', res)
                if (res.code === 0) {
                    this.total = res.data.total
                    this.sheet.loadData(res.data.tableInfo)
                    this.sheetData = this.sheet.datas[0]
                    this.sheetObj.trigger('change')
                    if (this.params.length === 0 && res.data.filterComponents) {
                        this.params = typeof res.data.filterComponents === 'string' ? JSON.parse(res.data.filterComponents) : res.data.filterComponents
                        this.params.forEach(item => {
                            item.dateFormat = this.dateFormat(item.type, item.fields[0].condition)
                            item.dateType = this.dateType(item.type, item.fields[0].condition)
                            return item
                        })
                    }
                    this.params.filter(item => { return item.component.type }).length && !this.sheetObj.toolbar.filterEl.active() && this.sheetObj.toolbar.filterEl.click()
                    this.shareErrorInfo = ''
                } else if (res.businessCode === '4001') {
                    this.shareErrorInfo = res.msg
                }
                this.loading = false
            }
            this.previewIns.setStopCallBackFun = e => {
                this.handelStopCallBack(e)
            }
            this.previewIns.startSse()
        },
        handleSizeChange(size) {
            this.size = size
            this.page = 1
            this.loadData()
        },
        handleCurrentChange(page) {
            this.page = page
            this.loadData()
        },
        handleSearch() {
            this.page = 1
            this.loadData()
        },
        triggerFilter() {
            this.showDataFilter = !this.showDataFilter
        },
        onDataFilterClose() {
            this.sheetObj.toolbar.filterEl.click()
        },
        dateFormat(type, condition) {
            let result = 'yyyy-MM-dd'
            if (type === 'DATETIME') {
                result = 'yyyy-MM-dd HH:mm:ss'
            }
            if (condition) {
                switch (condition) {
                    case 'YEAR':
                        result = 'yyyy'
                        break
                    case 'MONTH':
                        result = 'yyyy-MM'
                        break
                    case 'DAY':
                        result = 'yyyy-MM-dd'
                        break
                }
            }
            return result
        },
        dateType(type, condition) {
            let result = 'date'
            if (type === 'DATETIME') {
                result = 'datetime'
            }
            if (condition) {
                switch (condition) {
                    case 'YEAR':
                        result = 'year'
                        break
                    case 'MONTH':
                        result = 'month'
                        break
                    case 'DAY':
                        result = 'date'
                        break
                    case 'BETWEEN':
                        result = type === 'DATETIME' ? 'datetimerange' : 'daterange'
                        break
                }
            }
            return result
        },
        // 导出
        export(type) {
            // 报表设计都走同步
            this.syncExport(type)
        },
        // 执行同步SSE导出
        syncExport(type) {
            this.loading = true
            const data = Object.assign({}, this.paramsData)
            if (data.filterComponents) {
                data.filterComponents = JSON.stringify(data.filterComponents)
            }
            if (this.params.length) {
                data.filterComponents = JSON.stringify(this.params)
            }
            this.$ajax({
                url: `/reportInfo/download/${type}`,
                method: 'post',
                responseType: 'arraybuffer',
                data
            }).then(res => {
                this.download(res)
            }).finally(() => {
                this.loading = false
            })
        },
        // 打印
        onPrint() {
            // 报表设计都走同步
            this.syncPrint()
        },
        // 执行同步SSE打印
        syncPrint() {
            this.loading = true
            const data = Object.assign({ }, this.paramsData)
            if (data.filterComponents) {
                data.filterComponents = JSON.stringify(data.filterComponents)
            }
            if (this.params.length) {
                data.filterComponents = JSON.stringify(this.params)
            }
            this.$ajax({
                url: '/reportInfo/download/pdf',
                method: 'post',
                responseType: 'arraybuffer',
                data
            }).then(res => {
                this.print(res)
            }).finally(() => {
                this.loading = false
            })
        },
        print(response) {
            // 判断后端是否返回文件流，若是则下载文件
            const headers = response.headers
            if (headers && headers['content-type'] && (headers['content-type'].indexOf('application/pdf') > -1 || headers['content-type'].indexOf('application/octet-stream') > -1)) { // 判断返回文件流
                const url = window.URL.createObjectURL(new Blob([response.data], { 'type': 'application/pdf' }))
                const iframe = document.getElementById('print_preview')
                if (iframe === null) {
                    const iframe = document.createElement('iframe')
                    document.body.appendChild(iframe)
                    iframe.setAttribute('id', 'print_preview')
                    iframe.setAttribute('style', 'position:absolute;width:0px;height:0px;left:0px;top:0px;')
                    iframe.setAttribute('src', url)
                    iframe.onload = function() {
                        iframe.contentWindow.print()
                    }
                } else {
                    iframe.setAttribute('src', url)
                    iframe.onload = function() {
                        iframe.contentWindow.print()
                    }
                }
                return true
            }
            return false
        },

        download(response) {
            // 判断后端是否返回文件流，若是则下载文件
            const headers = response.headers
            if (headers && headers['content-type'] && headers['content-type'].indexOf('application/octet-stream') > -1) { // 判断返回文件流
                const blob = new Blob([response.data])
                const link = document.createElement('a')
                link.download = decodeURI(headers['content-disposition'].replace(/.*filename=/, ''))
                link.style.display = 'none'
                link.href = URL.createObjectURL(blob)
                document.body.appendChild(link)
                link.click()
                URL.revokeObjectURL(link.href)
                document.body.removeChild(link)
                return true
            }
            return false
        },
        handelStopCallBack(e) {
            let data
            data = JSON.parse(e.data)
            if (data?.code === 1) {
                this.$confirm('操作失败！', '提示', {
                    confirmButtonText: '知道了',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {})
                this.$emit('closeParent')
            } else {
                this.$confirm('响应时间过长已断开连接！请稍后重试！', '提示', {
                    confirmButtonText: '知道了',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {})
                this.$emit('closeParent')
            }
        }
    }
}
</script>
<style lang="scss" scoped>
.designer-preview {
    width: 100%;
    height: 100%;
    position: relative;
    flex-direction: column;
    display: flex;
    background-color: #FFF;
    z-index: 13;

    .designer-v2-preview {
        width: 100%;
        height: 0;
        flex: 1;

        ::v-deep .x-spreadsheet-toolbar {
            box-sizing: border-box;
        }

        ::v-deep *::-webkit-scrollbar {
            width: 13px;
            height: 13px;
        }
    }

    .pageContainer {
        margin: 2px 0;
        text-align: right;
    }

    .share-error-info {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #FFF;
        z-index: 14;
    }
}
</style>
