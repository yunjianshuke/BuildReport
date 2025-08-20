<template>
    <el-row :gutter="10" style="height: 100%;">
        <el-col :span="5" :xl="4">
            <div class="grid-mod">
                <el-card class="fm-generate-card clearfix is-always-shadow">
                    <el-button type="primary" @click.stop="appendRoot">新建分组</el-button>
                    <div class="grid-content" style="margin-top: 10px;">
                        <el-tree
                            ref="tree" :data="datas" node-key="id" :default-expanded-keys="expandedKeys"
                            :highlight-current="true" :props="defaultProps" @node-click="handleNodeClick"
                        >
                            <div slot-scope="{ node, data }" class="custom-tree-node">
                                <span>{{ node.label }}</span>
                                <span>
                                    <!-- <i
                                        class="el-icon-circle-plus" style="margin: 0 6px;color: #67C23A;"
                                        @click.stop="append(data)"
                                    /> -->
                                    <i
                                        class="el-icon-edit" style="margin: 0 6px;color: #1E90FF;"
                                        @click.stop="editNode(data)"
                                    />
                                    <i
                                        v-if="data.ifCanDel" class="el-icon-delete-solid"
                                        style="margin: 0 6px;color: #F56C6C;" @click.stop="remove(node, data)"
                                    />
                                    <i
                                        v-else class="el-icon-delete-solid" disabled
                                        style="margin: 0 6px;color: rgba(255, 255, 255, 0);"
                                    />
                                </span>
                            </div>
                        </el-tree>
                    </div>
                </el-card>
            </div>
        </el-col>
        <el-col :span="19" :xl="20" class="grid-mod grid-mod-flex" style="height: 100%;">
            <el-card class="fm-generate-card clearfix is-always-shadow" style="margin-bottom: 18px;">
                <el-form :inline="true">
                    <el-form-item label="业务关系名称">
                        <el-input v-model="queryParams.name" placeholder="业务关系名称" size="small" />
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
                    :data="relationList"
                    :border="true"
                    :stripe="true"
                    row-key="field"
                    :highlight-current-row="true"
                    height="calc(100vh - 320px)"
                    style="width: 100%"
                >
                    <el-table-column label="序号" type="index" align="center" width="80" />
                    <el-table-column prop="relationName" label="业务关系名称" align="center" min-width="300" />
                    <el-table-column prop="bizGroupName" label="分组名称" align="center" min-width="100" />
                    <!-- <el-table-column prop="createByName" label="创建人" align="center" min-width="100" /> -->
                    <el-table-column prop="createTime" label="创建时间" align="center" min-width="120" />
                    <el-table-column prop="updateTime" label="更新时间" align="center" min-width="120" />
                    <el-table-column label="操作" align="center" width="480" fixed="right">
                        <template #default="{ row }">
                            <el-button type="text" icon="el-icon-coin" size="mini" @click="handleDataset(row, '0')"> 设计器数据集 </el-button>
                            <el-button type="text" icon="el-icon-coin" size="mini" @click="handleDataset(row, '1')"> 筛选数据来源 </el-button>
                            <el-button type="text" icon="el-icon-edit" size="mini" @click="handleEdit(row)"> 编辑 </el-button>
                            <el-button type="text" icon="el-icon-edit" size="mini" @click="handleEditGroup(row)"> 修改分组 </el-button>
                            <el-button type="text" icon="el-icon-delete" size="mini" @click="handleDelete(row)"> 删除 </el-button>
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
            <el-dialog v-loading="loading" :visible.sync="visible" :title="title" class="relation-dialog" :fullscreen="isFullscreen" width="50%" :close-on-click-modal="false" :before-close="beforeClose" append-to-body>
                <div slot="title" class="report-dialog-header">
                    <div class="report-dialog-title">业务关系配置</div>
                    <div class="report-dialog-btns">
                        <i class="el-icon-full-screen" @click.stop="changFullScreen" />
                    </div>
                </div>
                <Chart v-if="visible" ref="chart" :relation-id="relationId" @save="onSave" />
            </el-dialog>
            <el-dialog v-loading="datasetLoading" :visible.sync="datasetVisible" :title="datasetTitle" class="relation-dialog" :fullscreen="isDatasetFullscreen" width="50%" :close-on-click-modal="false" :before-close="datasetBeforeClose" append-to-body>
                <div slot="title" class="report-dialog-header">
                    <div class="report-dialog-title">{{ businessType === '0' ? `设计器数据集(${relationName})`: `筛选数据来源(${relationName})` }}</div>
                    <div class="report-dialog-btns">
                        <i class="el-icon-full-screen" @click.stop="changDatasetFullScreen" />
                    </div>
                </div>
                <dataset-list v-if="datasetVisible" ref="datasets" :relation-id="relationId" :business-type="businessType" />
            </el-dialog>
            <!-- 添加树节点 -->
            <el-dialog title="新建分组" :visible="showAddNode" class="configWindow" width="20%" :close-on-click-modal="false" :show-close="false">
                <div v-if="showAddNode">
                    <el-form ref="formGroup" :model="newNodeform" :rules="rules" label-width="120px">
                        <el-form-item label="分组名称" prop="name">
                            <el-input v-model="newNodeform.name" />
                        </el-form-item>

                        <el-form-item label="业务分组code" prop="bizGroupCode">
                            <el-input v-model="newNodeform.bizGroupCode" :disabled="treeNodeEditStatus === 'edit'" />
                        </el-form-item>
                    </el-form>
                </div>
                <div slot="footer" class="dialog-footer">
                    <el-button type="primary" @click.prevent.stop="saveNode">确定</el-button>
                    <el-button type="default" @click.prevent.stop="showAddNode = false">取消</el-button>
                </div>
            </el-dialog>
            <el-dialog
                top="7vh" width="30%" class="configWindow" :visible="groupVisible" title="修改分组"
                :close-on-click-modal="false" :before-close="closeConfig" @closed="groupWindowClosed"
            >
                <div class="form-container">
                    <el-form ref="groupForm" :model="groupForm" label-width="100px" :rules="groupRules">
                        <el-form-item label="业务名称:" prop="name">
                            {{ groupForm.name }}
                        </el-form-item>
                        <el-form-item label="业务分组:" prop="groupId" required>
                            <el-cascader
                                v-model="groupForm.groupId" style="width: 100%;" :options="datas"
                                :props="{ checkStrictly: true, emitPath: false, label: 'name', value: 'id' }"
                                placeholder="请选择分组" clearable
                            />
                        </el-form-item>
                    </el-form>
                </div>
                <div class="form-operation">
                    <el-button type="primary" @click.prevent.stop="groupConfirm">确定</el-button>
                    <el-button type="default" @click.prevent.stop="groupVisible = false">取消</el-button>
                </div>
            </el-dialog>
        </el-col>
    </el-row>
</template>

<script>
const checkTableName = (rule, value, callback) => {
    if (!value) {
        return callback(new Error('请输入业务分类逻辑名'))
    }
    const regexp = new RegExp('^(?![0-9_A-Z])[a-z0-9_]+$', 'g')
    const test = regexp.test(value)
    if (!test) {
        return callback(new Error('请用⼩写字⺟或数字组合下划线，禁⽌以数字和下划线开头，禁止含有特殊字符'))
    }
}
import Chart from './components/Chart'
import DatasetList from './components/DatasetList'
export default {
    name: 'ReportRelation',
    components: {
        Chart,
        DatasetList
    },
    props: {
        listUrl: {
            type: String,
            default: '/relation/page'
        },
        addUrl: {
            type: String,
            default: '/relation/save'
        },
        deleteUrl: {
            type: String,
            default: '/relation/delete'
        },
        detailUrl: {
            type: String,
            default: '/relation/detail'
        },
        treeUrl: {
            type: String,
            default: '/businessGroup/tree'
        },
        treeUpdateUrl: {
            type: String,
            default: '/businessGroup/update'
        },
        treeSaveUrl: {
            type: String,
            default: '/businessGroup/saveEntity'
        },
        treeDeleteUrl: {
            type: String,
            default: '/businessGroup/deleteByIds'
        }
    },
    data() {
        return {
            loading: false,
            datas: [],
            defaultProps: {
                children: 'children',
                label: 'name'
            },
            expandedKeys: [],
            showAddNode: false,
            newNodeform: {
                name: '',
                bizGroupCode: ''
            },
            rules: {
                name: [
                    { required: true, message: '请输入分组名', trigger: 'blur' }
                ],
                bizGroupCode: [
                    { required: true, message: '请输入业务分组code', trigger: 'blur' },
                    { validator: checkTableName, trigger: 'blur' }
                ]
            },
            treeNodeEditStatus: '',
            selectNode: null,
            queryParams: {
                groupId: '',
                name: ''
            },
            relationList: [],
            page: 1,
            size: 15,
            total: 0,
            visible: false,
            title: '新增',
            isFullscreen: true,
            relationId: '',
            relationName: '',
            datasetVisible: false,
            datasetLoading: false,
            datasetTitle: '数据集',
            isDatasetFullscreen: true,
            businessType: '0',
            groupForm: {
                relationId: '',
                name: '',
                groupId: ''
            },
            groupVisible: false,
            groupRules: {
                groupId: [
                    { required: true, message: '请选择分组', trigger: 'blur' }
                ]
            }
        }
    },
    mounted() {
        this.loadTree()
        this.getList()
    },
    methods: {
        // 获取tree
        loadTree(data = {}) {
            this.$ajax({
                url: this.treeUrl,
                method: 'post',
                data: {
                    ...data
                }
            }).then(data => {
                if (data.code === 0 && data.data) {
                    const datas = data.data

                    const compt = arr => {
                        arr.forEach(ele => {
                            ele.ifCanDel = true
                            if (ele.children && ele.children.length > 0) {
                                ele.ifCanDel = false
                                compt(ele.children)
                            }
                        })
                    }
                    compt(datas)

                    this.datas = datas
                    this.$nextTick(() => {
                        this.openTree()
                    })
                } else if (data.code === 0 && !data.data) {
                    this.datas = []
                }
            })
        },
        openTree(open = true) {
            // 默认展开树
            const expandedKeys = []
            const getKeys = arr => {
                arr.forEach(ele => {
                    expandedKeys.push(ele.id)
                    if (ele.children && ele.children.length > 0) {
                        getKeys(ele.children)
                    }
                })
            }
            getKeys(this.datas)
            if (open) {
                this.expandedKeys = expandedKeys
            }
            return expandedKeys
        },
        // 点击树的节点
        handleNodeClick(data) {
            this.selectNode = data
            this.queryParams.groupId = data.id
            this.getList()
        },
        // 添加根节点
        appendRoot() {
            this.showAddNode = true
            this.treeNodeEditStatus = 'add'
            this.newNodeform = {
                bizGroupName: '',
                name: '',
                bizGroupCode: '',
                parentId: '',
                projectId: '',
                children: []
            }
        },
        // 添加节点
        append(data) {
            this.showAddNode = true
            this.treeNodeEditStatus = 'add'
            this.newNodeform = {
                bizGroupName: '',
                name: '',
                bizGroupCode: '',
                parentId: data.id,
                projectId: data.projectId,
                children: []
            }
        },
        // 编辑节点
        editNode(data) {
            this.showAddNode = true
            this.treeNodeEditStatus = 'edit'
            this.newNodeform = data
        },
        // 保存新增/编辑的分组
        saveNode() {
            if (!this.newNodeform.name || !this.newNodeform.bizGroupCode) {
                this.$message.error('请将表单填写完整')
                return
            }
            const regexp = new RegExp('^(?![0-9_A-Z])[a-z0-9_]+$', 'g')
            const test = regexp.test(this.newNodeform.bizGroupCode)
            if (!test) {
                this.$message.error('请用⼩写字⺟或数字组合下划线，禁⽌以数字和下划线开头，禁止含有特殊字符')
                return
            }
            this.newNodeform.bizGroupName = this.newNodeform.name
            if (this.newNodeform.id) {
                // 编辑
                this.$ajax({
                    url: this.treeUpdateUrl,
                    method: 'post',
                    data: {
                        ...this.newNodeform
                    }
                }).then(data => {
                    if (data.code === 0) {
                        this.showAddNode = false
                        this.loadTree()
                    }
                }).catch(() => {
                })
            } else {
                // 新增
                this.$ajax({
                    url: this.treeSaveUrl,
                    method: 'post',
                    data: {
                        ...this.newNodeform
                    }
                }).then(data => {
                    if (data.code === 0) {
                        this.showAddNode = false
                        this.loadTree()
                    }
                }).catch(() => {
                })
            }

        },
        // 删除节点
        remove(node, data) {
            this.$confirm('是否删除该分组？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.$ajax({
                    url: this.treeDeleteUrl,
                    method: 'post',
                    params: {
                        ids: data.id
                    }
                }).then(data => {
                    if (data.code === 0) {
                        this.loadTree()
                        this.$message.success('已删除')
                    } else if (data.code === 1) {
                        // this.$message.error('删除失败')  
                    }
                }).catch(() => {
                })

            })

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
                    vm.relationList = res.data.records
                    vm.total = res.data.total
                    return
                }
                vm.relationList = []
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
                name: '',
                groupId: ''
            }
            this.$refs.tree.setCurrentKey(null)
            this.page = 1
            this.getList()
        },
        beforeClose(done) {
            if (this.$refs.chart && this.$refs.chart.isChange()) {
                this.$confirm('数据未保存，确认操作？', '提示', {
                    distinguishCancelAndClose: true,
                    confirmButtonText: '保存并返回',
                    cancelButtonText: '返回',
                    type: 'warning'
                })
                    .then(() => {
                        this.onSaveRelationByClose(done)
                    }).catch(action => {
                        if (action === 'cancel') {
                            done()
                        }
                    })
            } else {
                done()
            }
        },
        datasetBeforeClose(done) {
            done()
        },
        onSave(form, done) {
            this.loading = true
            const vm = this
            vm.$ajax({
                url: vm.addUrl,
                method: 'post',
                data: form
            }).then(res => {
                if (res.code === 0) {
                    vm.$message.success('保存成功')
                    done && done()
                    vm.visible = false
                    vm.getList()
                }
            }).finally(() => {
                this.loading = false
            })
        },
        handleAdd() {
            this.title = '新增'
            this.relationId = ''
            this.visible = true
        },
        handleEdit(row) {
            this.title = '编辑'
            this.relationId = row.relationId
            this.visible = true
        },
        handleDelete(row) {
            const vm = this
            vm.$confirm(`确认删除业务关系： ${row.relationName} 吗？`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
                closeOnClickModal: false
            }).then(() => {
                vm.loading = true
                vm.$ajax({
                    url: `${vm.deleteUrl}/${row.relationId}`,
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
        onSaveRelationByClose(done) {
            if (!this.$refs.chart.isValid()) {
                return
            }
            const data = this.$refs.chart.getData()
            if (!data.relationName) {
                return 
            }
            this.onSave(data, done)
        },
        changFullScreen() {
            this.isFullscreen = !this.isFullscreen
            this.$nextTick(() => {
                this.$refs.chart.resize()
            })
        },
        changDatasetFullScreen() {
            this.isDatasetFullscreen = !this.isDatasetFullscreen
            this.$nextTick(() => {
                setTimeout(() => {
                    this.$refs.datasets.$refs.table.doLayout()
                }, 0)
            })
        },
        handleDataset(row, businessType) {
            this.relationId = row.relationId
            this.relationName = row.relationName
            this.businessType = businessType
            this.datasetVisible = true
        },
        /**
         * 关闭配置弹窗
         */
        closeConfig() {
            const vm = this
            vm.groupVisible = false
            vm.saveAsVisible = false
        },
        groupWindowClosed() {
            this.getList()
        },
        saveAsWindowClosed() {
            this.getList()
        },
        handleEditGroup(row) {
            this.groupForm.groupId = row.groupId
            this.groupForm.relationId = row.relationId
            this.groupForm.name = row.relationName
            this.groupVisible = true
        },
        groupConfirm() {
            this.$refs.groupForm.validate().then(valid => {
                if (valid) {
                    this.$ajax({
                        url: '/relation/updateGroup',
                        method: 'post',
                        data: { bizGroupId: this.groupForm.groupId, relationId: this.groupForm.relationId }
                    }).then(data => {
                        if (data.code === 0) {
                            this.$message.success('修改成功')
                            this.groupVisible = false
                        }
                    })
                }
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

.pageContainer {
  width: 100%;
  margin-top: 10px;
  text-align: right;
}
::v-deep .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
}

.relation-dialog {
  ::v-deep .el-dialog {
    height: 90%;
    margin-top: 4.5vh !important;

    &.is-fullscreen {
        height: 100%;
        margin: 0 !important;
    }

    .el-dialog__header {
      padding-top: 10px;
      line-height: 1;
    }

    .el-dialog__headerbtn {
      top: 10px;
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
      height: calc(100% - 39px);
      padding: 0;
      position: relative;
    }
  }
}
.vxe-gird-box, .el-form-item, .el-card__body {
    .el-button {
        padding: 8px 10px;
    }
    ::v-deep .el-table {
        .el-table__fixed-right {
            .el-table__fixed-body-wrapper {
                tr {
                    td:last-child {
                        text-align: left;
                        .el-button {
                            margin-left: 0!important;
                            margin-right: 10px;
                        }
                    }
                }
            }
        }
    }
}

.grid-content {
    background-color: #f7f7f7;
    padding: 10px;
}

.configWindow {
    ::v-deep .el-dialog {
        width: 900px;
        margin-bottom: 0;

        .el-dialog__body {
            padding: 15px;
        }

        .form-container {
            display: flex;
            flex-wrap: wrap;
            width: 100%;

            ul {
                list-style-type: none;
                padding-left: 0;
                display: flex;
                width: 45%;
                margin: 10px 0;

                &.U1 {
                    width: 90%;
                }

                .label {
                    width: 100px;
                    line-height: 32px;
                    text-align: center
                }

                .content {
                    flex: 1;

                    p {
                        line-height: 32px;
                    }
                }
            }
        }

        .form-operation {
            display: flex;
            justify-content: center;
            border-top: 1px solid #dcdfe6;
            padding-top: 15px;

            .el-button {
                margin-right: 20px;
            }
        }
    }
}
</style>
