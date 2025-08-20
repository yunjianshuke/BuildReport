<template>
    <div style="width: 100%;height: 100%;">
        <AbsoluteContainer>
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
                                            <i
                                                class="el-icon-circle-plus" style="margin: 0 6px;color: #67C23A;"
                                                @click.stop="append(data)"
                                            />
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
                        <el-form :inline="true" class="report-manager-list-search">
                            <el-form-item label="报表名称">
                                <el-input v-model="queryParams.name" placeholder="请输入报表名称" size="small" />
                            </el-form-item>
                            <el-form-item style="margin-left: 30px;">
                                <el-button icon="el-icon-search" type="primary" @click="getList">
                                    搜索
                                </el-button>
                                <el-button icon="el-icon-refresh" @click="reset">重置</el-button>
                            </el-form-item>
                        </el-form>
                    </el-card>
                    <div class="vxe-gird-box" style="display: flex; flex-direction: column;">
                        <div class="pb-md">
                            <el-button
                                type="primary" icon="el-icon-plus"
                                @click="handleAdd"
                            >
                                新增
                            </el-button>
                        </div>
                        <el-table
                            v-loading="loading" :data="reportList" :border="true" :stripe="true" row-key="field"
                            :highlight-current-row="true" style="width: 100%;"
                            height="calc(100vh - 320px)"
                        >
                            <el-table-column label="序号" type="index" align="center" width="80" />
                            <el-table-column prop="name" label="报表名称" align="center" min-width="240" />
                            <el-table-column prop="bizGroupName" label="分组名称" align="center" min-width="100" />
                            <el-table-column prop="createTime" label="创建时间" align="center" min-width="150" />
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
                </el-col>
            </el-row>
        </AbsoluteContainer>
        <!-- 设置 -->
        <!-- 添加树节点 -->
        <el-dialog title="新建分组" :visible="showAddNode" width="20%" :close-on-click-modal="false" :show-close="false">
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
            :visible.sync="designerVisible" :title="designerTitle" fullscreen class="designer-dialog"
            :close-on-click-modal="false" :before-close="designerBeforeClose" :modal="false" append-to-body
            @closed="designerClosed"
        >
            <report-designer-v2
                v-if="designerVisible" :report-id="currentReportId" @close="designerVisible = false"
                @preview="preview"
            />
        </el-dialog>
        <el-dialog
            top="7vh" width="30%" class="configWindow" :visible="groupVisible" title="修改分组"
            :close-on-click-modal="false" :before-close="closeConfig" @closed="groupWindowClosed"
        >
            <div class="form-container">
                <el-form ref="groupForm" :model="groupForm" label-width="100px" :rules="groupRules">
                    <el-form-item label="报表名称:" prop="name">
                        <el-input v-model="groupForm.name" />
                    </el-form-item>
                    <el-form-item label="报表分组:" prop="groupId" required>
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
        <el-dialog
            top="7vh" width="30%" class="configWindow" :visible="saveAsVisible" title="复制"
            :close-on-click-modal="false" :before-close="closeConfig" @closed="saveAsWindowClosed"
        >
            <div class="form-container">
                <el-form ref="saveAsForm" :model="saveAsForm" label-width="100px" :rules="saveAsRules">
                    <el-form-item label="报表名称:" prop="name" required>
                        <el-input v-model="saveAsForm.name" />
                    </el-form-item>
                    <el-form-item label="报表分组:" prop="groupId" required>
                        <el-cascader
                            v-model="saveAsForm.groupId" style="width: 100%;" :options="datas"
                            :props="{ checkStrictly: true, emitPath: false, label: 'name', value: 'id' }"
                            placeholder="请选择分组" clearable
                        />
                    </el-form-item>
                </el-form>
            </div>
            <div class="form-operation">
                <el-button type="primary" @click.prevent.stop="saveAsConfirm">确定</el-button>
                <el-button type="default" @click.prevent.stop="saveAsVisible = false">取消</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
const checkTableName = (rule, value, callback) => {
    if (!value) {
        return callback(new Error('请输入报表分类逻辑名'))
    }
    const regexp = new RegExp('^(?![0-9_A-Z])[a-z0-9_]+$', 'g')
    const test = regexp.test(value)
    if (!test) {
        return callback(new Error('请用⼩写字⺟或数字组合下划线，禁⽌以数字和下划线开头，禁止含有特殊字符'))
    }
}
import AbsoluteContainer from '@/components/AbsoluteContainer/index.vue'
import ReportDesignerV2 from './components/index.vue'
import operatorBtn from '@/components/yjOperator/index.vue' // 有操作区域按钮组件渲染 customAction.length>0
export default {
    name: 'ReportList',
    components: {
        AbsoluteContainer,
        ReportDesignerV2,
        operatorBtn
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
            reportList: [],
            refreshPlan: false, // 计划刷新列表
            page: 1,
            size: 15,
            total: 0,
            url: {
                manager: '/reportInfo/page',
                authList: '/reportAuth/list',
                authAdd: '/reportAuth/save',
                authDelete: '/reportInfo/delete',
                dept: '/admin/dept/tree',
                tree: '/reportGroup/tree',
                saveEntity: '/reportGroup/saveEntity',
                update: '/reportGroup/update',
                deleteByIdsTree: '/reportGroup/deleteByIds',
                preview: '/reportInfo/preview'
            },
            designerVisible: false,
            designerTitle: '',
            previewQuery: {},
            previewVisible: false,
            isFullscreen: false,
            groupForm: {
                reportId: '',
                name: '',
                groupId: ''
            },
            groupVisible: false,
            groupRules: {
                name: [
                    { required: true, message: '请输入报表名称', trigger: 'blur' }
                ],
                groupId: [
                    { required: true, message: '请选择分组', trigger: 'blur' }
                ]
            },
            saveAsForm: {
                reportId: '',
                name: '',
                groupId: ''
            },
            saveAsVisible: false,
            saveAsRules: {
                name: [
                    { required: true, message: '请输入报表名称', trigger: 'blur' }
                ],
                groupId: [
                    { required: true, message: '请选择分组', trigger: 'blur' }
                ]
            },
            currentReportId: '',
            operationBtns: [  // 操作列按钮配置，disabledMethod和visibleMethod为可选配置。默认为false和true
                {
                    code: 'edit',
                    name: '编辑',
                    type: 'text',
                    icon: 'el-icon-edit',
                    clickMethod: this.handleEdit
                },
                {
                    code: 'editgroup',
                    name: '修改名称分组',
                    type: 'text',
                    icon: 'el-icon-edit',
                    clickMethod: this.handleEditGroup
                },
                {
                    code: 'saveas',
                    name: '复制',
                    type: 'text',
                    icon: 'el-icon-copy-document',
                    clickMethod: this.handleSaveAs
                },
                {
                    code: 'delete',
                    name: '删除',
                    type: 'text',
                    icon: 'el-icon-delete',
                    clickMethod: this.handleDelete
                }

            ],
            previewUrl: ''
        }
    },
    
    mounted() {
        this.getList()
        this.loadTree()
    },
    beforeDestroy() {
        this.printIns && this.printIns.stopSse() && (this.printIns = null)
        this.downloadIns && this.downloadIns.stopSse() && (this.downloadIns = null)
    },
    methods: {
        // 获取tree
        loadTree(data = {}) {
            this.$ajax({
                url: this.url.tree,
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
                    url: this.url.update,
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
                    url: this.url.saveEntity,
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
                    url: this.url.deleteByIdsTree,
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
            const vm = this
            this.loading = true
            vm.$ajax({
                url: vm.url.manager,
                method: 'post',
                data: {
                    size: vm.size,
                    current: vm.page,
                    name: vm.queryParams.name,
                    groupId: vm.queryParams.groupId
                }
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
            this.getList(page)
        },
        groupWindowClosed() {
            this.getList()
        },
        saveAsWindowClosed() {
            this.getList()
        },
        // 删除报表
        handleDelete(row) {
            this.$confirm('是否删除该条数据？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.$ajax({
                    url: `${this.url.authDelete}/${row.reportId}`,
                    method: 'post'
                }).then(() => {
                    this.getList()
                    this.$message.success('已删除')
                }).catch(() => {
                })
            })

        },
        handleAdd() {
            this.currentReportId = ''
            this.designerTitle = '报表设计'
            this.designerVisible = true
        },
        handleEdit(row) {
            this.designerTitle = row.name
            this.currentReportId = row.reportId
            this.designerVisible = true
        },
        handleEditGroup(row) {
            this.groupForm.groupId = row.groupId
            this.groupForm.reportId = row.reportId
            this.groupForm.name = row.name
            this.groupVisible = true
        },
        groupConfirm() {
            this.$refs.groupForm.validate().then(valid => {
                if (valid) {
                    this.$ajax({
                        url: '/reportInfo/saveGroup',
                        method: 'post',
                        data: { groupId: this.groupForm.groupId, name: this.groupForm.name, reportId: this.groupForm.reportId }
                    }).then(data => {
                        if (data.code === 0) {
                            this.$message.success('修改成功')
                            this.groupVisible = false
                        }
                    })
                }
            })
        },
        saveAsConfirm() {
            this.$refs.saveAsForm.validate().then(valid => {
                if (valid) {
                    this.$ajax({
                        url: '/reportInfo/saveAs',
                        method: 'post',
                        data: { groupId: this.saveAsForm.groupId, name: this.saveAsForm.name, reportId: this.saveAsForm.reportId }
                    }).then(data => {
                        if (data.code === 0) {
                            this.$message.success('修改成功')
                            this.saveAsVisible = false
                        }
                    })
                }
            })
        },
        // 重置
        reset() {
            this.queryParams = {
                name: '',
                groupId: ''
            }
            this.$refs.tree.setCurrentKey(null)
            this.page = 1
            this.getList()
        },
        designerBeforeClose(done) {
            done()
        },
        previewBeforeClose(done) {
            done()
        },
        designerClosed() {
            this.getList()
        },
        previewClosed() {

        },
        preview(query) {
            this.previewQuery = query
            this.previewVisible = true
        },
        handleSaveAs(row) {
            this.saveAsForm.reportId = row.reportId
            this.saveAsForm.name = ''
            this.saveAsForm.groupId = row.groupId
            this.saveAsVisible = true
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
        }
    }
}
</script>

<style scoped lang="scss">
p,
ul {
    margin-block-start: 0;
    margin-block-end: 0;
}

.pb-md {
    padding-top: 10px;
    padding-bottom: 10px;
}

::v-deep .pageContainer {
    width: 100%;
    margin-top: 10px;
    text-align: right;
}

.grid-content {
    background-color: #f7f7f7;
    padding: 10px;
}

.report-manager-list-search {
    .el-form-item {
        margin-bottom: 0px;
    }
}

::v-deep {
    .sourceSel {
        width: 100% !important;

        .el-input {
            width: 100% !important;

            input {
                width: 100% !important;
            }
        }
    }
}

::v-deep .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
}

::v-deep .configWindow {
    * {
        ::-webkit-scrollbar {
            width: 8px !important;
            height: 8px !important;
        }

        ::-webkit-scrollbar-track-piece {
            background-color: #6767671A !important;
            border-radius: 8px !important;
            -webkit-border-radius: 8px !important;
        }

        ::-webkit-scrollbar-thumb:vertical {
            background-color: #69686866 !important;
            border-radius: 8px !important;
            -webkit-border-radius: 8px !important;
        }

        ::-webkit-scrollbar-thumb:horizontal {
            background-color: #EAEAEA67 !important;
            border-radius: 8px !important;
            -webkit-border-radius: 8px !important;
        }
    }

    .el-dialog {
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

                    .array-dynamic-wrap {
                        max-height: 225px;
                        overflow-x: hidden;
                        overflow-y: auto;
                        padding-right: 10px;

                        .array-dynamic-item {
                            display: flex;
                            align-items: center;
                            margin-bottom: 6px;
                            padding-bottom: 6px;
                            border-bottom: 1px dashed #eee;

                            .el-input {
                                margin-right: 10px;
                                vertical-align: top;
                                width: 200px;
                            }

                            .el-textarea {
                                margin-right: 10px;
                                vertical-align: top;
                                width: 400px;
                            }
                        }
                    }

                    .ORGSelect {
                        width: 100%;

                        ul {
                            width: 100%;
                        }

                        .ORGOption {
                            .el-scrollbar {
                                .el-select-dropdown__wrap {
                                    max-height: inherit;

                                    .el-select-dropdown__list {
                                        flex-direction: column;
                                        padding: 10px 20px 20px 10px;

                                        .ORGFilterInput {
                                            margin-bottom: 5px;
                                        }

                                        .el-tree {
                                            padding-right: 5px;
                                            max-height: 208px;
                                            overflow-x: hidden;
                                            overflow-y: auto;
                                        }
                                    }
                                }
                            }
                        }
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

.designer-dialog {
    ::v-deep .el-dialog {
        margin: 0;
        height: 100vh;

        .el-dialog__header {
            display: none;
        }

        .el-dialog__body {
            height: 100vh;
            padding: 0;
            position: relative;
        }
    }
}

.preview-dialog {
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

.vxe-gird-box, .el-form-item, .el-card__body {
    .el-button {
        padding: 8px 10px;
    }
}
.absolute-container {
    height: calc(100% - 0px) !important;
    padding-right: 20px;
}
</style>
