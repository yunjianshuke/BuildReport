<template>
    <div class="grid-mod grid-mod-flex">
        <el-card class="fm-generate-card clearfix is-always-shadow" style="margin-bottom: 18px;">
            <el-form :inline="true">
                <el-form-item label="缓存名称">
                    <el-input v-model="queryParams.dictName" placeholder="缓存名称" size="small" />
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
                v-loading="loading" :data="sourceList" :border="true" :stripe="true" row-key="field"
                :highlight-current-row="true" height="calc(100vh - 320px)" style="width: 100%"
            >
                <el-table-column label="序号" type="index" align="center" width="80" />
                <el-table-column prop="dictName" label="缓存名称" align="center" min-width="300" />
                <el-table-column label="操作" align="center" width="200">
                    <template #default="{ row }">
                        <el-button type="text" icon="el-icon-edit" size="mini" @click="handleEdit(row)"> 编辑 </el-button>
                        <el-button type="text" icon="el-icon-delete" size="mini" @click="handleDelete(row)"> 删除 </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div style="padding: 10px 0">
                <el-pagination
                    :background="true" class="pageContainer" :page-size="size" :current-page="page"
                    :page-sizes="[2, 4, 10, 15, 20]" layout="total, sizes, prev, pager, next, jumper" :total="total"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange"
                />
            </div>
        </div>
        <BusinessDialog :title="title" :visible.sync="visible" width="70%" :fullscreen="true" footer @close="closeDialog">
            <template #body>
                <div class="cache-body">
                    <div ref="cacheMenu" v-loading="menuLoading" class="cache-menu">
                        <el-input
                            v-model="filterText" class="cache-filter" placeholder="输入关键字进行过滤"
                            @change="filterChange"
                        />
                        <el-tree
                            ref="cacheTree" class="cache-tree" :data="treeData" :props="defaultProps"
                            :filter-node-method="filterNode"
                        >
                            <template slot-scope="{ node }">
                                <drag :transfer-data="node">
                                    <div class="custom-tree-node" :title="node.data.name">
                                        <div :class="'is-leaf' + node.level" style="line-height: 16px;">
                                            {{ node.data.name }}
                                        </div>
                                        <div
                                            v-if="node.level === 2" :class="'is-leaf' + node.level"
                                            style="line-height: 16px;color: #8492a6; font-size: 12px"
                                        >
                                            {{ node.data.comment }}
                                        </div>
                                    </div>
                                </drag>
                            </template>
                        </el-tree>
                    </div>
                    <div
                        class="cache-main"
                        :class="{ 'cache-empty': form.datasourceId === '' }"
                    >
                        <drop
                            class="cache-drop"
                            @drop="onDragEnd"
                        >
                            <el-row v-if="form.datasourceId" :gutter="20">
                                <el-col :span="14">
                                    <div class="cache-title b-md">{{ form.tableName }}</div>
                                    <el-form ref="form" class="cache-form" :rules="formRules" :model="form" label-width="120px">
                                        <el-form-item label="缓存名称" prop="dictName">
                                            <el-input v-model="form.dictName" placeholder="请输入缓存名称" style="width: 200px" />
                                        </el-form-item>
                                        <el-form-item label="缓存KEY" prop="dictValueField">
                                            <el-select v-model="form.dictValueField" placeholder="请选择缓存KEY" style="width: 200px">
                                                <el-option v-for="item in fieldList" :key="item.value" :label="item.value" :value="item.value" />
                                            </el-select>
                                        </el-form-item>
                                        <el-form-item label="缓存VALUE" prop="dictLabelField">
                                            <el-select v-model="form.dictLabelField" placeholder="请选择缓存VALUE" style="width: 200px">
                                                <el-option v-for="item in fieldList" :key="item.value" :label="item.value" :value="item.value" />
                                            </el-select>
                                        </el-form-item>
                                        <el-form-item label="默认缓存VALUE" prop="defaultLabel">
                                            <el-input v-model="form.defaultLabel" placeholder="请输入默认显示" style="width: 200px" />
                                        </el-form-item>
                                        <el-divider />
                                        <el-form-item label="筛选条件">
                                            <div
                                                v-for="(item, index) in form.conditionList" :key="index"
                                                class="condition-item"
                                            >
                                                <el-select
                                                    v-model="item.conditionKey" placeholder="请选择字段"
                                                    style="width: 200px"
                                                >
                                                    <el-option
                                                        v-for="field in fieldList" :key="field.value"
                                                        :label="field.value" :value="field.value"
                                                    />
                                                </el-select>
                                                <span>=</span>
                                                <el-input
                                                    v-model="item.conditionValue" placeholder="请输入值"
                                                    style="width: 200px"
                                                />
                                                <el-button
                                                    type="text" icon="el-icon-remove-outline" size="mini"
                                                    @click="removeCondition(index)"
                                                />
                                                <el-button v-if="form.conditionList.length - 1 === index" type="text" size="mini" icon="el-icon-circle-plus-outline" @click="addCondition" />
                                            </div>
                                            <el-button v-if="form.conditionList.length === 0" type="text" size="mini" icon="el-icon-circle-plus-outline" @click="addCondition" />
                                        </el-form-item>
                                    </el-form>
                                </el-col>
                                <el-col :span="10">
                                    <div class="cache-title b-md">示例</div>
                                    <div class="cache-map"><span>缓存结构：&lt;{{ form.dictName || '缓存名称' }}: &lt; {{ form.dictValueField || '缓存KEY' }}: {{ form.dictLabelField || '缓存VALUE' }} &gt;&gt;</span><el-button size="mini" @click="handleImage">使用原理</el-button></div>
                                    <div v-if="visibleImage" class="demo">
                                        <el-image style="width: 100%; height: 100%" :src="demoSrc" fit="contain" :preview-src-list="[demoSrc]" />
                                    </div>
                                </el-col>
                            </el-row>
                        </drop>
                    </div>
                </div>
            </template>
            <template #dialogBtns>
                <el-button type="primary" :loading="loading" @click="doSave">确 定</el-button>
                <el-button @click="visible = false">取 消</el-button>
            </template>
        </BusinessDialog>
    </div>
</template>

<script>
import { pick } from 'lodash'
import demoSrc from '../assets/demo.png'
import BusinessDialog from '@/components/BusinessDialog/index.vue'

export default {
    name: 'ReportCache',
    components: {BusinessDialog},
    props: {
        listUrl: {
            type: String,
            default: '/reportDict/getPage'
        },
        addUrl: {
            type: String,
            default: '/reportDict/save'
        },
        deleteUrl: {
            type: String,
            default: '/reportDict/remove'
        },
        detailUrl: {
            type: String,
            default: '/reportDict/datail'
        }
    },
    data() {
        return {
            loading: false,
            queryParams: {
                dictName: ''
            },
            sourceList: [],
            page: 1,
            size: 15,
            total: 0,
            visible: false,
            title: '新增',
            form: {
                id: '',
                datasourceId: '',
                tableName: '',
                dictName: '',
                dictValueField: '',
                dictLabelField: '',
                defaultLabel: '',
                conditionList: []
            },
            formRules: {
                dictName: [
                    { required: true, message: '请输入缓存名称', trigger: 'blur' }
                ],
                dictValueField: [
                    { required: true, message: '请选择实际字段', trigger: 'blur' }
                ],
                dictLabelField: [
                    { required: true, message: '请选择显示字段', trigger: 'blur' }
                ]
            },
            fieldList: [],
            menuLoading: false,
            filterText: '',
            treeData: [],
            defaultProps: {},
            demoSrc,
            visibleImage: false
        }
    },
    mounted() {
        this.getList()
    },
    methods: {
        /**
         * 加载菜单数据
         */
        loadMenuData() {
            const vm = this
            this.menuLoading = true
            vm.$ajax({
                url: '/datasource/list',
                method: 'post',
                data: {
                }
            }).then(res => {
                if (res.code === 0 && res.data) {
                    vm.treeData = res.data
                    const promises = []
                    vm.treeData.forEach(item => {
                        promises.push(vm.$ajax({
                            url: vm.filterText ? `/datasource/getTables/${item.id}/${vm.filterText}` : `/datasource/getTables/${item.id}`
                        }))
                    })
                    Promise.all(promises).then(resArray => {
                        resArray.forEach((subRes, index) => {
                            if (subRes.code === 0 && subRes.data) {
                                vm.$set(vm.treeData[index], 'children', subRes.data)
                            }
                        })
                        if (this.filterText) {
                            vm.$nextTick(() => {
                                this.$refs.cacheTree.filter(this.filterText)
                            })
                        }
                    }).finally(() => {
                        this.menuLoading = false
                    })
                }
            }).catch(() => {
                this.menuLoading = false
            })
        },
        filterChange() {
            this.loadMenuData()
        },
        filterNode(value, data) {
            if (!value) return true
            return data.name.indexOf(value) !== -1 || data.comment.indexOf(value) !== -1
        },
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
                url: vm.addUrl,
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
            this.form = {
                id: '',
                datasourceId: '',
                tableName: '',
                dictName: '',
                dictValueField: '',
                dictLabelField: '',
                defaultLabel: '',
                conditionList: []
            }
            this.title = '新增'
            this.loadMenuData()
            this.visible = true
        },
        handleEdit(row) {
            this.title = '编辑'
            this.form = pick(row, ['id', 'datasourceId', 'tableName', 'dictName', 'dictValueField', 'dictLabelField', 'defaultLabel', 'conditionList'])
            this.loadFields()
            this.loadMenuData()
            this.visible = true
        },
        handleDelete(row) {
            const vm = this
            vm.$confirm(`确认删除缓存： ${row.dictName} 吗？`, '提示', {
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
        },
        doSave() {
            if (!this.form.datasourceId) {
                this.$message.warning('请选择需要缓存的表')
                return false
            }
            this.$refs.form.validate(valid => {
                if (!valid) {
                    return false
                }

                // 验证条件列表
                if (this.form.conditionList && this.form.conditionList.length > 0) {
                    const invalidCondition = this.form.conditionList.some(item => {
                        return !item.conditionKey || !item.conditionValue
                    })
                    if (invalidCondition) {
                        this.$message.warning('请完善筛选条件信息')
                        return false
                    }
                }

                this.loading = true
                const params = {
                    ...this.form,
                    conditionList: this.form.conditionList || []
                }

                // 调用保存接口
                this.$ajax({
                    url: this.addUrl,
                    method: 'post',
                    data: params
                }).then(res => {
                    if (res.code === 0) {
                        this.$message.success('保存成功')
                        this.visible = false
                        this.getList()
                    }
                }).finally(() => {
                    this.loading = false
                })
            })
        },
        closeDialog() {
            this.getList()
        },
        addCondition() {
            this.form.conditionList.push({
                conditionKey: '',
                conditionValue: ''
            })
        },
        removeCondition(index) {
            this.form.conditionList.splice(index, 1)
        },
        onDragEnd(node) {
            this.form.datasourceId = node.parent.data.id
            this.form.tableName = node.data.name
            this.loadFields(node)
        },
        loadFields() {
            this.$ajax({
                url: `/datasource/getTableFields/${this.form.datasourceId}/${this.form.tableName}`,
                method: 'get',
                data: {
                }
            }).then(res => {
                if (res.code === 0) {
                    this.fieldList = res.data.map(item => {
                        return {
                            value: item.name,
                            label: item.remark || item.name
                        }
                    })
                }
            })
        },
        handleImage() {
            this.visibleImage = !this.visibleImage
        }
    }
}
</script>

<style scoped lang="scss">
.pb-md {
    padding-top: 10px;
    padding-bottom: 10px;
}

.b-md {
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

.vxe-gird-box, .el-form-item, .el-card__body {
    .el-button {
        padding: 8px 10px;
    }
}

.cache-body {
    display: flex;
    width: 100%;
    height: 100%;

    .cache-menu {
        width: 200px;
        height: 100%;
        border-right: 1px #e8e8e8 solid;
        overflow-y: auto;
        overflow-x: hidden;

        .custom-tree-node {
            cursor: pointer;
            width: 100%;
            height: 40px;
            user-select: none;
            -webkit-user-select: none;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            justify-content: center;
        }
    }

    .cache-filter {
        padding: 10px;
    }

    .cache-main {
        width: 0;
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow-y: auto;

        &.cache-empty {
          background-image: url('../assets/cache.svg');
          background-position: center;
          background-repeat: no-repeat;
          background-size: 300px 300px;
        }

        .cache-drop {
            width: 100%;
            height: 100%;
            .cache-title {
                font-size: 20px;
            }
            padding: 10px;
            .condition-item {
                display: flex;
                gap: 10px;
                justify-content: flex-start;
                align-items: center;
                margin-bottom: 10px;
            }
            .el-divider--horizontal {
                margin: 2px 0 16px 0;
            }
        }
    }

    ::v-deep .el-tree-node__content {
        padding-left: 0 !important;
        height: auto;
    }
}
.cache-map {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px;
}
</style>
