<template>
    <div class="designer-v2-filter-container">
        <div class="form-field">
            <!-- <div class="field-title">筛选条件组件</div>
            <div class="form-component">
                <div v-for="item in inputComponent" :key="item.key">
                    <drag
                        :transfer-data="item"
                        @dragstart="dragging = 'component'"
                        @dragend="dragging = null"
                    >
                        <el-button class="filter-button" type="primary" plain size="small">{{ item.alias }}</el-button>
                    </drag>
                </div>
            </div> -->
            <div class="field-title">数据筛选条件字段</div>
            <div class="form-component">
                <el-tree
                    ref="customTree"
                    class="filter-tree"
                    :data="dataFilters"
                    :props="defaultProps"
                    default-expand-all
                >
                    <template slot-scope="{ node }">
                        <drag
                            :transfer-data="node.data"
                            @dragstart="dragging = 'field'"
                            @dragend="dragging = null"
                        >
                            <div :class="{'custom-leaf': node.level !== 1}" :title=" node.level !== 1 ? node.data.name + '\n' + node.label: node.label">
                                <i v-if="node.level !== 1" class="el-icon-rank" /> {{ node.label }}
                            </div>
                        </drag>
                    </template>
                </el-tree>
            </div>
        </div>
        <div class="form-designer">
            <div class="top-bar">
                <el-button size="mini" @click="handlePreview">预览</el-button>
                <el-button type="primary" size="mini" @click="handleSave">保存</el-button>
            </div>
            <drop
                class="component-list"
                :class="{ 'component-empty': filterDatasets.length === 0 }"
                @drop="onComponentDragEnd"
            >
                <el-table v-if="filterDatasets.length" ref="datasetTable" :data="filterDatasets" highlight-current-row @row-click="handleCurrentChange">
                    <el-table-column label="组件标题" prop="">
                        <template slot-scope="scope"><el-input v-model="scope.row.component.label" /></template>
                    </el-table-column>
                    <el-table-column label="筛选组件" prop="">
                        <template slot-scope="scope"><el-select v-model="scope.row.component.type"><el-option v-for="item, subIndex in scope.row.fields[0].components" :key="'conponent' + scope.$index + subIndex" :label="item.alias" :value="item.key" /></el-select></template>
                    </el-table-column>
                    <el-table-column label="筛选字段">
                        <template slot-scope="scope">
                            <drop
                                style="width: 100%; padding: 10px; border: 2px dotted #CCCCCC; border-radius: 4px; display: flex; justify-content: flex-start; align-items: center; gap: 10px; flex-wrap: wrap ;"
                                @dragleave="(Drag, Drop) => { Drop.target.style.background = '' }"
                                @dragover="handleFieldDragover('field', ...arguments)"
                                @drop="(transferData, nativeEvent) => { onFieldDragEnd(transferData, scope.$index, nativeEvent) }"
                            >
                                {{ scope.row.fields.length === 0 ? '拖拽筛选字段到此，关联组件' : '' }}
                                <el-tag
                                    v-for="(tag, tabIndex) in scope.row.fields"
                                    :key="tag.alias + scope.$index + tabIndex"
                                    closable
                                    @close="removeFields(scope.$index, tabIndex)"
                                >
                                    {{ tag.alias }}
                                </el-tag>
                            </drop>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120">
                        <template slot-scope="scope">
                            <el-button type="text" :disabled="!scope.row.component || !scope.row.component.type" @click="handleDelete(scope.$index)">移除组件</el-button>
                            <el-button type="text" :disabled="!scope.row.component || !scope.row.component.type || ['radio', 'checkbox', 'select'].indexOf(scope.row.component.type) === -1" @click="handleOption(scope.$index)">配置</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </drop>
            <div v-if="isPreview" class="preview-container">
                <div class="preview-container-topbar">
                    预览
                    <div>
                        <el-button type="text" icon="el-icon-close" @click="handlePreviewClose" />
                    </div>
                </div>
                <DataFilter key="FormPreview" :params="previewList" />
            </div>
        </div>
        <div v-if="optionVisible" class="form-component-config">
            <div class="config-title">筛选组件属性 <i class="el-icon-close" @click="optionVisible = false" /></div>
            <el-form v-if="current && current.component && current.component.type" label-position="top" label-width="80px">
                <!-- <el-form-item label="组件标题">
                    <el-input v-model="current.component.label" clearable />
                </el-form-item> -->
                <template v-if="componentGroup.indexOf(current.component.type) > -1">
                    <el-form-item label="数据类型">
                        <el-select v-model="current.component.options.type" class="w-100" clearable>
                            <el-option v-for="item in componentSource" :key="item.key" :label="item.name" :value="item.key" />
                        </el-select>
                    </el-form-item>
                    <div v-if="current.component.options.type === 'final'">
                        <el-button type="text" @click="addFinalData">添加</el-button>
                        <el-row :gutter="2" type="flex" align="middle" justify="space-between">
                            <el-col :span="9">显示值</el-col>
                            <el-col :span="9">实际值</el-col>
                            <el-col :span="6" />
                        </el-row>
                        <el-row v-for="(item, index) in current.component.options.data" :key="'optionData' + index" :gutter="2" type="flex" align="middle" justify="space-between">
                            <el-col :span="9">
                                <el-input v-model="item.label" size="mini" placeholder="输入显示值" />
                            </el-col>
                            <el-col :span="9">
                                <el-input v-model="item.value" size="mini" placeholder="输入实际值" />
                            </el-col>
                            <el-col :span="6"><el-button type="text" @click="deleteFinalData(index)">移除</el-button></el-col>
                        </el-row>
                    </div>
                    <div v-if="current.component.options.type === 'dataset'" class="form-item-label"><span>筛选数据来源</span></div>
                    <el-form-item v-if="current.component.options.type === 'dataset'" label="">
                        <el-select v-model="current.component.options.dataset" class="w-100" clearable @change="onChangeOptionDataset">
                            <el-option
                                v-for="item in selectDatasets"
                                :key="item.id"
                                :label="item.name"
                                :value="item.id"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item v-if="current.component.options.type === 'dataset'" label="显示值">
                        <el-select v-model="current.component.options.label" class="w-100" clearable @change="onChangeOptionLabel">
                            <el-option v-for="item in optionsFields" :key="item.key" :label="item.alias" :value="item.key" />
                        </el-select>
                    </el-form-item>
                    <el-form-item v-if="current.component.options.type === 'dataset'" label="实际值">
                        <el-select v-model="current.component.options.value" class="w-100" clearable @change="onChangeOptionValue">
                            <el-option v-for="item in optionsFields" :key="item.key" :label="item.alias" :value="item.key" />
                        </el-select>
                    </el-form-item>
                </template>
            </el-form>
        </div>
    </div>
</template>
<script>
import DataFilter from '../data-filter/index'
import { componentList, dateGroup, intGroup, listGroup, expressionMap, componentSource } from '../const/index'
import { cloneDeep } from 'lodash'
export default {
    name: 'DesignerForm',
    components: {
        DataFilter
    },
    props: {
        datasets: {
            type: Array,
            default() {
                return []
            }
        },
        filterComponents: {
            type: Array,
            default() {
                return []
            }
        }
    },
    data() {
        return {
            dragging: null,
            isPreview: false,
            filterDatasets: [],
            previewList: [],
            inputComponent: componentList.map(item => {
                item.group = 'component'
                return item
            }),
            dataTypeOptions: [
                { label: '常量', value: '0' },
                { label: '数据集', value: '1' }
            ],
            currentIndex: -1,
            current: null,
            defaultProps: {
                children: 'dataFilter',
                label: 'label'
            },
            componentGroup: ['select', 'radio', 'checkbox'],
            defaultValueMultiple: '',
            componentSource,
            optionsFields: [],
            selectDatasets: [],
            dataFilters: [],
            optionVisible: false
        }
    },
    methods: {
        init() {
            this.filterDatasets = cloneDeep(this.filterComponents)
            this.initDataFilters()
            if (Array.isArray(this.filterDatasets) && this.filterDatasets.length === 0) {
                this.dataFilters.forEach(dataset => {
                    dataset.dataFilter.forEach(field => {
                        this.pushField(field)
                    })
                })
            }
            const relationIds = this.datasets.filter(item => { return item.businessType === '0' }).map(item => item.relationId)
            Promise.all(relationIds.map(id => {
                return this.$ajax({
                    url: '/reportInfo/getDataSetPage',
                    method: 'post',
                    data: {
                        relationId: id,
                        businessType: '1',
                        current: 1,
                        size: 999,
                        select: 1
                    }
                })
            })).then(res => {
                this.selectDatasets = res.reduce((all, item) => {
                    all.push(...item.data.records)
                    return all
                }, [])
            })
        },
        initDataFilters() {
            this.dataFilters = this.datasets.filter(item => { return item.businessType === '0' }).map((item, index) => {
                return {
                    datasetName: item.datasetName,
                    label: item.datasetName,
                    dataFilter: item.dataFilter.map((subItem, subIndex) => {
                        const components = this.componentList(subItem.type, subItem.condition)
                        let conditionName = ''
                        const finded = expressionMap[subItem.type].find(item => item.key === subItem.condition)
                        if (subItem.condition && finded) {
                            conditionName = finded.name
                        }
                        return {
                            ...subItem,
                            group: 'field',
                            label: subItem.alias,
                            datasetIndex: index,
                            fieldIndex: subIndex,
                            conditionName: conditionName,
                            components,
                            componentsName: components.map(item => item.alias).join('、')
                        }
                    })
                }
            })
        },
        /**
         * 拖拽业务关系
         * @param {*} group
         * @param {*} data
         * @param {*} event
         */
        handleComponentDragover(group, data, event) {
            if (group && data && group === 'component') {
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
            event.target.style.background = 'rgba(0, 0, 0, 0.3)'
        },
        componentList(type, condition) {
            if (intGroup.indexOf(type) > -1) {
                return componentList.slice(0, 1)
            } else if (dateGroup.indexOf(type) > -1) {
                return componentList.slice(4, 5)
            } else {
                if (listGroup.indexOf(condition) > -1) {
                    return [...componentList.slice(1, 2), ...componentList.slice(3, 4)]
                } else {
                    return componentList.slice(0, 3)
                }
            }
        },
        onComponentDragEnd(item, event) {
            event.stopPropagation()
            if (item.group === 'component') {
                this.filterDatasets.push({
                    component: {
                        type: item.key,
                        typeName: item.alias,
                        label: '',
                        value: null,
                        options: {
                            data: [{ label: '', value: '' }],
                            type: '',
                            dataset: '',
                            relationId: '',
                            label: '',
                            labelName: '',
                            labelCellId: '',
                            value: '',
                            valueName: '',
                            valueCellId: '',
                            labelAliasName: '',
                            valueAliasName: '',
                            multiple: false
                        }
                    },
                    fields: []
                })
            } else {
                if (this.filterDatasets.some(row => { return row.fields.some(field => { return field.key === item.key && field.datasetName === item.datasetName }) })) {
                    this.$message.warning('不可重复关联字段')
                    return
                }
                this.pushField(item)
            }
        },
        onFieldDragEnd(item, dataFilterIndex, event) {
            event.stopPropagation()
            event.target.style.background = ''

            if (!item.components.some(com => { return com.key === this.filterDatasets[dataFilterIndex].component.type })) {
                this.$message.warning(`字段仅支持组件：${item.componentsName}`)
                return
            }

            if (this.filterDatasets.some(dataset => {
                return dataset.fields.some(field => {
                    return field.key === item.key && field.datasetName === item.datasetName
                })
            })) {
                this.$message.warning('不可重复关联字段')
                return
            }

            if (this.filterDatasets[dataFilterIndex].type && this.filterDatasets[dataFilterIndex].type !== item.type) {
                this.$message.warning('日期控件绑定字段的类型不一致')
                return
            }

            if (!this.filterDatasets[dataFilterIndex].component.label) {
                this.filterDatasets[dataFilterIndex].component.label = `${item.alias}：`
            }
            this.filterDatasets[dataFilterIndex].fields.push(item)

            if (this.filterDatasets[dataFilterIndex].component.type === 'datetime') {
                this.filterDatasets[dataFilterIndex].type = item.type
            }

            if (this.filterDatasets[dataFilterIndex].component.type === 'checkbox' || (this.filterDatasets[dataFilterIndex].component.type === 'select' && listGroup.indexOf(item.condition) > -1)) {
                this.filterDatasets[dataFilterIndex].component.value = []
            } else {
                this.filterDatasets[dataFilterIndex].component.value = ''
            }

            if (dateGroup.indexOf(item.type) > -1) {
                if (item.condition === 'BETWEEN') {
                    this.filterDatasets[dataFilterIndex].component.value = ['', '']
                } else {
                    this.filterDatasets[dataFilterIndex].component.value = ''
                }
            } else {
                if (listGroup.indexOf(item.condition) > -1) {
                    this.filterDatasets[dataFilterIndex].component.options.multiple = true
                    if (!Array.isArray(this.filterDatasets[dataFilterIndex].component.value)) {
                        this.filterDatasets[dataFilterIndex].component.value = []
                    }
                } else {
                    this.filterDatasets[dataFilterIndex].component.options.multiple = false
                    if (Array.isArray(this.filterDatasets[dataFilterIndex].component.value)) {
                        this.filterDatasets[dataFilterIndex].component.value = ''
                    }
                }
            }

            this.$refs.datasetTable.setCurrentRow(this.filterDatasets[dataFilterIndex])
            this.current = this.filterDatasets[dataFilterIndex]
        },
        handlePreview() {
            this.previewList = this.filterDatasets.reduce((all, item) => {
                all.push(item)
                return all
            }, [])
            this.isPreview = true
        },
        handlePreviewClose() {
            this.isPreview = false
        },
        handleSave() {
            if (this.filterDatasets.some(dataset => {
                return dataset.fields.length === 0
            })) {
                this.$message.warning('请把所有筛选组件关联字段')
                return
            }
            this.$emit('save', this.filterDatasets)
        },
        handleDelete(dataFilterIndex) {
            this.filterDatasets.splice(dataFilterIndex, 1)
            this.$refs.datasetTable.setCurrentRow()
            this.current = null
        },
        removeFields(dataFilterIndex, tabIndex) {
            if (this.filterDatasets[dataFilterIndex].fields.length === 1) {
                this.filterDatasets.splice(dataFilterIndex, 1)
            } else {
                this.filterDatasets[dataFilterIndex].fields.splice(tabIndex, 1)
            }
        },
        handleCurrentChange(val) {
            this.$set(this, 'current', val)
            if (val && val.component && val.component.options && val.component.options.dataset) {
                this.onChangeOptionDataset(val.component.options.dataset)
            }
        },
        /**
         * 筛选下载框的数据源类型为常量时，新增常量
         */
        addFinalData() {
            this.current.component.options.data.push({
                label: '',
                value: ''
            })
        },
        /**
         * 筛选下载框的数据源类型为常量时，删除常量
         */
        deleteFinalData(index) {
            if (this.current.component.options.data.length === 1) {
                this.$message.warning('至少有一个下拉选项')
                return
            }
            this.current.component.options.data.splice(index, 1)
        },
        /**
         * 筛选下载框的数据源类型改变
         */
        onChangeOptionDataset(id) {
            const finded = this.selectDatasets.find(dataset => { return dataset.id === id })
            if (finded) {
                const [dataset] = JSON.parse(finded.fieldJsonArray)
                this.optionsFields = dataset.sqlFieldList.map(item => {
                    return {
                        ...item,
                        aliasName: dataset.aliasName,
                        cellId: dataset.cellId,
                        key: `${item.name}`
                    }
                })
                this.current.component.options.relationId = finded.relationId
            }
        },
        /**
         * 设置数据集选中字段别名
         * @param {*} key
         */
        onChangeOptionLabel(key) {
            const finded = this.optionsFields.find(field => { return field.key === key })
            if (finded) {
                this.current.component.options.labelAliasName = finded.aliasName
                this.current.component.options.labelName = finded.name
                this.current.component.options.labelCellId = finded.cellId
            }
        },
        /**
         * 设置数据集选中字段别名
         * @param {*} key
         */
        onChangeOptionValue(key) {
            const finded = this.optionsFields.find(field => { return field.key === key })
            if (finded) {
                this.current.component.options.valueAliasName = finded.aliasName
                this.current.component.options.valueName = finded.name
                this.current.component.options.valueCellId = finded.cellId
            }
        },
        toFormDataset() {
            this.$emit('toformdataset')
        },
        handleOption(index) {
            this.$refs.datasetTable.setCurrentRow(this.filterDatasets[index])
            this.current = this.filterDatasets[index]
            this.optionVisible = true
        },
        pushField(field) {
            const newData = {
                component: {
                    type: field.components[0].key,
                    typeName: field.components[0].alias,
                    label: '',
                    value: null,
                    options: {
                        data: [{ label: '', value: '' }],
                        type: '',
                        dataset: '',
                        relationId: '',
                        label: '',
                        labelName: '',
                        labelCellId: '',
                        value: '',
                        valueName: '',
                        valueCellId: '',
                        labelAliasName: '',
                        valueAliasName: '',
                        multiple: false
                    }
                },
                fields: [field]
            }

            if (!newData.component.label) {
                newData.component.label = `${field.alias}：`
            }

            if (newData.component.type === 'datetime') {
                newData.type = field.type
            }

            if (newData.component.type === 'checkbox' || (newData.component.type === 'select' && listGroup.indexOf(field.condition) > -1)) {
                newData.component.value = []
            } else {
                newData.component.value = ''
            }

            if (dateGroup.indexOf(field.type) > -1) {
                if (field.condition === 'BETWEEN') {
                    newData.component.value = ['', '']
                } else {
                    newData.component.value = ''
                }
            } else {
                if (listGroup.indexOf(field.condition) > -1) {
                    newData.component.options.multiple = true
                    if (!Array.isArray(newData.component.value)) {
                        newData.component.value = []
                    }
                } else {
                    newData.component.options.multiple = false
                    if (Array.isArray(newData.component.value)) {
                        newData.component.value = ''
                    }
                }
            }

            this.filterDatasets.push(newData)
        }
    }
}
</script>
<style scoped lang="scss">
.designer-v2-filter-container {
  width: 100%;
  height: 100%;
  display: flex;
  .form-field {
    width: 200px;
    height: 100%;
    border-right: 1px #e8e8e8 solid;
    flex-shrink: 0;
    overflow-y: auto;
    padding: 10px;
    .form-component {
      margin-top: 10px;
      width: 179px;
      display: flex;
      flex-direction: column;
      gap: 10px 0;
      .filter-tree {
        ::v-deep .el-tree-node__content {
          padding-left: 0 !important;
        }
      }
      .filter-button {
        width: 180px;
      }
    }
  }
  .form-designer {
    flex: 1;
    width: 0;
    height: 100%;
    display: flex;
    flex-direction: column;
    .top-bar {
      width: 100%;
      padding: 10px;
      display: flex;
      justify-content: flex-end;
    }
    .component-list {
      flex: 1;
      width: 100%;
      padding: 0 10px;
      height: 0;
      overflow-y: auto;
      &.component-empty {
        background-image: url('../x-spreadsheet/assets/form.svg');
        background-position: center;
        background-repeat: no-repeat;
        background-size: 300px 300px;
      }

      .form-item-delete-btn {
        line-height: 32px;
        cursor: pointer;
      }

      .form-item-delete-btn:hover {
        color: #409EFF;
      }
      .div-form-item {
        margin-bottom: 10px;
        padding: 10px;
        border-radius: 2px;
        border: 1px solid #d9ecff;
        display: flex;
      }

      .div-form-item:hover {
        border-color: #67C23A;
      }

      .form-item-active{
        border-color: #409EFF;
      }
    }
    .preview-container {
      position: relative;
      min-height: 136px;
      .preview-container-topbar {
        height: 36px;
        background-color: #eee;
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 10px;
      }
      .data-filter {
        top: 36px;
      }
    }
  }
  .form-component-config {
    width: 200px;
    flex-shrink: 0;
    border-left: 1px #e8e8e8 solid;
    position: relative;
    height: 100%;
    overflow-y: auto;
    padding: 10px;
  }
  .config-title,.field-title {
    font-size: 20px;
    font-weight: lighter;
    color: rgb(8, 137, 44);
  }
  .config-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    i {
        color: #000;
        cursor: pointer;
    }
  }
  .m-t-20 {
    margin-top: 20px;
  }

  .filter-tree {
    width: 100%;
    overflow-x: hidden;
  }

  .form-item-label {
    display: flex;
    width: 100%;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 10px;
  }
}
</style>
