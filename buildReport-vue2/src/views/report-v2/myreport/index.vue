<template>
    <AbsoluteContainer>
        <div class="grid-mod grid-mod-flex">
            <el-card class="fm-generate-card clearfix is-always-shadow" style="margin-bottom: 18px;">
                <el-form :model="queryParam" :inline="true">
                    <el-form-item label="报表名称">
                        <el-input v-model="queryParam.name" placeholder="请输入报表名称" size="small" clearable />
                    </el-form-item>
                    <el-form-item label="分组名称">
                        <el-cascader
                            v-model="queryParam.bizGroupIds"
                            size="small"
                            :options="datas"
                            :props="{ multiple: true, checkStrictly: true, emitPath: false, label: 'name', value: 'id' }"
                            placeholder="请选择分组"
                            :collapse-tags="true"
                            clearable
                        />
                    </el-form-item>
                    <el-form-item style="margin-left: 30px;">
                        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
                        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
                    </el-form-item>
                </el-form>
            </el-card>
            <div class="vxe-gird-box" style="display: flex; flex-direction: column;">
                <el-table
                    v-loading="loading" 
                    :data="reportList" 
                    :border="true" 
                    :stripe="true" 
                    row-key="field"
                    :highlight-current-row="true" 
                    style="width: 100%;" 
                    height="calc(100vh - 320px)"
                >
                    <el-table-column label="序号" type="index" align="center" width="80" />
                    <el-table-column prop="name" label="报表名称" align="center" min-width="240" />
                    <el-table-column prop="bizGroupName" label="分组名称" align="center" min-width="100" />
                    <el-table-column prop="updateTime" label="更新时间" align="center" min-width="150" />
                    <el-table-column label="操作" align="center" width="100" fixed="right">
                        <template #default="{ row }">
                            <operator-btn
                                :row="row"
                                :max-buttons="1"
                            >
                                <template v-for="o in operationBtns">
                                    <el-button
                                        v-if="o.visibleMethod ? o.visibleMethod(row) : true"
                                        :key="o.code"
                                        size="small"
                                        :icon="o.icon"
                                        :type="o.type"
                                        :disabled="o.disabledMethod ? o.disabledMethod(row) : false"
                                        @click="o.clickMethod(row)"
                                    >
                                        {{ o.name }}
                                    </el-button>
                                </template>
                            </operator-btn>
                        </template>
                    </el-table-column>
                </el-table>
                <div style="padding: 10px 0">
                    <el-pagination
                        :background="true" class="pageContainer" :page-size="size" :current-page="page"
                        :page-sizes="[2, 4, 10, 15, 20]" layout="total, sizes, prev, pager, next, jumper"
                        :total="total" @size-change="handleSizeChange" @current-change="handleCurrentChange"
                    />
                </div>
            </div>
            <report-preview-dialog ref="reportPreview" :preview-url="previewUrl" :title="title" :before-close="onBeforeClose" @close="closeLoad" />
        </div>
    </AbsoluteContainer>
</template>

<script>
import { batchRunETL } from '../api/etl'
import ReportPreviewDialog from '../manager/components/preview/dialogMy.vue'
import AbsoluteContainer from '@/components/AbsoluteContainer/index.vue'
import operatorBtn from '@/components/yjOperator/index.vue' // 有操作区域按钮组件渲染 customAction.length>0
export default {
    name: 'MyReport2',
    components: {
        AbsoluteContainer,
        ReportPreviewDialog,
        operatorBtn
    },
    data() {
        return {
            loading: false,
            reportList: [], // 报表列表
            datas: [],
            rowCurrent: {},
            queryParam: {
                name: '',
                bizGroupIds: []
            },
            rowData: {},
            formData: {},
            page: 1,
            size: 15,
            total: 0,
            etlIds: [],
            operationBtns: [  // 操作列按钮配置，disabledMethod和visibleMethod为可选配置。默认为false和true
                {
                    code: 'preview',
                    name: '查看',
                    type: 'text',
                    icon: 'el-icon-view',
                    clickMethod: this.handlePreview
                },
                {
                    code: 'exportWord',
                    name: '导出Word格式',
                    type: 'text',
                    icon: 'el-icon-download',
                    clickMethod: row => {
                        this.handleExport(row, 'word')
                    }
                },
                {
                    code: 'exportExcel',
                    name: '导出Excel格式',
                    type: 'text',
                    icon: 'el-icon-download',
                    clickMethod: row => {
                        this.handleExport(row, 'excel')
                    }
                },
                {
                    code: 'exportPDF',
                    name: '导出PDF格式',
                    type: 'text',
                    icon: 'el-icon-download',
                    clickMethod: row => {
                        this.handleExport(row, 'pdf')
                    }
                },
                {
                    code: 'print',
                    name: '打印',
                    type: 'text',
                    icon: 'el-icon-printer',
                    clickMethod: this.handlePrint
                }
            ],
            previewUrl: '',
            title: ''
        }
    },
    mounted() {
        this.getList()
        this.openShare()
        this.loadTree()
    },
    methods: {
        // 获取tree
        loadTree(data = {}) {
            this.$ajax({
                url: '/reportGroup/tree',
                method: 'post',
                data: {
                    ...data
                }
            }).then(data => {
                console.log('🚀 ~ file: index.vue:336 ~ loadTree ~ data:', data)
                if (data.code === 0 && data.data) {
                    const datas = data.data
                    this.datas = datas
                } else if (data.code === 0 && !data.data) {
                    this.datas = []
                }
            })
        },
        /**
         * 获取列表
         */
        getList() {
            const vm = this
            let requestParams = {
                size: vm.size,
                current: vm.page,
                ...this.queryParam
            }
            this.loading = true
            return vm.$ajax({
                url: '/reportInfo/auth/page',
                method: 'post',
                data: requestParams
            }).then(res => {
                if (res.code === 0 && res.data && res.data.total) {
                    vm.reportList = res.data.records
                    vm.total = res.data.total
                    return
                }
                vm.reportList = []
                vm.total = 0
            }).finally(() => {
                this.loading = false
            })
        },
        reLoad() {
            this.size = 15
            this.page = 1
            this.getList()
        },
        closeLoad() {
            this.loading = false
        },
        /**
         * 查看
         */
        handlePreview(row) {
            this.etlIds = row.etlIds
          
            this.previewUrl = '/reportInfo/preview/actual/sync'
            this.title = row.name
            if (Array.isArray(row.etlIds) && row.etlIds.length) {
                if (row.hasJob) {
                    this.$refs.reportPreview.open({ reportId: row.reportId })
                } else {
                    const loadingInstance = this.$loading({
                        lock: true,
                        text: '报表生成中...',
                        spinner: 'el-icon-loading',
                        background: 'rgba(0, 0, 0, 0.7)'
                    })
                    batchRunETL({ etlIds: row.etlIds }).then(data => {
                        if (data.code === 0) {
                            this.$refs.reportPreview.open({ reportId: row.reportId })
                        }
                    }).finally(() => {
                        loadingInstance.close()
                    })
                }
            } else {
                this.$refs.reportPreview.open({ reportId: row.reportId })
            }
            
        },
        // 导出
        handleExport(row, type) {
            // / 同步
            this.syncExport(row, type)
        },
        // 执行同步SSE导出
        syncExport(row, type) {
            this.loading = true
            const data = Object.assign({}, {reportId: row.reportId})
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
        handlePrint(row) {
            // 同步
            this.syncPrint(row)
        },
        // 执行同步SSE打印
        syncPrint(row) {
            this.loading = true
            const data = Object.assign({}, {reportId: row.reportId})
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
            console.log('response', response)
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
        // 重置
        handleReset() {
            this.queryParam = {
                name: '',
                bizGroupIds: []
            }
            this.formData = {}
            this.reLoad()
        },
        // 搜搜
        handleSearch() {
            this.formData = { ...this.queryParam }
            this.size = 15
            this.page = 1
            this.getList(this.formData)
        },
        openShare() {
            if (this.$route.query && this.$route.query.shareReportId) {
                this.title = decodeURIComponent(this.$route.query.shareReportName)
                this.previewUrl = '/reportShare/preview'
                this.$refs.reportPreview.open({ reportId: this.$route.query.shareReportId })
            }
        },
        onBeforeClose(done) {
            console.log('done')
            done()
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
                this.closeLoad()
            } else {
                const str = '响应时间过长已断开连接！请稍后重试！'
                this.$confirm(str, '提示', {
                    confirmButtonText: '知道了',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {})
                this.closeLoad()
            }
        },
        /**
       * 切换分页大小
       */
        handleSizeChange(size) {
            this.size = size
            this.page = 1
            this.getList()
        },
        /**
       * 变更页码
       */
        handleCurrentChange(page) {
            this.page = page
            this.getList()
        }

    }
}
</script>

<style scoped lang="scss">
.dream-page-wrap {
    background-color: #fff;
}
p, ul {
    margin-block-start: 0;
    margin-block-end: 0;
}
::v-deep .pageContainer {
    width: 100%;
    margin-top: 10px;
    text-align: right;
}
::v-deep .previewWindow {
    .el-dialog {
        margin-bottom: 0;
        .el-dialog__header {
            border-bottom: 1px solid #e5e5e5;
        }
        .el-dialog__body {
            height: 80vh;
        }
    }
}
.report-dialog {
    ::v-deep .el-dialog {
    height: 90%;
    margin-top: 4.5vh !important;

    &.is-fullscreen {
        height: 100%;
        margin: 0 !important;
    }

    .report-dialog-header {
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-right: 20px;

        i {
            color: #909399;
            font-size: 16px;
            cursor: pointer;
        }
    }

    .el-dialog__body {
      height: calc(100% - 60px);
      padding: 0;
      position: relative;
    }
  }
}
.absolute-container {
    height: calc(100% - 0px) !important;
}
.el-button--text {
    color: #409EFF;
    justify-content: left;
    padding-top: 0;
}
li:has(.el-button--text):hover {
    color: #409EFF;
    background-color: rgba(51,69,315,0.1);
}
.vxe-button.el-button--text:not(.is--disabled):hover {
    color: #409EFF;
}
.vxe-gird-box, .el-form-item, .el-card__body {
    .el-button {
        padding: 8px 10px;
    }
}
</style>
