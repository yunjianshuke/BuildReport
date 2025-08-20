<template>
    <div class="grid-mod grid-mod-flex">
        <el-card class="fm-generate-card clearfix is-always-shadow" style="margin-bottom: 18px;">
            <el-form :inline="true">
                <el-form-item label="数据源名称">
                    <el-input v-model="queryParams.name" placeholder="数据源名称" size="small" />
                </el-form-item>
                <el-form-item style="margin-left: 30px;">
                    <el-button icon="el-icon-search" type="primary" @click="getList">
                        搜索
                    </el-button>
                    <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <div class="vxe-gird-box">
            <div class="pb-md">
                <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增</el-button>
            </div>
            <el-table
                v-loading="loading"
                :data="sourceList"
                :border="true"
                :stripe="true"
                row-key="field"
                :highlight-current-row="true"
                height="calc(100vh - 320px)"
                style="width: 100%"
            >
                <el-table-column label="序号" type="index" align="center" width="80" />
                <el-table-column prop="name" label="数据源名称" align="center" min-width="300" />
                <el-table-column label="操作" align="center" width="200">
                    <template #default="{ row }">
                        <el-button :disabled="['buildreport_demo', 'buildreport_demo2', 'buildreport_etl_demo'].includes(row.name)" type="text" icon="el-icon-edit" size="mini" @click="handleEdit(row)"> 编辑 </el-button>
                        <el-button :disabled="['buildreport_demo', 'buildreport_demo2', 'buildreport_etl_demo'].includes(row.name)" type="text" icon="el-icon-delete" size="mini" @click="handleDelete(row)"> 删除 </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div style="padding: 10px 0">
                <el-pagination
                    :background="true"
                    class="pageContainer"
                    :page-size="size"
                    :current-page="page"
                    :page-sizes="[2, 4, 10, 15, 20]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="total"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                />
            </div>
        </div>
        <el-drawer :title="title" custom-class="source-drawer" :visible.sync="visible" direction="rtl" :before-close="beforeClose">
            <report-upsert v-if="visible" v-loading="loading" :config="config" @save="onSave" />
        </el-drawer>
    </div>
</template>

<script>
import ReportUpsert from './components/Upsert.vue'
const sourceTypeMap = {
    0: 'mysql',
    1: 'oracle',
    2: 'postgresql'
}
export default {
    name: 'ReportSource',
    components: {
        ReportUpsert
    },
    props: {
        listUrl: {
            type: String,
            default: '/datasource/page'
        },
        addUrl: {
            type: String,
            default: '/datasource/create'
        },
        editUrl: {
            type: String,
            default: '/datasource/update'
        },
        deleteUrl: {
            type: String,
            default: '/datasource/delete'
        },
        detailUrl: {
            type: String,
            default: '/datasource/datail' 
        }
    },
    data() {
        return {
            loading: false,
            queryParams: {
                name: ''
            },
            sourceList: [],
            page: 1,
            size: 15,
            total: 0,
            visible: false,
            title: '新增',
            config: {}
        }
    },
    mounted() {
        this.getList()
    },
    methods: {
        /**
         * 获取列表
         */
        getList() {
            this.loading = true
            const vm = this
            vm.$ajax({
                url: vm.listUrl,
                method: 'post',
                data: {
                    size: vm.size,
                    current: vm.page,
                    ...vm.queryParams
                }
            }).then(res => {
                if (res.code === 0 && res.data && res.data.total) {
                    vm.sourceList = res.data.records
                    vm.total = res.data.total
                    return
                }
                vm.sourceList = []
                vm.total = 0
            }).finally(() => {
                this.loading = false
            })
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
        },
        reset() {
            this.queryParams = {
                name: ''
            }
            this.page = 1
            this.getList()
        },
        beforeClose(done) {
            done()
        },
        onSave(form) {
            this.loading = true
            const vm = this
            vm.$ajax({
                url: form.id ? `${vm.editUrl}/${form.id}` : vm.addUrl,
                method: 'post',
                data: form
            }).then(res => {
                if (res.code === 0) {
                    vm.$message.success('保存成功')
                    vm.visible = false
                    vm.getList()
                }
            }).finally(() => {
                this.loading = false
            })
        },
        handleAdd() {
            this.config = { id: '', name: '', type: 0, options: { }}
            this.title = '新增'
            this.visible = true
        },
        handleEdit(row) {
            this.title = '编辑'
            row.sourceType = sourceTypeMap[row.type]
            this.config = row
            this.visible = true
        },
        handleDelete(row) {
            const vm = this
            vm.$confirm(`确认删除数据源： ${row.name} 吗？`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
                closeOnClickModal: false
            }).then(() => {
                vm.loading = true
                vm.$ajax({
                    url: `${vm.deleteUrl}/${row.id}`,
                    method: 'post'
                }).then(res => {
                    if (res.code === 0) {
                        vm.$message.success('删除成功')
                        vm.getList()
                    }
                }).finally(() => {
                    this.loading = false
                })
            })
        } 
    }
}
</script>

<style scoped lang="scss">
.pb-md {
    padding-top: 10px;
    padding-bottom: 10px;
}

p,
ul {
    margin-block-start: 0;
    margin-block-end: 0;
}

.pageContainer {
  width: 100%;
  margin-top: 10px;
  text-align: right;
}

.el-drawer__wrapper {
  ::v-deep .source-drawer .el-drawer__header {
    margin-bottom: 0px;
    padding: 10px;
  }
}
.vxe-gird-box, .el-form-item, .el-card__body {
    .el-button {
        padding: 8px 10px;
    }
}
</style>
