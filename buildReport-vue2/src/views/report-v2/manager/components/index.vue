<template>
    <div v-loading="loading" class="report-designer-v2">
        <div class="designer-v2-body">
            <designer-tree :data="datasetsTree" @adddataset="onAddDataset" @resize="onSheetRerender" />
            <div class="designer-v2-center">
                <div class="designer-v2-main">
                    <div ref="designerV2Main" class="designer-v2-main-ins" />
                    <div v-if="isPanel" class="designer-v2-panel">
                        <div class="panel-title">单元格属性</div>
                        <ul v-if="isPanelList" class="panel-list">
                            <li class="panel-list-item">
                                <h4>数据类型</h4>
                                <section>
                                    <div class="panel-list-label">类型：</div>
                                    <div class="panel-list-value">
                                        <!-- <el-select v-if="type === 0 || type === 7" v-model="type" placeholder="请选择" size="mini">
                      <el-option v-for="item in typeStaticList" :key="item.value" :label="item.label" :value="item.value"></el-option>
                    </el-select> -->
                                        <span v-if="type === 0">常量</span>
                                        <span v-else-if="type === 7">序号</span>
                                        <span v-else-if="type === 5">图片</span>
                                        <el-select v-else v-model="type" placeholder="请选择" size="mini">
                                            <el-option
                                                v-for="item in typeList" :key="item.value" :label="item.label"
                                                :value="item.value"
                                            />
                                        </el-select>
                                    </div>
                                </section>
                                <section v-if="type === 3">
                                    <div class="panel-list-label" />
                                    <div class="panel-list-value">
                                        <el-select v-model="summary" placeholder="请选择" size="mini">
                                            <el-option
                                                v-for="item in summaryList" :key="item.value" :label="item.label"
                                                :value="item.value"
                                            />
                                        </el-select>
                                    </div>
                                </section>
                                <section>
                                    <div class="panel-list-label">扩展：</div>
                                    <div class="panel-list-value">
                                        <div v-if="type === 0 || type === 3 || type === 4 || type === 5">无</div>
                                        <el-select
                                            v-else v-model="expand" placeholder="请选择" size="mini"
                                            :disabled="type === 5"
                                        >
                                            <el-option
                                                v-for="item in expandList" :key="item.value" :label="item.label"
                                                :value="item.value"
                                            />
                                        </el-select>
                                    </div>
                                </section>
                            </li>
                            <li class="panel-list-item">
                                <h4>上下文</h4>
                                <section>
                                    <div class="panel-list-label">水平：</div>
                                    <div class="panel-list-value">
                                        <el-select v-model="context[0]" placeholder="请选择" size="mini">
                                            <el-option
                                                v-for="item in contextList" :key="item.value" :label="item.label"
                                                :value="item.value"
                                            />
                                        </el-select>
                                    </div>
                                </section>
                                <section v-if="context[0] === 2">
                                    <div class="panel-list-label">{{ customAxisX }}</div>
                                    <div class="panel-list-value">
                                        <i
                                            :class="{ 'el-icon-aim': true, active: isCustomAxisX }"
                                            @click="handleCustomAxis('X')"
                                        />
                                    </div>
                                </section>
                                <section>
                                    <div class="panel-list-label">垂直：</div>
                                    <div class="panel-list-value">
                                        <el-select v-model="context[1]" placeholder="请选择" size="mini">
                                            <el-option
                                                v-for="item in contextList" :key="item.value" :label="item.label"
                                                :value="item.value"
                                            />
                                        </el-select>
                                    </div>
                                </section>
                                <section v-if="context[1] === 2">
                                    <div class="panel-list-label">{{ customAxisY }}</div>
                                    <div class="panel-list-value">
                                        <i
                                            :class="{ 'el-icon-aim': true, active: isCustomAxisY }"
                                            @click="handleCustomAxis('Y')"
                                        />
                                    </div>
                                </section>
                            </li>
                            <li class="panel-list-item">
                                <h4>格式</h4>
                                <section>
                                    <div class="panel-list-label">格式化：</div>
                                    <div class="panel-list-value">
                                        <el-select
                                            v-model="format" placeholder="请选择" size="mini" clearable
                                            :disabled="type === 5"
                                        >
                                            <el-option
                                                v-for="item in formatList" :key="item.value" :label="item.label"
                                                :value="item.value"
                                            />
                                        </el-select>
                                    </div>
                                </section>
                            </li>
                            <li class="panel-list-item">
                                <h4>是否开启分组小计</h4>
                                <section>
                                    <div class="panel-list-value">
                                        <el-switch v-model="groupTotal" size="mini" />
                                    </div>
                                </section>
                            </li>
                        </ul>
                    </div>
                </div>
                <report-preview-panel
                    ref="reportPreview" :preview-url="previewUrl" @close="handlePreviewClose"
                    @resize="onSheetRerender"
                />
            </div>
        </div>
        <div v-show="tab === 'form'" class="designer-v2-form">
            <DesignerForm ref="form" :datasets="datasets" :filter-components="filterComponents" @save="filterSave" @toformdataset="toFormDataset" />
        </div>
        <bottom-bar ref="bottomBar" :list="tabs" :value.sync="tab" :valid-change="validChange" @change="handleBottomBarChange" />
        <div v-if="isHelpPopup" ref="helpPopup" class="help-popup">
            <div>类型：{{ typeText }}</div>
            <div>集：{{ dataset }}</div>
            <div>表：{{ aliasName }}</div>
            <div>列：{{ text }}</div>
        </div>
        <save-dialog ref="reportSaveDialog" @saved="onSaved" />
        <link-dialog ref="reportLinkDialog" @change="onLinkChange" />
        <image-dialog ref="reportImageDialog" @change="onImageChange" />
        <expression-dialog ref="reportExpressionDialog" @change="onExpressionChange" />
        <guide v-if="visibleGuide" @close="handleCloseGuide" />
        <BusinessDialog
            title="选择数据集"
            :visible.sync="datasetVisible"
            top="10vh"
            content-height="50vh"
            width="30%"
            :close-on-click-modal="true"
            append-to-body
            :before-close="() => { visible = false }"
            :footer="true"
            :options="formSet"
            @click="() => { visible = false }"
            @update:visible="() => { visible = false }"
        >
            <template #body>
                <div v-loading="datasetLoading">
                    <el-input v-model="filterText" placeholder="输入关键字进行过滤" />
                    <el-tree ref="datasetTree" :data="datasetTree" :default-checked-keys="selectedDataset" default-expand-all node-key="id" show-checkbox :props="defaultProps" empty-text="无数据" :check-on-click-node="true" :expand-on-click-node="false" :highlight-current="true" :filter-node-method="filterNode" />
                </div>
            </template>
        </BusinessDialog>
    </div>
</template>
<script>
import Spreadsheet from './x-spreadsheet/src'
import zhCn from './x-spreadsheet/src/locale/zh-cn'
import { xtoast } from './x-spreadsheet/src/component/message'
import { xy2expr, expr2xy } from './x-spreadsheet/src/core/alphabet'
import { cssPrefix } from './x-spreadsheet/src/config'
import { cloneDeep} from 'lodash'
Spreadsheet.locale('zh-cn', zhCn)
import BottomBar from './bottom-bar/index'
import DesignerForm from './form/index'
import ReportPreviewPanel from './preview/panel'
import SaveDialog from './save-dialog/index'
import LinkDialog from './link-dialog/index'
import ImageDialog from './image-dialog/index'
import ExpressionDialog from './expression-dialog/index'
import Guide from './guide/index'
import DesignerTree from './tree/index'
import CryptoJS from 'crypto-js'
import BusinessDialog from '@/components/BusinessDialog/index.vue'

export default {
    name: 'ReportDesignerV2',
    components: {
        BottomBar,
        ReportPreviewPanel,
        SaveDialog,
        LinkDialog,
        ImageDialog,
        ExpressionDialog,
        DesignerForm,
        Guide,
        DesignerTree,
        BusinessDialog
    },
    props: {
        reportId: {
            type: String,
            default: ''
        },
        previewUrl: {
            type: String,
            default: '/reportInfo/preview/sync '
        },
        detailUrl: {
            type: String,
            default: '/reportInfo/info'
        }
    },
    data() {
        return {
            loading: false,
            tab: 'designer',
            datasets: [],
            filterComponents: [],
            defaultProps: {
                label: 'label',
                children: 'children'
            },
            hHeadHeight: 25,
            vHeadWidth: 60,
            isMove: false,
            isDown: false,
            type: 0,
            expand: 0,
            dataset: '',
            aliasName: '',
            text: '',
            typeText: '分组',
            typeList: [
                { value: 1, label: '分组' },
                { value: 2, label: '列表' }
            ],
            typeStaticList: [
                { value: 0, label: '静态' },
                { value: 7, label: '序号' }
            ],
            summaryList: [
                { value: 'Sum', label: '求和' },
                { value: 'Avg', label: '平均值' },
                { value: 'Max', label: '最大值' },
                { value: 'Min', label: '最小值' },
                { value: 'Count', label: '计数' }
            ],
            expandList: [
                { value: 0, label: '无' },
                { value: 1, label: '纵向' },
                { value: 2, label: '横向' }
            ],
            contextList: [
                { value: 0, label: '无' },
                { value: 1, label: '默认' },
                { value: 2, label: '自定义' }
            ],
            formatList: [
                { value: 'yyyy', label: 'yyyy' },
                { value: 'yyyy-MM', label: 'yyyy-MM' },
                { value: 'yyyy-MM-dd', label: 'yyyy-MM-dd' },
                { value: 'yyyy-MM-dd HH:mm:ss', label: 'yyyy-MM-dd HH:mm:ss' },
                { value: 'yyyy/MM', label: 'yyyy/MM' },
                { value: 'yyyy/MM/dd', label: 'yyyy/MM/dd' },
                { value: 'yyyy年MM月dd日', label: 'yyyy年MM月dd日' },
                { value: 'yyyy年MM月dd日 HH:mm:ss', label: 'yyyy年MM月dd日 HH:mm:ss' },
                { value: 'HH:mm', label: 'HH:mm' },
                { value: 'HH:mm:ss', label: 'HH:mm:ss' }
            ],
            context: [1, 1],
            customAxisX: 'A1',
            customAxisY: 'A1',
            format: '',
            isCustomAxisX: false,
            isCustomAxisY: false,
            isHelpPopup: false,
            isPanel: true,
            isPanelList: false,
            summary: 'Sum',
            originData: {},
            visibleGuide: false,
            tabs: [
                {
                    label: '设计器',
                    value: 'designer'
                },
                {
                    label: '筛选器',
                    value: 'form'
                }
            ],
            formSet: {
                sureBtn: '保存',
                cancelBtn: '取消',
                confirm: this.datasetConfirm
            },
            selectedDataset: [],
            datasetTree: [],
            datasetVisible: false,
            filterText: '',
            datasetLoading: false,
            groupTotal: false
        }
    },
    computed: {
        datasetsTree() {
            return this.datasets.length
                ? this.datasets.filter(item => { return item.businessType === '0' }).map(item => {
                    return {
                        ...item,
                        tables: item.fields.reduce((all, item) => {
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
                        }, [])
                    }
                })
                : []
        }
    },
    watch: {
        type(value) {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell) {
                cell.type = value
                this.sheet.reRender()
                this.sheetObj.trigger('change')
            }
        },
        expand(value) {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell) {
                cell.expand = value
                this.sheet.reRender()
                this.sheetObj.trigger('change')
            }
        },
        summary(value) {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell) {
                cell.summary = value
                this.sheet.reRender()
                this.sheetObj.trigger('change')
            }
        },
        context: {
            handler(value) {
                const cell = this.sheetData.getCell(this.ri, this.ci)
                if (cell) {
                    cell.context = [value[0], value[1]]
                    if (value[0] === 2) {
                        cell.axisX = expr2xy(this.customAxisX)
                    } else {
                        this.customAxisX = 'A1'
                    }
                    if (value[1] === 2) {
                        cell.axisY = expr2xy(this.customAxisY)
                    } else {
                        this.customAxisY = 'A1'
                    }
                    this.sheet.reRender()
                    this.sheetObj.trigger('change')
                }
            },
            deep: true
        },
        format(value) {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell) {
                cell.format = value
                this.sheet.reRender()
                this.sheetObj.trigger('change')
            }
        },
        customAxisX(value) {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell) {
                cell.axisX = expr2xy(value)
                this.sheet.reRender()
                this.sheetObj.trigger('change')
            }
        },
        customAxisY(value) {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell) {
                cell.axisY = expr2xy(value)
                this.sheet.reRender()
                this.sheetObj.trigger('change')
            }
        },
        filterText(val) {
            this.$refs.datasetTree.filter(val)
        },
        groupTotal(value) {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell) {
                cell.groupTotal = value
                this.sheet.reRender()
                this.sheetObj.trigger('change')
            }
        }
    },
    mounted() {
        this.id = this.reportId
        this.initializeSheet()
        this.addSheetEventListeners()
        this.initializeData(this.id, true)
        this.showGuide()
    },
    beforeDestroy() {
        this.sheet && this.sheet.destroy()
        this.sheet = undefined
    },
    methods: {
        /**
         * 初始化报表设计器
         */
        initializeSheet() {
            this.sheet = new Spreadsheet(this.$refs.designerV2Main, {
                showBottomBar: false,
                view: {
                    height: () => this.$refs.designerV2Main.clientHeight,
                    width: () => this.$refs.designerV2Main.clientWidth
                }
            })
            this.sheetObj = this.sheet.sheet
            this.sheetDom = this.sheetObj.el.el
            this.sheetData = this.sheet.datas[0]
        },
        /**
         * 绑定事件
        */
        addSheetEventListeners() {
            this.sheetDom.addEventListener('dragover', this.handleDragOver)
            this.sheetDom.addEventListener('drop', this.handleDrop)
            this.sheetDom.addEventListener('mousemove', this.handleMouseMove)
            this.sheetDom.addEventListener('mousedown', this.handleMouseDown)
            this.sheetDom.addEventListener('mouseup', this.handleMouseUp)
            this.sheet.on('cell-selected', this.handleCellSelected)
            this.sheet.on('paste', this.handlePaste)
            this.sheet.on('save', () => {
                this.onSave()
            })
            this.sheet.on('close', () => {
                if (this.isOriginChange()) {
                    this.$confirm('数据未保存，确认操作？', '提示', {
                        distinguishCancelAndClose: true,
                        confirmButtonText: '保存并返回',
                        cancelButtonText: '返回',
                        type: 'warning'
                    }).then(() => {
                        this.onSave(true)
                    }).catch(action => {
                        if (action === 'cancel') {
                            this.$emit('close')
                        }
                    })
                } else {
                    this.$emit('close')
                }
            })
            this.sheet.on('preview', () => {
                // if (!this.$refs.reportPreview.visible) {
                this.onPreview()
                // }
            })
            this.sheet.on('parameter', () => {
                this.isPanel = !this.isPanel
                this.$nextTick(() => {
                    this.sheetObj.sheetReset()
                })
            })
            // this.sheet.on('change', debounce(() => {
            //     const obliqueInput = this.$refs.designerV2Main.querySelector(`.${cssPrefix}-oblique-input`)
            //     if (obliqueInput) {
            //         const cell = this.sheetData.getCell(this.ri || 0, this.ci || 0)
            //         obliqueInput.value = cell ? cell.text : ''
            //     }
            //     if (this.$refs.reportPreview.visible && this.isChange()) {
            //         this.onPreview()
            //         this.setDataCache()
            //     }
            // }, 200))
            this.sheet.on('link', () => {
                this.openLink()
            })
            this.sheet.on('image', () => {
                this.openImage()
            })
            this.sheet.on('expression', () => {
                this.openExpression()
            })
            this.sheet.on('order', () => {
                this.triggerOrder()
            })
            this.sheet.on('createList', () => {
                this.onCreateList()
            })
            // this.sheet.on('share', () => {
            //     this.openShare()
            // })
        },
        /**
         * 初始化excel报表数据
         */
        initializeData(reportId, isInit) {
            if (!reportId) {
                this.setDataCache()
                this.setOriginData()
                const { sri, sci } = this.sheetObj.selector.range
                const cell = this.sheetData.getCell(sri, sci)
                this.handleCellSelected(cell, sri, sci)
                return
            }
            this.loading = true
            this.$ajax({
                url: `${this.detailUrl}/${reportId}`,
                method: 'post'
            }).then(res => {
                if (res.code === 0 && res.data) {
                    const excelConfig = JSON.parse(res.data.componentJson)
                    const rowCells = Object.values(excelConfig.rows).filter(row => row.cells).map(row => { return Object.keys(row.cells).length })
                    if (rowCells.length) {
                        const maxCellLength = Math.max(...rowCells)
                        if (maxCellLength > excelConfig.cols.len) {
                            excelConfig.cols.len = maxCellLength
                        }
                    }
                    this.sheet.loadData(excelConfig)
                    this.sheetData = this.sheet.datas[0]
                    this.originData = res.data
                    this.filterComponents = typeof res.data.filterComponents === 'string' ? JSON.parse(res.data.filterComponents) : (res.data.filterComponents || [])
                    this.onDatasetChange(res.data.dataset
                        ? res.data.dataset.map(dataset => {
                            const fields = []
                            dataset.tables.forEach(table => {
                                table.sqlFieldList.forEach(field => {
                                    fields.push(Object.assign({
                                        datasetName: dataset.datasetName,
                                        aliasName: table.aliasName,
                                        tableName: table.tableName,
                                        cellId: table.cellId,
                                        key: `${table.aliasName}_${field.name}`,
                                        checked: true
                                    }, field))
                                })
                            })
                            return {
                                datasetId: dataset.id,
                                datasetName: dataset.datasetName,
                                relationId: dataset.relationId,
                                type: dataset.type + '',
                                businessType: (dataset.businessType || '0') + '',
                                fields,
                                dataFilter: dataset.dataFilter || []
                            }
                        })
                        : [])
                    // const customDatasource = (res.data.dataset || []).filter(item => {
                    //     return item.type === 1
                    // })
                    // if (customDatasource.length) {
                    //     this.$refs.formDataset.initCustomDatasourceField(customDatasource)
                    // }
                    setTimeout(() => {
                        this.setDataCache()
                        this.setOriginData()
                        if (isInit) {
                            const { sri, sci } = this.sheetObj.selector.range
                            const cell = this.sheetData.getCell(sri, sci)
                            this.handleCellSelected(cell, sri, sci)
                        }
                    }, 0)
                }
            }).finally(() => {
                this.loading = false
            })
        },
        /**
         * 点击单元格事件
         */
        handleCellSelected(cell, ri, ci) {
            if (this.isCustomAxisX || this.isCustomAxisY) return
            this.ri = ri
            this.ci = ci
            if (this.isMove) return
            if (cell && cell.text) {
                this.isPanelList = true
                this.type = cell.type ? cell.type : 0
                this.expand = cell.expand ? cell.expand : 0
                this.context = cell.context ? cell.context : [1, 1]
                this.summary = cell.summary ? cell.summary : 'Sum'
                this.customAxisX = cell.axisX && cell.axisX.length ? xy2expr(cell.axisX[0], cell.axisX[1]) : 'A1'
                this.customAxisY = cell.axisY && cell.axisY.length ? xy2expr(cell.axisY[0], cell.axisY[1]) : 'A1'
                this.format = cell.format ? cell.format : ''
                this.groupTotal = !!cell.groupTotal
            } else {
                this.isPanelList = false
            }
            const obliqueInput = this.$refs.designerV2Main.querySelector(`.${cssPrefix}-oblique-input`)
            if (obliqueInput) {
                obliqueInput.value = cell ? cell.text : ''
            }
        },
        /**
         * 粘贴的回调函数
         */
        handlePaste() {
            this.isPanelList = true
        },
        /**
         * excel报表移动事件
         */
        handleMouseMove(e) {
            if (this.isCustomAxisX || this.isCustomAxisY) return
            const { ri, ci } = this.sheetData.getCellRectByXY(
                e.offsetX,
                e.offsetY
            )
            const cell = this.sheetData.getCell(ri, ci)
            this.draggingRange(e)
            this.promptLayer(e, cell)
            this.moveSheetPosition(cell, ri, ci)
        },
        /**
         * 拖拽范围
         */
        draggingRange(e) {
            const { left, top, width } = this.sheetData.getSelectedRect()
            const cursorMargin = 5
            const x = e.offsetX - this.vHeadWidth
            const y = e.offsetY - this.hHeadHeight
            if (
                y >= top - cursorMargin &&
                y <= top + cursorMargin &&
                x >= left &&
                x <= left + width
            ) {
                this.sheetDom.style.cursor = 'move'
                this.isMove = true
            } else if (!this.isDown) {
                this.sheetDom.style.cursor = 'default'
                this.isMove = false
            }
        },
        /**
         * 提示层
         */
        promptLayer(e, cell) {
            if (cell && cell.type >= 1 && cell.type <= 3) {
                this.dataset = cell.dataset
                this.aliasName = cell.aliasName
                this.text = cell.text
                this.typeText = this.typeList.find(item => item.value === cell.type).label
                this.isHelpPopup = true
                this.$nextTick(() => {
                    this.$refs.helpPopup.style.left = e.pageX + 10 + 'px'
                    this.$refs.helpPopup.style.top = e.pageY + 10 + 'px'
                })
            } else {
                this.isHelpPopup = false
            }
        },
        /**
         * 移动单元格位置
         */
        moveSheetPosition(cell, ri, ci) {
            if (this.isDown && ri > -1 && ci > -1) {
                this.sheetData.selector.setMove(this.cutRi, this.cutCi)
                this.sheetObj.selector.set(ri, ci, true)
                this.sheetObj.trigger('cell-selected', cell, ri, ci)
                this.pasteRi = ri
                this.pasteCi = ci
            }
        },
        /**
         * excel报表鼠标按下事件
         */
        handleMouseDown() {
            if (this.isMove) {
                this.isDown = true
                this.cutRi = this.pasteRi = this.sheetData.selector.ri
                this.cutCi = this.pasteCi = this.sheetData.selector.ci
                this.moveCell = this.sheetData.getCell(this.cutRi, this.cutCi)
                this.lastData = cloneDeep(this.sheetData.getData())
                this.sheetData.clipboard.cut(this.sheetObj.selector.range)
                this.sheetObj.selector.showClipboard()
            }
        },
        /**
         * excel报表鼠标抬起事件
         */
        handleMouseUp(e) {
            this.customAxisSelected(e)
            this.changeSheetPosition()
        },
        /**
         * 触发自定义轴选择
         */
        customAxisSelected(e) {
            const { ri, ci } = this.sheetData.getCellRectByXY(
                e.offsetX,
                e.offsetY
            )
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (this.isCustomAxisX) {
                this.isCustomAxisX = false
                this.customAxisX = xy2expr(ci, ri)
                this.$nextTick(() => {
                    this.sheetObj.selector.set(this.ri, this.ci, true)
                    this.sheetObj.trigger('cell-selected', cell, this.ri, this.ci)
                })
            } else if (this.isCustomAxisY) {
                this.isCustomAxisY = false
                this.customAxisY = xy2expr(ci, ri)
                this.$nextTick(() => {
                    this.sheetObj.selector.set(this.ri, this.ci, true)
                    this.sheetObj.trigger('cell-selected', cell, this.ri, this.ci)
                })
            }
        },
        /**
         * 切换单元格位置
         */
        changeSheetPosition() {
            if (this.isDown) {
                this.isDown = false
                this.sheetDom.style.cursor = 'default'
                this.sheetObj.selector.hideClipboard()
                this.sheetData.selector.setMove()
                if (this.moveCell && this.moveCell.text !== '' && !(this.cutRi === this.pasteRi && this.cutCi === this.pasteCi)) {
                    if (!this.sheetData.canPaste(this.sheetData.clipboard.range, this.sheetObj.selector.range, msg => xtoast('Tip', msg))) {
                        return false
                    }
                    this.sheetData.rows.setCell(this.cutRi, this.cutCi, { text: '' })
                    this.sheetData.rows.setCell(this.pasteRi, this.pasteCi, this.moveCell)
                    this.sheetData.merges.move(this.sheetData.clipboard.range, this.sheetObj.selector.range.sri - this.sheetData.clipboard.range.sri, this.sheetObj.selector.range.sci - this.sheetData.clipboard.range.sci)
                    this.sheet.reRender()
                    const cell = this.sheetData.getCell(this.pasteRi, this.pasteCi)
                    this.sheetObj.selector.set(this.pasteRi, this.pasteCi, true)
                    this.sheetObj.trigger('cell-selected', cell, this.pasteRi, this.pasteCi)
                    this.sheetData.history.add(this.lastData)
                    this.sheetData.history.recordText(this.pasteRi, this.pasteCi, this.moveCell.text)
                    this.sheetObj.trigger('change')
                }
            }
        },
        /**
         * 拖拽字段移动事件
         */
        handleDragOver(e) {
            const { ri, ci } = this.sheetData.getCellRectByXY(
                e.offsetX,
                e.offsetY
            )
            // const cell = this.sheetData.getCell(ri, ci)
            this.sheetObj.selector.set(ri, ci, true)
            // this.sheetObj.trigger('cell-selected', cell, ri, ci)
            this.ri = ri
            this.ci = ci
            e.preventDefault()
        },
        /**
         * 拖拽字段释放事件
         */
        handleDrop(e) {
            if (this.ri < 0 || this.ci < 0) {
                return
            }
            const dataset = e.dataTransfer.getData('dataset')
            const aliasName = e.dataTransfer.getData('aliasName')
            const text = e.dataTransfer.getData('text/plain')
            const key = e.dataTransfer.getData('key')
            if (text) {
                this.sheetData.rows.setCellText(this.ri, this.ci, this.sheetData.history.recordText(this.ri, this.ci))
                this.sheetData.history.add(this.sheetData.getData())
                this.sheetData.rows.setCellText(this.ri, this.ci, text, dataset, key, aliasName)
                this.sheetData.history.recordText(this.ri, this.ci, text)
                this.sheet.reRender()
                this.sheetObj.toolbar.reset()
                const cell = this.sheetData.getCell(this.ri, this.ci)
                this.type = cell.type
                this.expand = cell.expand
                this.context = cell.context
                this.dataset = cell.dataset
                this.aliasName = cell.aliasName
                this.text = cell.text
                this.typeText = this.type === 1 ? '分组' : '列表'
                this.isPanelList = true
                this.customAxisX = 'A1'
                this.customAxisY = 'A1'
                this.summary = 'Sum'
                this.format = cell.format
                this.groupTotal = !!cell.groupTotal
                this.sheetObj.trigger('change')
            }
            e.preventDefault()
        },
        /**
         * 获取excel报表最终数据
         */
        handleData() {
            console.log(this.sheet.getData())
            return this.sheet.getData()
        },
        /**
         * 自定义上下文事件
         */
        handleCustomAxis(axis) {
            if (axis === 'X') {
                this.isCustomAxisY = false
                this.isCustomAxisX = !this.isCustomAxisX
            } else if (axis === 'Y') {
                this.isCustomAxisX = false
                this.isCustomAxisY = !this.isCustomAxisY
            }
            if (this.isCustomAxisX || this.isCustomAxisY) {
                this.sheetDom.style.cursor = 'pointer'
            } else {
                this.sheetDom.style.cursor = 'default'
            }
        },
        onAddDataset() {
            this.selectedDataset = this.datasets.map(item => item.datasetId)
            this.datasetLoading = true
            this.$ajax({
                url: '/relation/treeByAuth',
                method: 'post'
            }).then(res => {
                if (res.code === 0) {
                    const result = []
                    res.data.forEach(group => {
                        if (group.data && Object.keys(group.data).length) {
                            result.push({
                                id: group.id,
                                label: group.bizGroupName,
                                children: Object.keys(group.data).map(relation => {
                                    return {
                                        id: relation,
                                        label: relation,
                                        children: group.data[relation].map(dataset => {
                                            return {
                                                ...dataset,
                                                label: dataset.name
                                            }
                                        })
                                    }
                                })
                            })
                        }
                    })
                    this.datasetTree = result
                }
            }).finally(() => {
                this.datasetLoading = false
            })
            this.datasetVisible = true
        },
        onDatasetChange(dataset) {
            this.datasets = dataset
            this.$nextTick(() => {
                this.$refs.form.init()
            })
        },
        getDatasetParams() {
            return this.datasets.map(item => item.datasetId)
        },
        onPreview() {
            this.$refs.reportPreview.open({
                componentJson: JSON.stringify(this.handleData()),
                dataSetId: this.getDatasetParams(),
                filterComponents: this.filterComponents,
                reportId: this.reportId ? this.reportId : this.originData.reportId,
                source: 'editPreview'
            })
            this.$nextTick(() => {
                this.sheetObj.sheetReset()
            })
        },
        onSave(flag) {
            const data = this.handleData()
            const dataSetId = this.getDatasetParams()
            this.$refs.reportSaveDialog.handleSave({
                reportId: this.originData.reportId,
                name: this.originData.name,
                groupId: this.originData.groupId,
                componentJson: JSON.stringify(data),
                dataSetId,
                filterComponents: JSON.stringify(this.filterComponents)
            }, () => {
                if (flag) {
                    this.$emit('close')
                }
            })
        },
        onSaved(reportId) {
            // this.$emit('close')
            this.initializeData(reportId)
            this.id = reportId
            this.$message.success('保存成功')
        },
        onLinkChange(link) {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell) {
                if (link && link.targetReport) {
                    cell.link = link
                } else {
                    delete cell.link
                }
                this.sheet.reRender()
                this.sheetObj.trigger('change')
            }
            this.$refs.reportLinkDialog.close()
        },
        onImageChange(image) {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell && cell.text) {
                if (image && image.base64) {
                    this.sheetData.history.add(this.sheetData.getData())
                    cell.image = {
                        width: image.width,
                        height: image.height
                    }
                    this.sheetData.rows.setCellText(this.ri, this.ci, image.base64, '', '', '', 5)
                    this.sheetData.history.recordText(this.ri, this.ci, image.base64)
                } else {
                    this.sheetData.history.add(this.sheetData.getData())
                    delete cell.image
                    this.sheetData.rows.setCellText(this.ri, this.ci, '', '', '', '', 0)
                    this.sheetData.history.recordText(this.ri, this.ci, '')
                }
                this.sheet.reRender()
                this.sheetObj.trigger('change')
            } else {
                if (image && image.base64) {
                    this.sheetData.rows.setCellText(this.ri, this.ci, this.sheetData.history.recordText(this.ri, this.ci))
                    this.sheetData.history.add(this.sheetData.getData())
                    this.sheetData.rows.setCellText(this.ri, this.ci, image.base64, '', '', '', 5)
                    this.sheetData.history.recordText(this.ri, this.ci, image.base64)
                    const cell = this.sheetData.getCell(this.ri, this.ci)
                    cell.image = {
                        width: image.width,
                        height: image.height
                    }
                    this.type = cell.type
                    this.expand = cell.expand
                    this.context = cell.context
                    this.dataset = cell.dataset
                    this.aliasName = cell.aliasName
                    this.text = cell.text
                    this.format = cell.format
                    this.groupTotal = !!cell.groupTotal
                    this.isPanelList = true
                    this.sheet.reRender()
                    this.sheetObj.trigger('change')
                }
            }
            this.$refs.reportImageDialog.close()
        },
        onExpressionChange(expression) {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell && cell.text) {
                if (expression) {
                    cell.expression = true
                    cell.expressionText = expression
                } else {
                    delete cell.expression
                    delete cell.expressionText
                }
                this.sheet.reRender()
                this.sheetObj.trigger('change')
            } else {
                if (expression) {
                    this.sheetData.rows.setCellText(this.ri, this.ci, this.sheetData.history.recordText(this.ri, this.ci))
                    this.sheetData.history.add(this.sheetData.getData())
                    this.sheetData.rows.setCellText(this.ri, this.ci, '', '', '', '', 0)
                    this.sheetData.history.recordText(this.ri, this.ci, '')
                    const cell = this.sheetData.getCell(this.ri, this.ci)
                    cell.expression = true
                    cell.expressionText = expression
                    this.type = cell.type
                    this.expand = cell.expand
                    this.context = cell.context
                    this.dataset = cell.dataset
                    this.aliasName = cell.aliasName
                    this.text = cell.text
                    this.format = cell.format
                    this.groupTotal = !!cell.groupTotal
                    this.isPanelList = true
                    this.sheet.reRender()
                    this.sheetObj.trigger('change')
                }
            }
            // 重置表达式图标active
            this.sheetObj.toolbar.reset()
            this.$refs.reportExpressionDialog.handleClose()
        },
        handlePreviewClose() {
            this.$nextTick(() => {
                this.sheetObj.sheetReset()
            })
        },
        onSheetRerender() {
            this.sheetObj.sheetReset()
        },
        /**
         * 获取字符串的md5
         * @param {*} arrayBuffer
         */
        getMd5(arrayBuffer) {
            const wordArray = CryptoJS.lib.WordArray.create(arrayBuffer)
            return CryptoJS.MD5(wordArray).toString()
        },
        isChange() {
            return this.getMd5(JSON.stringify(this.handleData())) !== this.dataCache
        },
        isOriginChange() {
            return this.getMd5(JSON.stringify({
                componentJson: this.handleData(),
                dataset: this.getDatasetParams(),
                filterComponents: this.filterComponents
            })) !== this.originDataCache
        },
        setDataCache() {
            this.dataCache = this.getMd5(JSON.stringify(this.handleData()))
        },
        setOriginData() {
            this.originDataCache = this.getMd5(JSON.stringify({
                componentJson: this.handleData(),
                dataset: this.getDatasetParams(),
                filterComponents: this.filterComponents
            }))
        },
        openLink() {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell) {
                const params = {
                    targetReport: '',
                    condition: [],
                    cell: cell,
                    sourceFields: []
                }
                if (cell && cell.dataset) {
                    const source = this.datasets.find(item => { return item.alias === cell.dataset || item.datasetName === cell.dataset })
                    params.sourceFields = source.fields
                }
                if (cell.link) {
                    params.targetReport = cell.link.targetReport
                    params.condition = cell.link.condition
                }
                this.$refs.reportLinkDialog.open(params)
            }
        },
        openShare() {
            if (!this.id) {
                this.$message.warning('请先保存报表')
                return
            }
            this.$refs.reportShareDialog.open({
                targetReport: this.id,
                targetReportName: this.originData.name
            })
        },
        openImage() {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            const params = {
                base64: '',
                width: '',
                height: ''
            }
            if (cell && cell.image) {
                params.base64 = cell.text
                params.width = cell.image.width
                params.height = cell.image.height
            }
            this.$refs.reportImageDialog.open(params)
        },
        openExpression() {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            const inputFields = []
            this.sheetData.rows.each(ri => {
                this.sheetData.rows.eachCells(ri, (ci, cell) => {
                    if (cell.type === 1 || cell.type === 2) {
                        inputFields.push({
                            name: cell.text,
                            coordinates: xy2expr(Number(ci), Number(ri))
                        })
                    }
                })
            })
            const params = {
                expression: cell && cell.expression ? cell.expressionText : '',
                inputFields
            }
            this.$refs.reportExpressionDialog.open(params)
        },
        filterSave(filterComponents) {
            this.filterComponents = filterComponents
            this.onPreview()
        },
        showGuide() {
            if (localStorage.getItem('readReportGuide') !== 'Y') {
                this.visibleGuide = true
            }
        },
        handleCloseGuide() {
            localStorage.setItem('readReportGuide', 'Y')
            this.visibleGuide = false
        },
        handleBottomBarChange(tab) {
            if (tab === 'dataset') {
                this.$refs.dataset.setDefaultCurrent()
            } else if (tab === 'formDataset') {
                this.$refs.formDataset.setDefaultCurrent()
            }
        },
        validChange(now, old) {
            if (old === 'dataset') {
                if (this.$refs.dataset.isChange()) {
                    this.$message.warning('请保存设计器数据集')
                    return false
                }
            } else if (old === 'formDataset') {
                if (this.$refs.formDataset.isChange()) {
                    this.$message.warning('请保存设计器数据集')
                    return false
                }
            }
            return true
        },
        triggerOrder() {
            const cell = this.sheetData.getCell(this.ri, this.ci)
            if (cell && cell.text) {
                if (cell.type === 7) {
                    this.sheetData.history.add(this.sheetData.getData())
                    this.sheetData.rows.setCellText(this.ri, this.ci, '', '', '', '', 0)
                    this.sheetData.history.recordText(this.ri, this.ci, cell.text)
                } else {
                    this.sheetData.history.add(this.sheetData.getData())
                    this.sheetData.rows.setCellText(this.ri, this.ci, '序号', '', '', '', 7)
                    this.sheetData.history.recordText(this.ri, this.ci, cell.text)
                }
            } else {
                this.sheetData.rows.setCellText(this.ri, this.ci, this.sheetData.history.recordText(this.ri, this.ci))
                this.sheetData.history.add(this.sheetData.getData())
                this.sheetData.rows.setCellText(this.ri, this.ci, '序号', '', '', '', 7)
                this.sheetData.history.recordText(this.ri, this.ci, '')
                const cell = this.sheetData.getCell(this.ri, this.ci)
                this.type = cell.type
                this.expand = cell.expand
                this.context = cell.context
                this.dataset = cell.dataset
                this.aliasName = cell.aliasName
                this.text = cell.text
                this.format = cell.format
                this.groupTotal = !!cell.groupTotal
                this.isPanelList = true
            }
            this.sheet.reRender()
            this.sheetObj.trigger('change')
            this.sheetObj.toolbar.reset()
        },
        toFormDataset() {
            this.$refs.bottomBar.onChange('formDataset')
        },
        datasetConfirm() {
            const selected = this.$refs.datasetTree.getCheckedNodes(true)
            this.onDatasetChange(selected.map(dataset => {
                const fields = []
                JSON.parse(dataset.fieldJsonArray).forEach(table => {
                    table.sqlFieldList.forEach(field => {
                        fields.push(Object.assign({
                            datasetName: dataset.name,
                            aliasName: table.aliasName,
                            tableName: table.tableName,
                            cellId: table.cellId,
                            key: `${table.aliasName}_${field.name}`,
                            checked: true
                        }, field))
                    })
                })
                return {
                    datasetId: dataset.id,
                    datasetName: dataset.name,
                    relationId: dataset.relationId,
                    type: dataset.type + '',
                    businessType: (dataset.businessType || '0') + '',
                    fields,
                    dataFilter: dataset.dataFilter ? JSON.parse(dataset.dataFilter) : []
                }
            }))
            this.datasetVisible = false
        },
        onCreateList() {
            let titleRi = this.ri
            let fieldTitleRi = this.ri + 1
            let fieldRi = this.ri + 2
            let ci = this.ci
            this.sheetData.changeData(() => {
                this.datasets.forEach(dataset => {
                    this.sheetData.rows.deleteCell(titleRi, ci, 'text')
                    this.sheetData.rows.setCellText(titleRi, ci, dataset.datasetName, '', '', '')
                    dataset.fields.forEach((field, fieldIndex) => {
                        this.sheetData.rows.deleteCell(fieldTitleRi, ci + fieldIndex, 'text')
                        this.sheetData.rows.deleteCell(fieldRi, ci + fieldIndex, 'text')
                        this.sheetData.rows.setCellText(fieldTitleRi, ci + fieldIndex, field.alias, '', '', '')
                        this.sheetData.rows.setCellText(fieldRi, ci + fieldIndex, field.alias, field.datasetName, field.key, field.aliasName)

                    })
                    titleRi = titleRi + 3
                    fieldTitleRi = fieldTitleRi + 3
                    fieldRi = fieldRi + 3
                })

                this.sheetData.updateLen()
                this.sheetObj.sheetReset()
                this.sheetObj.toolbar.reset()
            })
        },
        filterNode(value, data) {
            if (!value) return true
            return data.label.indexOf(value) !== -1
        }
    }
}
</script>
<style scoped lang="scss">
.report-designer-v2 {
    display: flex;
    width: 100%;
    height: 100vh;
    overflow: hidden;
    flex-direction: column;

    & * {
        box-sizing: border-box;
    }

    ::v-deep .x-spreadsheet-toolbar {
        box-sizing: border-box;
    }

    ::v-deep *::-webkit-scrollbar {
        width: 13px;
        height: 13px;
    }

    .designer-v2-body {
        flex: 1;
        width: 100%;
        height: 0;
        display: flex;
        overflow: hidden;

        .designer-v2-center {
            flex: 1;
            width: 0;
            height: 100%;
            display: flex;
            flex-direction: column;

            .designer-v2-main {
                height: 0;
                flex: 1;
                width: 100%;
                display: flex;

                .designer-v2-main-ins {
                    width: 0;
                    flex: 1;
                    height: 100%;
                }
            }
        }

        .designer-v2-panel {
            width: 200px;
            flex-shrink: 0;
            border-left: 1px #e8e8e8 solid;
            position: relative;
            height: 100%;
            overflow-y: auto;

            .panel-title {
                font-size: 20px;
                font-weight: lighter;
                color: rgb(8, 137, 44);
                margin: 10px;
            }

            .panel-close {
                cursor: pointer;
                position: absolute;
                right: 10px;
                top: 10px;
            }

            .panel-list {
                margin: 0;
                padding: 10px;
                list-style: none;

                .panel-list-item {
                    margin-bottom: 20px;
                    padding: 0;

                    h4 {
                        margin: 0;
                        font-size: 16px;
                    }

                    section {
                        display: flex;
                        align-items: center;
                        margin: 15px 5px;
                        height: 20px;
                    }
                }

                .panel-list-label {
                    width: 60px;
                    flex-shrink: 0;
                    text-align: center;
                }

                .panel-list-value {
                    flex: 1;
                    cursor: pointer;

                    i {
                        font-size: 18px;
                        line-height: 20px;
                    }

                    .active {
                        font-size: 16px;
                        line-height: 20px;
                        color: #409EFF;
                    }
                }
            }
        }
    }

    .designer-v2-dataset {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 30px;
        background-color: #FFF;
        z-index: 4000;
    }

    .designer-v2-form {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 30px;
        background-color: #FFF;
        z-index: 4000;
    }

    .help-popup {
        background-color: #fff;
        border: 1px solid rgba(0, 0, 0, 0.2);
        color: #222;
        font-size: 11px;
        word-wrap: break-word;
        position: absolute;
        min-width: 100px;
        z-index: 2001;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);

        div {
            padding: 5px;
        }
    }
    
}
</style>
