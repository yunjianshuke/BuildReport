<template>
    <div class="relation-chart-config" :style="{ height: boxHeight + 'px', minHeight: boxMinHeight + 'px' }">
        <div class="resize-handle" @mousedown.prevent="startResize">
            <chart-config-topbar :datas="datas" :now-data="nowData" @collapse="onCollapse" @selectChange="onSelectChange" />
        </div>
        <div class="relation-chart-config-body">
            <div v-if="currentCell.type === 'node'" class="relation-chart-fields" :style="{ width: boxWidth + 'px', minWidth: boxMinWidth + 'px' }">
                <div class="field-label">
                    别名
                </div>
                <div>
                    <el-input v-model="currentCell.aliasName" size="mini" @change="handleAliasNameChange" />
                </div>
                <div class="field-label mar-t-20">
                    隐藏
                </div>
                <div>
                    <el-checkbox v-model="currentCell.hidden" size="mini">数据集中隐藏</el-checkbox>
                </div>
                <div class="field-label mar-t-20">
                    字段 <span style="font-size: 12px; color: red;">* 字段描述用于创建数据集，请认真填写，必填</span>
                </div>
                <el-table ref="fieldTable" size="mini" width="100%" :height="fieldsHeight" :data="fields[nowData] || []" border stripe fit>
                    <el-table-column 
                        prop="type"
                        label="类型"
                    />
                    <el-table-column 
                        prop="name"
                        label="字段名称"
                    />
                    <el-table-column 
                        prop="remark"
                        label="字段备注"
                    />
                    <el-table-column 
                        prop="alias"
                        label="字段描述"
                    >
                        <template slot-scope="scope">
                            <el-input v-model="scope.row.alias" size="mini" />
                        </template>
                    </el-table-column>
                    <el-table-column 
                        prop="defaultValue"
                        label="字段默认筛选"
                    >
                        <template slot-scope="scope">
                            <el-input v-model="scope.row.defaultValue" size="mini" />
                        </template>
                    </el-table-column>
                    <el-table-column 
                        prop="defaultValue"
                        label="字段关联缓存"
                    >
                        <template slot-scope="scope">
                            <el-button v-if="scope.row.dictConfId" type="text" size="mini" @click="openCache(scope.row, scope.$index)">{{ scope.row.dictName }}</el-button>
                            <el-button v-else type="text" size="mini" icon="el-icon-connection" @click="openCache(scope.row, scope.$index)" />
                        </template>
                    </el-table-column>
                </el-table>
                <div class="resize-handle-horizontal" @mousedown.prevent="startResizeHorizontal" />
            </div>
            <div v-if="currentCell.type === 'edge'" class="relation-chart-fields" :style="{ width: boxWidth + 'px', minWidth: boxMinWidth + 'px' }">
                <div class="field-label">
                    关联
                </div>
                <el-row type="flex" justify="start" align="middle">
                    <el-col :span="9" class="font-12">{{ currentCell.sourceNodeName }}</el-col>
                    <el-col :span="3" style="text-align: center;" class="font-12">运算符</el-col>
                    <el-col :span="9" class="font-12">{{ currentCell.targetNodeName }}</el-col>
                    <el-col :span="3" class="font-12" style="text-align: center;">操作</el-col>
                </el-row>
                <el-row v-for="item,index in currentCell.symbleList" :key="index" style="margin-top: 8px;" type="flex" justify="start" align="middle">
                    <el-col :span="9" style="text-align: center;">
                        <el-select v-model="item.sourceColumnName" size="mini" style="width: 100%" placeholder="选择字段" filterable @change="onSymbleChange">
                            <el-option v-for="sourceField in sourceFields" :key="sourceField.name" :label="sourceField.name" :value="sourceField.name">
                                <span style="float: left">{{ sourceField.name }}</span>
                                <span style="float: right; color: #8492a6; font-size: 13px">{{ sourceField.type }}</span>
                            </el-option>
                        </el-select>
                    </el-col>
                    <el-col :span="3" style="text-align: center;">=</el-col>
                    <el-col :span="9" style="text-align: center;">
                        <el-select v-model="item.targetColumnName" size="mini" style="width: 100%" placeholder="选择字段" filterable @change="onSymbleChange">
                            <el-option v-for="targetField in targetFields" :key="targetField.name" :label="targetField.name" :value="targetField.name">
                                <span style="float: left">{{ targetField.name }}</span>
                                <span style="float: right; color: #8492a6; font-size: 13px">{{ targetField.type }}</span>
                            </el-option>
                        </el-select>
                    </el-col>
                    <el-col :span="3" style="padding-left: 10px;"><i v-if="currentCell.symbleList.length !== 1" class="el-icon-remove-outline" style="margin-right: 4px;" @click="removeSymble(index)" /> <i v-if="index === currentCell.symbleList.length - 1" class="el-icon-circle-plus-outline" @click="addSymble" /></el-col>
                </el-row>
                <div class="font-12 mar-t-10">选择匹配字段以创建此关系</div>
                <div class="resize-handle-horizontal" @mousedown.prevent="startResizeHorizontal" />
            </div>
            <div v-if="currentCell.type === 'node'" class="relation-chart-preview">
                <el-table :data="previewData" size="mini" :height="previewHeight" empty-text="" border stripe fit>
                    <el-table-column
                        v-for="item in fields[nowData]"
                        :key="item.name"
                        :prop="item.name"
                        :label="item.name"
                    />
                </el-table>
                <div v-if="previewData.length === 0" class="preview-empty">
                    <div><el-button v-if="!isLoad" @click="loadPreviewData">加载预览数据</el-button><span v-else>暂无数据</span></div>
                </div>
            </div>
            <div v-else class="relation-chart-preview">
                <div class="relation-chart-preview-cannot">数据预览不可用</div>
            </div>
        </div>
        <BusinessDialog title="选择字段关联缓存" :visible.sync="cacheVisible" width="50%" footer content-height="auto">
            <template #body>
                <div class="cache-topbar"><div>{{ cache.label }} 关联缓存：</div><div class="cache-actions"><el-input v-model="cacheFilter" placeholder="请输入缓存名称" size="mini" clearable /><el-button size="mini" @click="handleClearSelect">清空选中</el-button></div></div> 
                <el-form v-model="cache" label-width="0px" class="cache-dialog-form">
                    <el-form-item label="">
                        <el-radio-group v-model="cache.dictConfId" size="mini" @change="handleCacheChange">
                            <el-radio v-for="item in cacheListFilter" :key="item.id" border :label="item.id">{{ item.dictName }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                </el-form>
            </template>
            <template #dialogBtns>
                <el-button type="primary" @click="cacheSubmit">确 定</el-button>
                <el-button @click="cacheVisible = false">取 消</el-button>
            </template>
        </BusinessDialog>
    </div>
</template>

<script>
import { Graph } from '@antv/x6'
import ChartConfigTopbar from './chartConfigTopbar.vue'
import BusinessDialog from '@/components/BusinessDialog/index.vue'

export default {
    name: 'ChartConfigPanel',
    components: {
        ChartConfigTopbar,
        BusinessDialog
    },
    props: {
        datas: {
            type: Array,
            default() {
                return []
            }
        },
        nowData: {
            type: String,
            default: ''
        },
        chartIns: {
            type: Graph,
            default() {
                return null
            }
        },
        fields: {
            type: Object,
            default() {}
        },
        getPreview: {
            type: String,
            default: ''
        }
    },
    data() {
        return {
            boxHeight: 300,
            boxMinHeight: 36,
            boxWidth: 800,
            boxMinWidth: 200,
            startY: 0,
            startX: 0,
            resizing: false,
            isLoads: {},
            previewDatas: {},
            cache: {
                dictConfId: '',
                dictName: '',
                label: ''
            },
            cacheVisible: false,
            cacheList: [],
            cacheFilter: ''
        }
    },
    computed: {
        // 当前选中的节点或者边的datas值
        currentCell() {
            return this.datas.find(item => { return item.id === this.nowData }) || {}
        },
        // 字段列表的高度
        fieldsHeight() {
            return this.boxHeight - 112 - 110
        },
        // 原始节点的字段列表
        sourceFields() {
            return this.currentCell.type === 'edge' ? this.fields[this.currentCell.sourceNodeId] : []
        },
        // 目标节点的字段列表
        targetFields() {
            return this.currentCell.type === 'edge' ? this.fields[this.currentCell.targetNodeId] : []
        },
        // 预览列表的高度
        previewHeight() {
            return this.boxHeight - 36 - 10
        },
        // 预览列表的值
        previewData() {
            return this.previewDatas[this.nowData] || []
        },
        // 预览列表是否加载
        isLoad() {
            return this.isLoads[this.nowData] || false
        },
        cacheListFilter() {
            if (this.cacheFilter) {
                return this.cacheList.filter(item => { return item.dictName.indexOf(this.cacheFilter) > -1 })
            } else {
                return this.cacheList
            }
        }
    },
    methods: {
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
     * 开始左右拖动
     * @param {*} event 
     */
        startResizeHorizontal(event) {
            this.resizing = true
            this.startX = event.clientX
            document.addEventListener('mousemove', this.doResizeHorizontal)
            document.addEventListener('mouseup', this.stopResizeHorizontal)
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
     * 左右拖动
     * @param {*} event 
     */
        doResizeHorizontal(event) {
            if (this.resizing) {
                const offset = event.clientX - this.startX
                this.boxWidth += offset
                if (this.boxWidth < this.boxMinWidth) {
                    this.boxWidth = this.boxMinWidth
                }
                this.startX = event.clientX
            }
        },
        /**
     * 上下拖动结束
     */
        stopResize() {
            this.resizing = false
            document.removeEventListener('mousemove', this.doResize)
            document.removeEventListener('mouseup', this.stopResize)
            this.$nextTick(() => {
                this.$emit('resize')
            })
        },
        /**
     * 左右拖动结束
     */
        stopResizeHorizontal() {
            this.resizing = false
            document.removeEventListener('mousemove', this.doResizeHorizontal)
            document.removeEventListener('mouseup', this.stopResizeHorizontal)
        },
        /**
     * 折叠
     * @param {*} isCollapse 
     */
        onCollapse(isCollapse) {
            if (isCollapse) {
                this.oldBoxHeight = this.boxHeight
                this.boxHeight = this.boxMinHeight
            } else {
                this.boxHeight = this.oldBoxHeight
            }
            this.$nextTick(() => {
                this.$emit('resize')
            })
        },
        /**
     * 修改选中的节点或边
     * @param {*} id 
     */
        onSelectChange(id) {
            this.$emit('selectChange', id)
        },
        /**
     * 关联两侧都选中字段后，移除边的感叹号
     * @param {*} id 
     */
        onSymbleChange() {
            let result = true
            this.currentCell.symbleList.forEach(item => {
                if (!item.sourceColumnName || !item.targetColumnName) {
                    result = false
                }
            })
            if (result) {
                this.$emit('joined', this.currentCell.id)
            }
        },
        /**
     * 加载预览数据
     */
        loadPreviewData() {
            this.$ajax({
                url: `${this.getPreview}/${this.currentCell.datasourceId}/${this.currentCell.tableName}`,
                methods: 'get',
                data: {}
            }).then(res => {
                if (res.code === 0) {
                    this.$set(this.previewDatas, this.nowData, res.data)
                    this.$set(this.isLoads, this.nowData, true)
                } 
            })
        },
        scrollTo(name) {
            const index = this.fields[this.nowData].findIndex(item => { return item.name === name })
            const tableBodyWrapper = this.$refs.fieldTable.$el.querySelector('.el-table__body-wrapper')
            if (tableBodyWrapper) {
                const row = tableBodyWrapper.querySelector(`.el-table__row:nth-child(${index + 1})`)
                if (row) {
                    const { top } = row.getBoundingClientRect()
                    const { top: tableTop } = tableBodyWrapper.getBoundingClientRect()
                    tableBodyWrapper.scrollTop += top - tableTop
                }
            }
        },
        addSymble() {
            this.currentCell.symbleList.push({
                sourceColumnName: '',
                targetColumnName: ''
            })
        },
        removeSymble(index) {
            this.currentCell.symbleList.splice(index, 1)
        },
        handleAliasNameChange() {
            this.$emit('aliasnamechange')
        },
        openCache(row, index) {
            this.cacheCurrent = row
            this.cacheIndex = index
            this.cache = {
                dictName: row.dictName,
                dictConfId: row.dictConfId,
                label: row.name
            }
            this.getCacheList()
            this.cacheVisible = true
        },
        cacheSubmit() {
            this.$set(this.fields[this.nowData][this.cacheIndex], 'dictName', this.cache.dictName)
            this.$set(this.fields[this.nowData][this.cacheIndex], 'dictConfId', this.cache.dictConfId)
            this.cacheVisible = false
        },
        getCacheList() {
            if (this.cacheList.length === 0) {
                this.$ajax({
                    url: '/reportDict/getList',
                    methods: 'get',
                    data: {}
                }).then(res => {
                    if (res.code === 0) {
                        this.cacheList = res.data || [] 
                    } 
                })
            }
        },
        handleCacheChange(value) {
            const finded = this.cacheList.find(item => { return item.id === value })
            this.cache.dictName = finded.dictName
        },
        handleClearSelect() {
            this.cache.dictConfId = ''
            this.cache.dictName = ''
        }
    }
}
</script>
<style scoped lang="scss">
.relation-chart-config {
  width: 100%;
  background: white;
  position: relative;
  overflow: hidden;
  
  .resize-handle {
    cursor: ns-resize;
  }

  .relation-chart-config-body {
    height: calc(100% - 36px);
    display: flex;
    align-items: flex-start;
    justify-content: flex-start;
    overflow: auto;
    .relation-chart-fields {
      position: relative;
      min-height: 100%;
      padding: 10px;
      .field-label {
        font-weight: bold;
        margin-bottom: 10px;
      }
      .mar-t-20 {
        margin-top: 20px;
      }
      .mar-t-10 {
        margin-top: 10px;
      }
      .resize-handle-horizontal {
        width: 1px;
        cursor: ew-resize;
        background-color: #999;
        height: 100%;
        position: absolute;
        top: 0;
        right: 0;
      }
    }
    .relation-chart-preview {
      position: relative;
      flex: 1;
      height: 100%;
      width: 0;
      .preview-empty {
        position: absolute;
        padding-top: 60px;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        & > div {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: #FFFFFF;
        }
      }

      .relation-chart-preview-cannot {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
    
  }

  .font-12 {
    font-size: 12px;
  }

}
.cache-topbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    .cache-actions {
        display: flex;
        gap: 10px;
    }
}
.cache-dialog-form {
  .el-radio-group {
    padding-top: 10px;
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    gap:10px;
  }
  .el-radio {
      box-sizing: border-box;
      margin: 0 !important;
      padding: 5px 10px;
      ::v-deep .el-radio__input {
          display: none;
      }
      line-height: normal;
      ::v-deep .el-radio__label {
          width: 100%;
          overflow: hidden;
          padding-left: 0px;
      }
  }
}
</style>
