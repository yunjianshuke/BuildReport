<template>
    <div class="designer-v2-dataset">
        <div v-if="businessType === '1'" class="dataset-relation">
            <!-- <el-input v-model="filterText" class="relation-chart-filter" placeholder="输入关键字进行过滤" clearable /> -->
            <!-- <div v-if="businessType === '0'">
                <drag
                    v-for="item in relationListFilter"
                    :key="item.relationId"
                    :transfer-data="item"
                    @dragstart="dragging = item.group"
                    @dragend="dragging = null"
                >
                    <div class="relation-item">
                        <i class="el-icon-rank" /> {{ item.relationName }}
                    </div>
                </drag>
            </div> -->
            <div>
                <!-- <el-tree
                    ref="customTree"
                    class="custom-tree"
                    :data="customData"
                    :props="defaultProps"
                    default-expand-all
                    :filter-node-method="filterNode"
                >
                    <template slot-scope="{ node }">

                    </template>
                </el-tree> -->
                <drag
                    v-for="item in customData"
                    :key="item.name"
                    class="custom-list"
                    :transfer-data="item"
                    @dragstart="handleCustomDragStart(item)"
                    @dragend="dragging = null"
                >
                    <div v-if="businessType === '0'" :title="item.comment">
                        <i class="el-icon-rank" /> {{ item.name }}
                    </div>
                    <div v-if="businessType === '1'" class="custom-list-comment">
                        <i class="el-icon-rank" /> <div><div>{{ item.name }}</div><div v-if="item.comment" class="custom-list-comment-item">{{ item.comment }}</div></div>
                    </div>
                </drag>
            </div>
        </div>
        <div v-loading="loading" class="dataset-select">
            <drop
                style="width: 0; height: 100%; flex: 1; display: flex; flex-direction: column;"
                :class="{ allowed: dragging === 'relation', 'dataset-empty': !current && businessType === '1' }"
                @dragover="handleRelationDragover('relation', ...arguments)"
                @drop="onRelationDragEnd"
            >
                <!-- <div class="dataset-list">
                    <el-tag
                        v-for="(item, index) in filterDatasets"
                        :key="item.datasetId"
                        :type="current && current.datasetId === item.datasetId ? '': 'info'"
                        effect="plain"
                        closable
                        size="medium"
                        @click="handleSelect(item, true)"
                        @close.stop="handleClose(index, 'dataset')"
                    >
                        {{ item.datasetName }}
                    </el-tag>
                    <el-tag v-if="current && !current.datasetId" closable size="medium" @close="handleClose(-1, 'add')">新增数据集</el-tag>
                </div> -->
                <div v-if="current" class="dataset-body">
                    <div class="dataset-name">
                        <span>名称：</span>
                        <el-input v-model="current.datasetName" placeholder="请输入数据集名称" clearable />
                        <el-button type="primary" style="margin-left: 10px;" @click="onSaveDataset">保存</el-button>
                        <el-button type="primary" @click="onPreviewDataset">预览</el-button>
                    </div>
                    <div class="dataset-tables">
                        <div v-for="table, index in current.tables" :key="table.key" class="dataset-table">
                            <template v-if="!table.hidden">
                                <div class="dataset-table-name">
                                    <div>{{ table.aliasName }}</div>
                                    <div><el-checkbox v-model="table.checkAll" :indeterminate="table.isIndeterminate" @change="(isChange) => { handleCheckAllChange(isChange, index) }">全选</el-checkbox></div>
                                </div>
                                <div class="dataset-table-fields">
                                    <drag
                                        v-for="field in table.fields"
                                        :key="field.key"
                                        :transfer-data="field"
                                        @dragstart="dragging = field.group"
                                        @dragend="dragging = null"
                                    >
                                        <el-checkbox v-model="field.checked" border :title="field.name + '\n' + field.alias" @change="handleCheckedCitiesChange(index)">{{ field.alias }}</el-checkbox>
                                    </drag>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>
                <div v-if="showPreview" class="dataset-preview" :style="{ height: boxHeight + 'px', minHeight: boxMinHeight + 'px' }">
                    <div class="dataset-preview-topbar" @mousedown.prevent="startResize">
                        预览
                        <el-button type="text" icon="el-icon-close" @click="closePreview" />
                    </div>
                    <el-table ref="previewTable" :data="previewData" size="mini" width="100%" :height="boxHeight - 36" border stripe fit>
                        <el-table-column v-for="field in previewFields" :key="field.key" :prop="field.key" :label="field.alias" />
                    </el-table>
                </div>
            </drop>
            <drop
                class="dataset-parameter"
                :class="{ allowed: dragging === 'field', 'params-empty': dataFilter.length === 0 }"
                @dragover="handleFieldDragover('field', ...arguments)"
                @drop="onParamDragEnd"
            >
                <div class="parameter-title">{{ businessType === '0' ? '组件筛选字段配置': '筛选范围配置' }}</div>
                <div v-for="(param, index) in dataFilter" :key="'field' + param.key + index" class="dataset-parameter-item">
                    <div class="param-label">{{ param.alias }}</div>
                    <div class="param-icon" @click.stop="openParameter(index)"><i class="el-icon-edit" /></div>
                    <div class="param-icon" @click.stop="deleteParameter(index)"><i class="el-icon-delete" /></div>
                </div>
            </drop>
        </div>
        <el-dialog
            title="筛选条件配置"
            :visible.sync="paramVisible"
            :append-to-body="true"
            custom-class="param-dialog"
            width="740px"
        >
            <template v-if="currentParam">
                <el-divider content-position="left">筛选条件</el-divider>
                <el-row :gutter="20">
                    <el-col :span="6">字段</el-col>
                    <el-col :span="6">条件</el-col>
                    <el-col :span="12">默认值</el-col>
                </el-row>
                <el-row class="mar-t-10" :gutter="20" type="flex" align="middle">
                    <el-col :span="6">{{ currentParam.alias }}</el-col>
                    <el-col :span="6">
                        <el-select v-model="currentParam.condition" clearable @change="handleConditionChange">
                            <el-option
                                v-for="(funItem, funIndex) in fun"
                                :key="'condition-' + funIndex"
                                :label="funItem.name"
                                :value="funItem.key"
                            />
                        </el-select>
                    </el-col>
                    <el-col :span="12">
                        <el-date-picker
                            v-if="currentParam.type === 'DATETIME' && currentParam.condition !== 'DYNAMIC_DATE'"
                            :key="currentParam.condition"
                            v-model="currentParam.defaultValue"
                            :append-to-body="false"
                            style="width: 100%;"
                            :type="dateType"
                            :value-format="dateFormat"
                            :format="dateFormat"
                            placeholder="选择默认值"
                            range-separator="至"
                            start-placeholder="选择默认值"
                            end-placeholder="选择默认值"
                            clearable
                        />
                        <el-date-picker
                            v-else-if="currentParam.type === 'DATE' && currentParam.condition !== 'DYNAMIC_DATE'"
                            :key="currentParam.condition"
                            v-model="currentParam.defaultValue"
                            :append-to-body="false"
                            style="width: 100%;"
                            :type="dateType"
                            :value-format="dateFormat"
                            :format="dateFormat"
                            placeholder="选择默认值"
                            clearable
                        />
                        <el-select v-else-if="isDynamicDate" v-model="currentParam.defaultValue" style="width: 100%;">
                            <el-option
                                v-for="(dynamicItem, dynamicIndex) in dynamicDateValue"
                                :key="'dynamic-' + dynamicIndex"
                                :label="dynamicItem.name"
                                :value="dynamicItem.key"
                            />
                        </el-select>
                        <el-time-picker
                            v-else-if="currentParam.type === 'TIME'"
                            :key="currentParam.condition"
                            v-model="currentParam.defaultValue"
                            :append-to-body="false"
                            :picker-options="{
                                selectableRange: '00:00:00 - 23:59:59'
                            }"
                            value-format="HH:mm:ss"
                            placeholder="选择默认值"
                            clearable
                        />
                        <el-input v-else-if="componentMultipleReadonly" v-model="defaultValueMultiple" placeholder="输入默认值" clearable @change="defaultValueMultipleChange" />
                        <el-input v-else v-model="currentParam.defaultValue" placeholder="输入默认值" clearable />
                    </el-col>
                </el-row>
            </template>
            <span slot="footer" class="dialog-footer">
                <el-button type="primary" size="mini" @click="saveParam">确 定</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import { cloneDeep } from 'lodash'
import { expressionMap, componentSource, datetimeFormatList, dateGroup, listGroup, dynamicDateValue } from '@/views/report-v2/manager/components/const/index'
import CryptoJS from 'crypto-js'

export default {
    name: 'DesignerDataset',
    props: {
        listUrl: {
            type: String,
            default: '/datasource/list'
        },
        getTables: {
            type: String,
            default: '/datasource/getTables'
        },
        getTableFields: {
            type: String,
            default: '/datasource/getTableFields'
        },
        relationUrl: {
            type: String,
            default: '/relation/listByAuth'
        },
        relationDetailUrl: {
            type: String,
            default: '/relation/info'
        },
        previewUrl: {
            type: String,
            default: '/dataset/run'
        },
        getPreview: {
            type: String,
            default: '/datasource/preview'
        },
        datasetDetailUrl: {
            type: String,
            default: '/reportInfo/getDataset'
        },
        businessType: {
            type: String,
            default: '0'
        },
        relationId: {
            type: String,
            default: ''
        },
        datasetId: {
            type: String,
            default: ''
        },
        datasets: {
            type: Array,
            default() {
                return []
            }
        }
    },
    data() {
        return {
            // 过滤关键字
            filterText: '',
            // 当前数据集
            current: null,
            // 业务关系列表
            relationList: [],
            // 业务关系缓存索引
            relationMap: {},
            // 自定义缓存索引
            datasourceMap: {},
            // 数据集预览实际数据
            previewData: [],
            // 数据集预览表头字段
            previewFields: [],
            // 显示数据集预览
            showPreview: false,
            // 拖拽中类型
            dragging: null,
            // 数据集类型
            datasetType: this.businessType,
            // 数据集树型结构配置
            defaultProps: {
                children: 'children',
                label: 'name'
            },
            // 自定义数据集列表
            customData: [],
            // 请求中动画
            loading: false,
            // 数据集筛选是否显示
            paramVisible: false,
            // 当前选中的筛选列表下标
            paramIndex: -1,
            // 组件数据来源类型
            componentSource,
            // 组件options的列表
            optionsFields: [],
            // 日期组件的Format类型
            datetimeFormatList,
            dynamicDateValue,
            boxHeight: 300,
            boxMinHeight: 160,
            componentGroup: ['select', 'radio', 'checkbox'],
            defaultValueMultiple: '',
            datasetCache: '',
            filterDatasets: []
        }
    },
    computed: {
        relationListFilter() {
            if (this.filterText) {
                return this.relationList.filter(relation => {
                    return relation.relationName.indexOf(this.filterText) > -1
                })
            }
            return this.relationList
        },
        dataFilter() {
            return this.current && this.current.dataFilter ? this.current.dataFilter : []
        },
        currentParam() {
            return this.paramIndex === -1 || this.paramIndex >= this.dataFilter.length ? null : this.dataFilter[this.paramIndex]
        },
        fun() {
            return this.currentParam && this.currentParam.type ? expressionMap[this.currentParam.type] : []
        },
        dateFormat() {
            let result = 'yyyy-MM-dd'
            if (this.currentParam && this.currentParam.type === 'DATETIME') {
                result = 'yyyy-MM-dd HH:mm:ss'
            }
            if (this.currentParam && this.currentParam.condition) {
                switch (this.currentParam.condition) {
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
        dateType() {
            let result = 'date'
            if (this.currentParam && this.currentParam.type === 'DATETIME') {
                result = 'datetime'
            }
            if (this.currentParam && this.currentParam.condition) {
                switch (this.currentParam.condition) {
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
                        result = this.currentParam.type === 'DATETIME' ? 'datetimerange' : 'daterange'
                        break
                }
            }
            return result
        },
        componentMultipleReadonly() {
            return this.currentParam ? listGroup.indexOf(this.currentParam.condition) > -1 : false
        },
        isDynamicDate() {
            return ['DATETIME', 'DATE'].some(item => { return item === this.currentParam.type }) && this.currentParam.condition === 'DYNAMIC_DATE'
        }
    },
    watch: {
        filterText(val) {
            this.$refs.customTree && this.$refs.customTree.filter(val)
        }
    },
    mounted() {
        this.filterDatasets = this.datasets.filter(item => { return item.businessType === this.businessType })
        if (this.businessType === '0') {
            // this.loadRelation()
            this.getRelationById()
        } else {
            this.initCustomDatasourceField(this.datasets)
        }
    },
    methods: {
        getRelationById() {
            this.$ajax({
                url: `${this.relationDetailUrl}/${this.relationId}`,
                method: 'post'
            }).then(res => {
                if (res.code === 0) {
                    const item  = res.data
                    item.type = '0'
                    item.nodeList.forEach((table, index) => {
                        table.key = `table${index}`
                        table.fields = JSON.parse(table.componentJson)
                        table.fields.forEach(field => {
                            field.key = `${table.aliasName}_${field.name}`
                            field.checked = false
                            // 拖拽分组
                            field.group = 'field'
                            field.cellId = table.cellId
                        })
                    })
                    // 缓存索引业务关系
                    this.relationMap[item.relationId] = item
                    // 拖拽分组
                    item.group = 'relation'
                    this.relationList = [item]
                    if (this.datasetId) {
                        this.setDefaultCurrent()
                    } else {
                        this.onRelationDragEnd(item)
                    }
                }
            })
        },
        /**
         * 开始上下拖动
         * @param {*} event
         */
        startResize(event) {
            this.resizing = true
            this.startY = event.clientY
            document.addEventListener('mousemove', this.doResize)
            document.addEventListener('mouseup', this.stopResize)
        },
        /**
         * 上下拖动
         * @param {*} event
         */
        doResize(event) {
            if (this.resizing) {
                const offset = this.startY - event.clientY
                this.boxHeight += offset
                if (this.boxHeight < this.boxMinHeight) {
                    this.boxHeight = this.boxMinHeight
                }
                this.startY = event.clientY
            }
        },
        /**
         * 上下拖动结束
         */
        stopResize() {
            this.resizing = false
            document.removeEventListener('mousemove', this.doResize)
            document.removeEventListener('mouseup', this.stopResize)
        },
        /**
         * 拖拽业务关系
         * @param {*} group
         * @param {*} data
         * @param {*} event
         */
        handleRelationDragover(group, data, event) {
            if (group && data && group === 'relation') {
                if (group !== data.group) {
                    event.dataTransfer.dropEffect = 'none'
                }
            }
        },
        /**
         * 拖拽筛选字段
         * @param {*} group
         * @param {*} data
         * @param {*} event
         */
        handleFieldDragover(group, data, event) {
            if (group && data && group === 'field') {
                if (group !== data.group) {
                    event.dataTransfer.dropEffect = 'none'
                }
                event.stopPropagation()
            }
        },
        /**
         * 菜单筛选
         */
        filterNode(value, data) {
            if (!value) return true
            return data.name.indexOf(value) !== -1
        },
        /**
         * 加载业务关系
         */
        loadRelation() {
            this.loading = true
            this.$ajax({
                url: this.relationUrl,
                method: 'get'
            }).then(res => {
                if (res.code === 0 && res.data) {
                    res.data.forEach(item => {
                        // 设置数据集类型
                        item.type = '0'
                        item.nodeList.forEach((table, index) => {
                            table.key = `table${index}`
                            table.fields = JSON.parse(table.componentJson)
                            table.fields.forEach(field => {
                                field.key = `${table.aliasName}_${field.name}`
                                field.checked = false
                                // 拖拽分组
                                field.group = 'field'
                                field.cellId = table.cellId
                            })
                        })
                        // 缓存索引业务关系
                        this.relationMap[item.relationId] = item
                        // 拖拽分组
                        item.group = 'relation'
                    })
                    this.relationList = res.data
                    this.setDefaultCurrent()
                }
            }).finally(() => {
                this.loading = false
            })
        },
        /**
         * 加载数据源和数据表
         */
        loadCustomTables() {
            this.$ajax({
                url: `/datasource/getTablesByRelation/${this.relationId}`,
                method: 'get',
                data: {
                }
            }).then(res => {
                if (res.code === 0) {
                    this.customData = res.data.map(item => {
                        return {
                            comment: item.comment,
                            name: item.name,
                            group: 'relation',
                            relationId: this.relationId,
                            relationName: item.datasourceName,
                            dataSourceId: item.datasourceId,
                            type: '1'
                        }
                    })
                    // this.customData.forEach(item => {
                    //     this.$ajax({
                    //         url: `${this.getTables}/${item.id}`
                    //     }).then(res => {
                    //         if (res.code === 0 && res.data) {
                    //             res.data.forEach(childItem => {
                    //                 // 添加拖拽分组
                    //                 childItem.group = 'relation'
                    //                 childItem.relationId = this.relationId
                    //                 // 自定义时，relationName使用数据源名称
                    //                 childItem.relationName = childItem.name
                    //                 childItem.dataSourceId = item.id
                    //                 // 设置数据集类型
                    //                 childItem.type = '1'
                    //             })
                    //             // 组装树型结构
                    //             this.$set(item, 'children', res.data)
                    //         }
                    //     })
                    // })
                }
            }).finally(() => {
                this.loading = false
            })
        },
        /**
         * 加载自定义表字段
         * @param {*} dataSourceId
         * @param {*} tableName
         */
        loadCustomFields(dataSourceId, tableName, show) {
            this.loading = true
            this.$ajax({
                url: `${this.getTableFields}/${dataSourceId}/${tableName}`,
                method: 'get',
                data: {
                }
            }).then(res => {
                if (res.code === 0 && res.data) {
                    res.data.forEach(item => {
                        item.alias = item.remark || item.name
                        item.checked = false
                        // 添加拖拽分组
                        item.group = 'field'
                        item.key = `${item.name}`
                    })
                    // 缓存索引自定义表数据
                    this.datasourceMap[dataSourceId + tableName] = {
                        nodeList: [{
                            dataSourceId,
                            cellId: tableName,
                            aliasName: tableName,
                            tableName: tableName,
                            fields: res.data
                        }]
                    }
                    // 拖拽自定义表后显示字段
                    if (show) {
                        this.current = {
                            datasetId: this.datasetId,
                            datasetName: this.filterDatasets.length ? this.filterDatasets[0].datasetName : `${tableName}数据集`,
                            relationId: this.relationId,
                            dataSourceId: dataSourceId,
                            tables: cloneDeep(this.datasourceMap[dataSourceId + tableName].nodeList).map(item => {
                                Object.assign(item, {
                                    isIndeterminate: false,
                                    checkAll: false
                                })
                                return item
                            }),
                            dataFilter: [],
                            type: '1',
                            businessType: this.businessType
                        }
                        // 设置数据缓存
                        this.setDataCache()
                    } else {
                        this.setDefaultCurrent()
                    }
                }
            }).finally(() => {
                this.loading = false
            })
        },
        /**
         * 拖拽放下业务关系
         * @param {*} relation
         */
        onRelationDragEnd(relation) {
            if (relation) {
                if (relation.type === '0') {
                    this.current = {
                        datasetId: '',
                        datasetName: `${relation.relationName}数据集`,
                        relationId: relation.relationId,
                        tables: cloneDeep(this.relationMap[relation.relationId].nodeList).map(item => {
                            Object.assign(item, {
                                isIndeterminate: false,
                                checkAll: false
                            })
                            return item
                        }),
                        dataFilter: [],
                        type: '0',
                        businessType: this.businessType
                    }
                    // 设置数据缓存
                    this.setDataCache()
                } else {
                    this.loadCustomFields(relation.dataSourceId, relation.name, true)
                }
            }
        },
        /**
         * 拖拽放下筛选字段
         * @param {*} field
         * @param {*} event
         */
        onParamDragEnd(field, event) {
            event.stopPropagation()
            if (field) {
                // 组装筛选条件数据结构
                this.dataFilter.push({
                    datasetName: this.current.datasetName,
                    cellId: field.cellId,
                    key: field.key,
                    name: field.name,
                    type: field.type,
                    alias: field.alias,
                    condition: '',
                    defaultValue: ''
                })
                this.paramIndex = this.dataFilter.length - 1
                // 弹出筛选条件设置窗口
                this.paramVisible = true
            }
        },
        /**
         * 选择已保存的数据集
         * @param {*} item
         */
        handleSelect(item, valid) {
            if (valid && this.isChange()) {
                this.$message.warning('请保存数据集')
                return
            }
            let tables = {}
            if (item.type === '0') {
                if (!this.relationMap[item.relationId]) {
                    return
                }
                tables = cloneDeep(this.relationMap[item.relationId].nodeList)
            } else {
                if (!this.datasourceMap[item.dataSourceId + item.fields[0].tableName]) {
                    return
                }
                tables = cloneDeep(this.datasourceMap[item.dataSourceId + item.fields[0].tableName].nodeList)
            }
            this.datasetType = item.type
            this.setDefaultCheck(tables, item.fields)
            this.current = Object.assign({
                tables
            }, item)
            // 设置数据缓存
            this.setDataCache()
        },
        /**
         * 勾选已选中的字段
         * @param {*} tables
         * @param {*} fields
         */
        setDefaultCheck(tables, fields) {
            const fieldMap = fields.reduce((all, item) => {
                if (all[item.cellId]) {
                    all[item.cellId].push(item.name)
                } else {
                    all[item.cellId] = [item.name]
                }
                return all
            }, {})
            tables.forEach(table => {
                Object.assign(table, {
                    isIndeterminate: false,
                    checkAll: false
                })
                if (fieldMap[table.cellId]) {
                    table.fields.forEach(field => {
                        if (fieldMap[table.cellId].indexOf(field.name) > -1) {
                            field.checked = true
                            table.isIndeterminate = true
                        }
                    })
                    if (table.fields.length === table.fields.filter(item => item.checked).length) {
                        table.isIndeterminate = false
                        table.checkAll = true
                    }
                }
            })
        },
        /**
         * 关闭新增数据集或删除数据集
         * @param {*} index
         * @param {*} type
         */
        handleClose(index, type) {
            this.current = null
            this.datasetCache = ''
            if (type !== 'add') {
                // eslint-disable-next-line vue/no-mutating-props
                this.datasets.splice(index, 1)
            }
            this.closePreview()
        },
        /**
         * 保存数据集
         */
        onSaveDataset() {
            const isAdd = !this.current.datasetId
            // 校验数据集名称重复
            if (this.datasets.some(dataset => { return dataset.datasetId !== this.current.datasetId && dataset.datasetName === this.current.datasetName })) {
                this.$message.warning('数据集名称重复')
                return
            }

            // 组装数据集字段列表
            const fields = []
            this.current.tables.forEach(table => {
                table.fields.forEach(field => {
                    if (field.checked) {
                        fields.push(Object.assign({ cellId: table.cellId, dataSourceId: table.dataSourceId, aliasName: table.aliasName, tableName: table.tableName, datasetName: this.current.datasetName }, field))
                    }
                })
            })

            // 校验字段列表长度
            if (fields.length === 0) {
                this.$message.warning('请至少选择一个字段')
                return
            }

            // 创建新数据集
            const dataset = {
                datasetId: isAdd ? '' : this.current.datasetId,
                datasetName: this.current.datasetName,
                relationId: this.current.relationId,
                dataSourceId: this.current.dataSourceId,
                type: this.datasetType,
                businessType: this.businessType,
                dataFilter: this.current.dataFilter,
                fields: fields
            }

            // 通知数据集变化
            this.$emit('change', this.getDatasetParams([dataset]))
            // 重置数据缓存
            this.setDataCache()
        },
        /**
         * 预览数据集选中字段
         */
        onPreviewDataset() {
            this.loading = true
            if (this.datasetType === '0') {
                const data = this.current.tables.map(table => {
                    return {
                        cellId: table.cellId,
                        tableName: table.tableName,
                        aliasName: table.aliasName,
                        sqlFieldList: table.fields.filter(field => {
                            return field.checked
                        })
                    }
                }).filter(table => table.sqlFieldList.length)
                this.previewFields = this.current.tables.reduce((all, table) => {
                    all.push(...table.fields.filter(field => { return field.checked }))
                    return all
                }, [])
                this.$ajax({
                    url: `${this.previewUrl}/${this.current.relationId}`,
                    method: 'post',
                    data: {
                        data,
                        dataFilters: this.current.dataFilter
                    }
                }).then(res => {
                    if (res.code === 0 && res.data) {
                        this.previewData = res.data
                        this.showPreview = true
                        this.$nextTick(() => {
                            this.$refs.previewTable.doLayout()
                        })
                    }
                }).finally(() => {
                    this.loading = false
                })
            } else if (this.datasetType === '1') {
                const dataSourceId = this.current.tables[0].dataSourceId
                const aliasName = this.current.tables[0].aliasName
                this.previewFields = this.current.tables[0].fields.filter(field => field.checked)

                this.$ajax({
                    url: `${this.getPreview}/${dataSourceId}/${aliasName}`,
                    methods: 'get',
                    data: {
                        dataFilters: this.current.dataFilter
                    }
                }).then(res => {
                    if (res.code === 0 && res.data) {
                        this.previewData = res.data
                        this.showPreview = true
                        this.$nextTick(() => {
                            this.$refs.previewTable.doLayout()
                        })
                    }
                }).finally(() => {
                    this.loading = false
                })
            }
        },
        /**
         * 关闭预览
         */
        closePreview() {
            this.showPreview = false
            this.previewData = []
            this.previewFields = []
        },
        /**
         * 打开筛选器属性设置窗口
         */
        openParameter(index) {
            this.paramIndex = index
            const currentParam = this.dataFilter[this.paramIndex]
            if (Array.isArray(currentParam.defaultValue) && currentParam.defaultValue.length) {
                this.defaultValueMultiple = currentParam.defaultValue[0]
            }
            this.paramVisible = true
        },
        /**
         * 获取数据集接口保存结构
         */
        getDatasetParams(datasets) {
            return datasets.map(dataset => {
                return {
                    id: dataset.datasetId,
                    datasetName: dataset.datasetName,
                    relationId: dataset.relationId,
                    dataSourceId: dataset.dataSourceId,
                    type: dataset.type,
                    tables: dataset.fields.reduce((all, item) => {
                        const index = all.findIndex(field => { return field.cellId === item.cellId })
                        if (index === -1) {
                            all.push({
                                cellId: item.cellId,
                                tableName: item.tableName,
                                aliasName: item.aliasName,
                                sqlFieldList: [item]
                            })
                        } else {
                            all[index].sqlFieldList.push(item)
                        }
                        return all
                    }, []),
                    businessType: dataset.businessType,
                    dataFilter: dataset.dataFilter
                }
            })
        },
        /**
         * 删除筛选条件
         */
        deleteParameter(index) {
            this.dataFilter.splice(index, 1)
        },
        /**
         * 筛选条件保存
         */
        saveParam() {
            if (!this.currentParam.condition) {
                this.$message.warning('请填写查询条件')
                return
            }
            this.paramVisible = false
        },
        /**
         * 拖拽自定义表
         */
        handleCustomDragStart(item) {
            this.dragging = item.group
        },
        /**
         * 编辑报表时，已保存自定义数据集，加载数据集字段
         */
        initCustomDatasourceField(customDatasource) {
            this.loadCustomTables()
            customDatasource.forEach(item => {
                Array.isArray(item.fields) && item.fields.length && this.loadCustomFields(item.dataSourceId, item.fields[0].tableName)
            })
        },
        /**
         * 条件切换
         */
        handleConditionChange() {
            if (dateGroup.indexOf(this.currentParam.type) > -1) {
                if (this.currentParam.condition === 'BETWEEN') {
                    this.currentParam.defaultValue = ['', '']
                    this.dataFilter[this.paramIndex].defaultValue = ['', '']
                } else {
                    this.currentParam.defaultValue = ''
                    this.dataFilter[this.paramIndex].defaultValue = ''
                }
            } else {
                if (listGroup.indexOf(this.currentParam.condition) > -1) {
                    if (!Array.isArray(this.currentParam.defaultValue)) {
                        this.currentParam.defaultValue = []
                        this.defaultValueMultiple = ''
                    }
                } else {
                    if (Array.isArray(this.currentParam.defaultValue)) {
                        this.currentParam.defaultValue = ''
                    }
                }
            }
        },
        defaultValueMultipleChange(value) {
            if (value) {
                this.currentParam.defaultValue = value.split(',')
            } else {
                this.currentParam.defaultValue = []
            }
        },
        handleCheckAllChange(isChange, index) {
            if (isChange) {
                this.current.tables[index].fields.forEach(item => {
                    item.checked = true
                })
            } else {
                this.current.tables[index].fields.forEach(item => {
                    item.checked = false
                })
            }
            this.current.tables[index].isIndeterminate = false
        },
        handleCheckedCitiesChange(index) {
            const checkedCount = this.current.tables[index].fields.filter(item => item.checked).length
            this.current.tables[index].checkAll = checkedCount === this.current.tables[index].fields.length
            this.current.tables[index].isIndeterminate = checkedCount > 0 && checkedCount < this.current.tables[index].fields.length
        },
        setDefaultCurrent() {
            if (!this.current && this.filterDatasets.length) {
                this.handleSelect(this.filterDatasets[0], false)
            }
        },
        /**
         * 获取字符串的md5
         * @param {*} arrayBuffer
         */
        getMd5(arrayBuffer) {
            const wordArray = CryptoJS.lib.WordArray.create(arrayBuffer)
            return CryptoJS.MD5(wordArray).toString()
        },
        /**
         * 设置数据缓存，用以比较数据是否有变化
         */
        setDataCache() {
            this.datasetCache = this.getMd5(JSON.stringify(this.current))
        },
        /**
         * 数据集是否有变化
         */
        isChange() {
            return this.current !== null && this.getMd5(JSON.stringify(this.current)) !== this.datasetCache
        }
    }
}
</script>

<style scoped lang="scss">
.designer-v2-dataset {
    width: 100%;
    height: 100%;
    display: flex;
    .dataset-relation {
        width: 200px;
        height: 100%;
        width: 200px;
        border-right: 1px #e8e8e8 solid;
        overflow: auto;
        ::v-deep .el-tabs__item {
          padding: 0;
        }
        ::v-deep .el-tabs__active-bar {
          width: 100px !important;
        }
        ::v-deep .el-tree-node__content {
          padding-left: 0 !important;
        }
        .custom-leaf {
          cursor: move;
          user-select: none;
          -webkit-user-select: none;
        }
        .relation-chart-filter {
          padding: 10px;
        }
        .relation-item {
            cursor: move;
            width: 100%;
            line-height: 20px;
            user-select: none;
            -webkit-user-select: none;
            padding: 0 10px;
        }
        .custom-list {
            cursor: move;
            width: 100%;
            line-height: 20px;
            user-select: none;
            -webkit-user-select: none;
            padding: 2px 10px;

            .custom-list-comment {
                display: flex;
                align-items: center;
                justify-content: flex-start;
                gap: 0 10px;
                .custom-list-comment-item {
                    color: #999;
                }
            }
        }
    }
    .dataset-select {
        flex: 1;
        height: 100%;
        width: 0;
        overflow-y: auto;
        display: flex;

        .dataset-empty {
          background-image: url('../../../../manager/components/x-spreadsheet/assets/table.svg');
          background-position: center;
          background-repeat: no-repeat;
          background-size: 300px 300px;
        }

        .dataset-parameter {
          display: flex;
          flex-direction: column;
          grid-gap: 10px;
          justify-content: flex-start;
          align-items: flex-start;
          width: 200px;
          border-left: 1px solid #DCDFE6;
          padding: 10px;
          .allowed {
            background-color: rgba(0,0,0,.1);
          }

          &.params-empty {
            background-image: url('../../../../manager/components/x-spreadsheet/assets/parameter.svg');
            background-position: center;
            background-repeat: no-repeat;
            background-size: 100% auto;
          }

          .parameter-title {
            font-size: 20px;
            font-weight: lighter;
            color: rgb(8, 137, 44);
          }

          .dataset-parameter-item {
            width: 180px;
            height: 32px;
            border-radius: 3px;
            padding: 5px 10px 5px 10px;
            font-size: 12px;
            line-height: 22px;
            border: 1px solid #DCDFE6;
            display: flex;
            align-items: center;
            .param-label {
              flex: 1;
              width: 0;
              overflow: hidden;
              height: 22px;
            }
            .param-icon {
              height: 12px;
              line-height: 12px;
              cursor: pointer;
              &+.param-icon {
                padding-left: 5px;
              }
            }
          }
        }

        .dataset-list {
          width: 100%;
          min-height: 56px;
          padding: 10px;
          display: flex;
          align-items: center;
          justify-content: flex-start;
          flex-wrap: wrap;
          gap: 10px;
          ::v-deep .el-tag{
            cursor: pointer;
          }
        }
        .dataset-body {
          flex: 1;
          width: 100%;
          height: 0;
          padding: 10px;
          padding-top: 0px;
          display: flex;
          flex-direction: column;
          .dataset-name {
            display: flex;
            align-items: center;
            & > .el-input {
              width: 260px;
            }
            padding-bottom: 10px;
            padding-left: 10px;
          }
          .dataset-tables {
            flex: 1;
            width: 100%;
            height: 0;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
            gap: 10px 0;

            .dataset-table {
              .dataset-table-name {
                width: 100%;
                height: 40px;
                line-height: 20px;
                font-size: 14px;
                padding: 10px;
                font-weight: bold;
                display: flex;
                align-items: center;
                justify-content: space-between;
              }
              .dataset-table-fields {
                padding: 10px;
                display:grid;
                grid-template-columns:repeat(auto-fill, 160px);
                grid-gap:10px;
                justify-content:space-between;
                .el-checkbox {
                  box-sizing: border-box;
                  width: 160px;
                  margin: 0 !important;
                  padding: 5px 10px;
                  ::v-deep .el-checkbox__input {
                    display: none;
                  }
                  ::v-deep .el-checkbox__label {
                    line-height: 22px;
                    width: 100%;
                    overflow: hidden;
                    padding-left: 0px;
                  }
                }
              }
              .dataset-table-fields ::first-line {
                  justify-content: flex-start;
                }
            }
          }
        }

        .dataset-preview-topbar {
          height: 36px;
          background-color: #eee;
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 0 10px;
          cursor: ns-resize;
        }
    }
  }
.mar-t-10 {
  margin-top: 10px;
}
::v-deep .el-dialog.param-dialog {
  .el-dialog__body {
    padding: 0 20px 20px 20px;
  }
}
.w-100 {
  width: 100%;
}
</style>

