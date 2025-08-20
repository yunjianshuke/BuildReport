<template>
    <div class="relation-chart">
        <div ref="relationChartMenu" class="relation-chart-menu">
            <el-input v-model="filterText" class="relation-chart-filter" placeholder="输入关键字进行过滤" @change="filterChange" />
            <el-tree
                ref="relationChartTree"
                v-loading="loading"
                class="relation-chart-tree"
                :data="treeData"
                :props="defaultProps"
                :filter-node-method="filterNode"
            >
                <template slot-scope="{ node }">
                    <div class="custom-tree-node" :title="node.data.name" @mousedown="handleStartDrag(node, $event)">
                        <div :class="'is-leaf' + node.level" style="line-height: 16px;">{{ node.data.name }}</div>
                        <div v-if="node.level === 2" :class="'is-leaf' + node.level" style="line-height: 16px;color: #8492a6; font-size: 12px">{{ node.data.comment }}</div>
                    </div>
                </template>
            </el-tree>
        </div>
        <div class="relation-chart-main">
            <div ref="relationChartOuter" class="relation-chart-canvas">
                <div ref="relationChartCanvas" class="relation-chart-ins" />
                <div class="relation-chart-name">
                    <el-form ref="form" :model="form" :rules="formRule" label-width="0" :inline="true">
                        <el-form-item label="" prop="relationName">
                            <el-input v-model="form.relationName" placeholder="请输入业务关系名称" />
                        </el-form-item>
                        <el-form-item label="" prop="bizGroupId">
                            <el-cascader
                                v-model="form.bizGroupId" :options="groupList"
                                :props="{ checkStrictly: true, emitPath: false, label: 'name', value: 'id' }"
                                placeholder="请选择分组" clearable
                            />
                        </el-form-item>
                    </el-form>
                    <el-button type="primary" @click="onSave">保存</el-button>
                </div>
            </div>
            <chart-config-panel
                v-if="isRelationConfig"
                ref="relationChartConfig"
                :chart-ins="graph"
                :selector="selector"
                :datas="datas"
                :now-data="nowData"
                :fields="fields"
                :get-preview="getPreview"
                @close="isRelationConfig = false"
                @selectChange="onSelectChange"
                @joined="onJoined"
                @resize="resize"
                @aliasnamechange="onAliasNameChange"
            />
        </div>
    </div>
</template>
<script>
import { Graph, Addon, Markup } from '@antv/x6'
import warningIcon from '../icons/warning.svg'
import { DagreLayout } from '@antv/layout'
import ChartConfigPanel from './chartConfigPanel.vue'
import { pick } from 'lodash'
import CryptoJS from 'crypto-js'

export default {
    name: 'ReportRelationChart',
    components: {
        ChartConfigPanel
    },
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
        getPreview: {
            type: String,
            default: '/datasource/preview'
        },
        detailUrl: {
            type: String,
            default: '/relation/info'
        },
        relationId: {
            type: String,
            default: ''
        }
    },
    data() {
        return {
            form: {
                relationId: '',
                relationName: '',
                bizGroupId: ''
            },
            formRule: {
                relationName: [{
                    required: true,
                    message: '请输入业务关系名称',
                    trigger: 'blur'
                }],
                bizGroupId: [{
                    required: true,
                    message: '请选择业务关系分组',
                    trigger: 'blur'
                }]
            },
            treeData: [],
            nodeWidth: 150,
            nodeHeight: 70,
            rootNodeScale: 1.5,
            nodeBreathingSpace: 20,
            isRelationConfig: false,
            shapeSelectionColor: '#FFA500',
            warningSize: 20,
            datas: [],
            nowData: '',
            loading: false,
            filterText: '',
            defaultProps: {
                children: 'children',
                label: 'name'
            },
            rootNodeText: null,
            graph: null,
            selector: [],
            fields: {},
            detailCache: '',
            groupList: []
        }
    },
    beforeDestroy() {
        this.graph && this.graph.dispose()
    },
    mounted() {
        this.loadTree()
        this.initializeGraph()
        this.loadMenuData()
        // 编辑，初始化数据
        this.loadGraphData()
        this.setupDragAndDrop()
        // 新增，添加监听，编辑的监听在loadGraphData方法中
        if (!this.relationId) {
            this.setupEventListeners()
            this.initDetailMd5()
        }
    },
    methods: {
        // 获取tree
        loadTree() {
            this.$ajax({
                url: '/businessGroup/tree',
                method: 'post',
                data: {}
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

                    this.groupList = datas
                    this.$nextTick(() => {
                        this.openTree()
                    })
                } else if (data.code === 0 && !data.data) {
                    this.groupList = []
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
        /**
         * 初始化图表
         */
        initializeGraph() {
            this.graph = new Graph({
                container: this.$refs.relationChartCanvas,
                grid: true,
                history: false,
                keyboard: false,
                autoResize: true,
                scroller: {
                    enabled: true,
                    pageVisible: false,
                    pageBreak: false,
                    pannable: true
                },
                mousewheel: {
                    enabled: true,
                    modifiers: ['ctrl']
                },
                interacting(cellView) {
                    if (
                        cellView.cell.getData() !== undefined &&
                        !cellView.cell.getData().disableMove
                    ) {
                        return { nodeMovable: false, edgeLabelMovable: false }
                    } else if (cellView.labelSelectors) {
                        return false
                    }
                    return true
                },
                onEdgeLabelRendered: args => {
                    const { selectors } = args
                    const content = selectors.foContent
                    if (content) {
                        const icon = document.createElement('img')
                        icon.src = warningIcon
                        icon.width = this.warningSize
                        icon.height = this.warningSize
                        icon.style.background = 'white'
                        icon.style.cursor = 'pointer'
                        content.appendChild(icon)
                    }
                }
            })
        },
        /**
         * 加载图表数据
         */
        loadGraphData() {
            if (!this.relationId) {
                return
            }
            this.$ajax({
                url: `${this.detailUrl}/${this.relationId}`,
                method: 'post'
            }).then(res => {
                if (res.code === 0) {
                    Object.assign(this.form, pick(res.data, ['relationId', 'relationName', 'bizGroupId']))
                    // 创建节点和边
                    this.loadChart(res.data)
                }
            })
        },
        /**
         * 根据图表数据绘制节点和边
         * @param {*} data
         */
        loadChart(data) {
            data.nodeList.forEach((item, index) => {
                this.fields[item.cellId] = JSON.parse(item.componentJson)
                const node = this.graph.addNode({
                    id: item.cellId,
                    width: this.nodeWidth,
                    height: this.nodeHeight,
                    x: item.guiX,
                    y: item.guiY,
                    attrs: {
                        label: {
                            text: this.foldedText(item.tableName, 13) + '\n' + this.foldedText(item.aliasName, 10),
                            fill: '#6a6c8a'
                        },
                        body: {
                            stroke: '#31d0c6',
                            strokeWidth: 2
                        }
                    },
                    tools: [
                        {
                            name: 'button-remove',
                            args: {
                                x: '100%',
                                y: 0,
                                offset: { x: -10, y: 10 },
                                onClick: ({ cell }) => {
                                    const nodes = this.graph.getNodes()
                                    if (cell.id === nodes[0].id) {
                                        this.graph.clearCells()
                                        this.rootNodeText = null
                                    } else {
                                        this.graph.removeCell(cell)
                                    }
                                    this.nowData = ''
                                    this.resetEdgeAndNodeStyles()
                                    this.getDataForAllNodesAndEdges()
                                }
                            }
                        }
                    ],
                    data: {
                        datasourceId: item.datasourceId,
                        datasourceName: item.datasourceName,
                        tableName: item.tableName,
                        aliasName: item.aliasName,
                        commentName: item.commentName,
                        hidden: item.hidden || false,
                        disableMove: true
                    }
                })
                // 初始化主节点
                if (index === 0) {
                    this.initialNodeCentered(node)
                    this.rootNodeText = node.getData().tableName
                }
                // 更新表字段
                this.loadFields(node)
            })
            data.edgeList.forEach(item => {
                this.graph.addEdge({
                    id: item.edgeId,
                    source: {
                        cell: item.sourceCellNodeId,
                        anchor: 'right',
                        connectionPoint: 'anchor'
                    },
                    target: {
                        cell: item.targetCellNodeId,
                        anchor: 'left',
                        connectionPoint: 'anchor'
                    },
                    attrs: {
                        line: {
                            stroke: '#5F95FF',
                            strokeWidth: 2
                        }
                    },
                    data: {
                        symbleList: JSON.parse(item.componentJson)
                    },
                    connector: {
                        name: 'smooth',
                        args: {
                            direction: 'H'
                        }
                    }
                })
            })
            // 更新布局
            this.handleFormat()
            // 添加监听
            this.setupEventListeners()
            // 更新datas
            this.getDataForAllNodesAndEdges()
            // 缓存初始数据的md5，用于比对是否有修改
            this.initDetailMd5()
        },
        /**
         * 加载菜单数据
         */
        loadMenuData() {
            const vm = this
            vm.loading = true
            vm.$ajax({
                url: vm.listUrl,
                method: 'post',
                data: {
                }
            }).then(res => {
                if (res.code === 0 && res.data) {
                    vm.treeData = res.data
                    const promises = []
                    vm.treeData.forEach(item => {
                        promises.push(vm.$ajax({
                            url: vm.filterText ? `${vm.getTables}/${item.id}/${vm.filterText}` : `${vm.getTables}/${item.id}`
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
                                this.$refs.relationChartTree.filter(this.filterText)
                            })
                        }
                    }).finally(() => {
                        vm.loading = false
                    })
                }
            }).catch(() => {
                vm.loading = false
            })
        },
        filterChange() {
            this.loadMenuData()
        },
        /**
         * 设置拖拽功能
         */
        setupDragAndDrop() {
            this.dnd = new Addon.Dnd({
                target: this.graph,
                scaled: false,
                animation: true,
                dndContainer: this.$refs.relationChartMenu,
                getDragNode: node => node.clone({ keepId: true }),
                getDropNode: node => node.clone({ keepId: true }),
                validateNode: node => {
                    if (this.rootNodeText === node.getData().tableName) {
                        this.$message({
                            message: '主表已添加',
                            type: 'warning'
                        })
                        return false
                    } else {
                        return true
                    }
                }
            })
        },
        /**
         * 监听所有事件
         */
        setupEventListeners() {
            const vm = this
            // 监听节点添加事件
            this.graph.on('node:added', ({ node }) => {
                if (this.dragEdge) {
                    // this.graph.removeEdge(this.dragEdge)
                    this.dragEdge.setTarget({
                        cell: node.id,
                        anchor: 'left',
                        connectionPoint: 'anchor'
                    })
                    this.dragEdge = null
                }
                const nodes = this.graph.getNodes()
                if (nodes.length === 1) {
                    this.initialNodeCentered(node)
                    this.rootNodeText = node.getData().tableName
                }
                this.connectToClosestNode(node)
                this.getDataForAllNodesAndEdges()
                const edges = this.graph.getEdges()
                edges.find(edge => {
                    if (edge.getTargetCellId() === node.id) {
                        this.onSelectChange(edge.id)
                    }
                })
                this.handleFormat()
                this.loadFields(node)
            })
            // 监听节点移动事件
            this.graph.on('node:moving', ({ node, x, y }) => {
                if (!vm.nodeOldX) {
                    vm.nodeOldX = x
                    vm.nodeOldY = y
                }
                if (Math.abs(Math.abs(x) - Math.abs(vm.nodeOldX)) > vm.nodeBreathingSpace || Math.abs(Math.abs(y) - Math.abs(vm.nodeOldY)) > vm.nodeBreathingSpace) {
                    this.connectToClosestNode(node)
                    this.getDataForAllNodesAndEdges()
                }
            })

            this.graph.on('node:mouseup', ({ node }) => {
                const edges = this.graph.getEdges()
                edges.find(edge => {
                    if (edge.getTargetCellId() === node.id) {
                        this.onSelectChange(edge.id)
                    }
                })
                this.handleFormat()
                this.nodeOldX = undefined
                this.nodeOldY = undefined
            })
            // 节点点击
            this.graph.on('node:click', ({ node }) => {
                if (node.attr('body/stroke') === this.shapeSelectionColor) {
                    this.nowData = ''
                    this.isRelationConfig = false
                    this.resetEdgeAndNodeStyles()
                    this.$nextTick(() => {
                        this.handleFormat()
                    })
                } else {
                    this.nowData = node.id
                    this.isRelationConfig = true
                    this.resetEdgeAndNodeStyles()
                    node.attr('body/stroke', this.shapeSelectionColor)
                    this.$nextTick(() => {
                        this.handleFormat()
                    })
                }
            })
            // 边点击
            this.graph.on('edge:click', ({ edge }) => {
                if (edge.attr('line/stroke') === this.shapeSelectionColor) {
                    this.nowData = ''
                    this.isRelationConfig = false
                    this.resetEdgeAndNodeStyles()
                } else {
                    this.nowData = edge.id
                    this.isRelationConfig = true
                    this.resize()
                    this.resetEdgeAndNodeStyles()
                    edge.attr('line/stroke', this.shapeSelectionColor)
                }
            })
            this.$refs.relationChartMenu.addEventListener('mousedown', this.handleMouseDown)
        },
        resize() {
            this.graph.resize(
                this.$refs.relationChartOuter.clientWidth,
                this.$refs.relationChartOuter.clientHeight
            )
            this.centerNodesVertically()
        },
        /**
         * 处理鼠标按下事件
         */
        handleMouseDown(e) {
            if (!(e.target.classList.contains('custom-tree-node') || e.target.classList.contains('is-leaf2'))) return
            const nodes = this.graph.getNodes()
            let closestNode = null
            let minDistance = Infinity
            const { x, y } = this.graph.pageToLocal(e.pageX, e.pageY)
            if (nodes.length) {
                nodes.forEach(node => {
                    const distance = this.mouseCalculateDistance(node, x, y)
                    if (distance < minDistance) {
                        minDistance = distance
                        closestNode = node
                    }
                })

                this.dragEdge = this.graph.addEdge({
                    source: {
                        cell: closestNode.id,
                        anchor: 'right',
                        connectionPoint: 'anchor'
                    },
                    target: {
                        x,
                        y,
                        anchor: 'left',
                        connectionPoint: 'anchor'
                    },
                    attrs: {
                        line: {
                            stroke: '#5F95FF',
                            strokeWidth: 2
                        }
                    },
                    data: {
                        symbleList: [{
                            sourceColumnName: '',
                            targetColumnName: '',
                            symble: '='
                        }]
                    },
                    connector: {
                        name: 'smooth',
                        args: {
                            direction: 'H'
                        }
                    },
                    defaultLabel: {
                        markup: Markup.getForeignObjectMarkup(),
                        attrs: {
                            fo: {
                                width: this.warningSize,
                                height: this.warningSize,
                                y: -this.warningSize / 2,
                                x: -this.warningSize / 2
                            }
                        }
                    },
                    label: {
                        position: 0.5
                    }
                })
            }
            document.addEventListener('mousemove', this.handleMouseMove)
            document.addEventListener('mouseup', this.handleMouseUp)
        },
        /**
         * 处理鼠标移动事件
         */
        handleMouseMove(e) {
            if (this.dragEdge) {
                const nodes = this.graph.getNodes()
                let closestNode = null
                let minDistance = Infinity
                const { x, y } = this.graph.pageToLocal(e.pageX, e.pageY)
                nodes.forEach(node => {
                    const distance = this.mouseCalculateDistance(node, x, y)
                    if (distance < minDistance) {
                        minDistance = distance
                        closestNode = node
                    }
                })
                this.dragEdge.setSource({
                    cell: closestNode.id,
                    anchor: 'right',
                    connectionPoint: 'anchor'
                })
                this.dragEdge.setTarget({
                    x,
                    y,
                    anchor: 'left',
                    connectionPoint: 'anchor'
                })
            }
        },
        /**
         * 处理鼠标抬起事件
         */
        handleMouseUp() {
            if (this.dragEdge && !this.dragEdge.getTargetCellId()) {
                this.graph.removeEdge(this.dragEdge)
                this.dragEdge = null
            }
            document.removeEventListener('mousemove', this.handleMouseMove)
            document.removeEventListener('mouseup', this.handleMouseUp)
        },
        /**
         * 设置主表固定居中，且不能拖拽
         */
        initialNodeCentered(node) {
            const centerX =
                this.$refs.relationChartOuter.clientWidth / 2 -
                (this.nodeWidth * this.rootNodeScale) / 2
            const centerY =
                this.$refs.relationChartOuter.clientHeight / 2 -
                (this.nodeHeight * this.rootNodeScale) / 2
            node.position(centerX, centerY)
            node.setData({ disableMove: false })
            node.size({
                width: this.nodeWidth * this.rootNodeScale,
                height: this.nodeHeight * this.rootNodeScale
            })
        },
        /**
         * 开始拖动节点
         */
        handleStartDrag(treeNode, e) {
            if (treeNode.level === 1) return
            const node = this.graph.createNode({
                width: this.nodeWidth,
                height: this.nodeHeight,
                attrs: {
                    label: {
                        text: this.foldedText(treeNode.label, 13) + (treeNode.data.comment ? '\n' + this.foldedText(treeNode.data.comment, 10) : ''),
                        fill: '#6a6c8a'
                    },
                    body: {
                        stroke: '#31d0c6',
                        strokeWidth: 2
                    }
                },
                tools: [
                    {
                        name: 'button-remove',
                        args: {
                            x: '100%',
                            y: 0,
                            offset: { x: -10, y: 10 },
                            onClick: ({ cell }) => {
                                const nodes = this.graph.getNodes()
                                if (cell.id === nodes[0].id) {
                                    this.graph.clearCells()
                                    this.rootNodeText = null
                                } else {
                                    this.graph.removeCell(cell)
                                }
                                this.nowData = ''
                                this.resetEdgeAndNodeStyles()
                                this.getDataForAllNodesAndEdges()
                            }
                        }
                    }
                ]
            })
            node.setData({
                datasourceId: treeNode.parent.data.id,
                datasourceName: treeNode.parent.data.name,
                tableName: treeNode.label,
                aliasName: treeNode.data.comment,
                commentName: treeNode.data.comment,
                hidden: false,
                disableMove: true
            })
            this.dnd.start(node, e)
            e.preventDefault()
        },
        /**
         * 连接到最近的节点
         */
        connectToClosestNode(newNode) {
            const nodes = this.graph.getNodes()
            let closestNode = null
            let minDistance = Infinity
            // 获取新节点的所有子节点
            const childNodes = this.getChildNodes(newNode)
            // this.removeConnections(newNode)
            nodes.forEach(node => {
                if (node.id !== newNode.id && !childNodes.includes(node.id)) {
                    const distance = this.calculateDistance(node, newNode)
                    if (distance < minDistance) {
                        minDistance = distance
                        closestNode = node
                    }
                }
            })
            if (closestNode) {
                if (!this.edgeExists(closestNode, newNode)) {
                    this.nowData = ''
                    this.resetEdgeAndNodeStyles()
                    const edges = this.graph.getEdges()
                    const edge = edges.find(edge => {
                        return edge.getTargetCellId() === newNode.id
                    })
                    if (edge) {
                        edge.setSource({
                            cell: closestNode.id,
                            anchor: 'right',
                            connectionPoint: 'anchor'
                        })
                        edge.data = {
                            symbleList: [{
                                sourceColumnName: '',
                                targetColumnName: '',
                                symble: '='
                            }]
                        }
                        edge.setLabels({
                            position: 0.5,
                            markup: Markup.getForeignObjectMarkup(),
                            attrs: {
                                fo: {
                                    width: this.warningSize,
                                    height: this.warningSize,
                                    y: -this.warningSize / 2,
                                    x: -this.warningSize / 2
                                }
                            }
                        })
                    } else {
                        this.graph.addEdge({
                            source: {
                                cell: closestNode.id,
                                anchor: 'right',
                                connectionPoint: 'anchor'
                            },
                            target: {
                                cell: newNode.id,
                                anchor: 'left',
                                connectionPoint: 'anchor'
                            },
                            attrs: {
                                line: {
                                    stroke: '#5F95FF',
                                    strokeWidth: 2
                                }
                            },
                            data: {
                                symbleList: [{
                                    sourceColumnName: '',
                                    targetColumnName: '',
                                    symble: '='
                                }]
                            },
                            connector: {
                                name: 'smooth',
                                args: {
                                    direction: 'H'
                                }
                            },
                            defaultLabel: {
                                markup: Markup.getForeignObjectMarkup(),
                                attrs: {
                                    fo: {
                                        width: this.warningSize,
                                        height: this.warningSize,
                                        y: -this.warningSize / 2,
                                        x: -this.warningSize / 2
                                    }
                                }
                            },
                            label: {
                                position: 0.5
                            }
                        })
                    }
                }
            }
        },
        /**
         * 获取所有子节点
         */
        getChildNodes(node) {
            const childNodes = []
            const edges = this.graph.getOutgoingEdges(node) || []
            edges.forEach(edge => {
                const targetNode = this.graph.getCellById(edge.getTargetCellId())
                if (targetNode) {
                    childNodes.push(targetNode.id)
                    childNodes.push(...this.getChildNodes(targetNode))
                }
            })
            return childNodes
        },
        /**
         * 查找现有的连接线
         */
        edgeExists(node1, node2) {
            const edges = this.graph.getEdges()
            return edges.some(
                edge =>
                    (edge.getSourceCellId() === node1.id &&
                        edge.getTargetCellId() === node2.id) ||
                    (edge.getSourceCellId() === node2.id &&
                        edge.getTargetCellId() === node1.id)
            )
        },
        /**
         * 删除多余的连接线
         */
        removeConnections(node) {
            const edges = this.graph.getConnectedEdges(node)
            const targetEdges = edges.filter(
                edge => edge.getTargetCellId() === node.id
            )
            if (targetEdges.length > 1) {
                // 如果target连线多于一个，删除距离最远的那个
                let maxDistance = 0
                let edgeToRemove = null

                targetEdges.forEach(edge => {
                    const sourceNode = this.graph.getCellById(edge.getSourceCellId())
                    const distance = this.calculateDistance(sourceNode, node)
                    if (distance > maxDistance) {
                        maxDistance = distance
                        edgeToRemove = edge
                    }
                })

                if (edgeToRemove) {
                    this.graph.removeEdge(edgeToRemove)
                }
            }
        },
        /**
         * 计算距离
         */
        calculateDistance(node1, node2) {
            const bbox1 = node1.getBBox()
            const bbox2 = node2.getBBox()
            const center1 = {
                x: bbox1.x + bbox1.width / 2,
                y: bbox1.y + bbox1.height / 2
            }
            const center2 = {
                x: bbox2.x + bbox2.width / 2,
                y: bbox2.y + bbox2.height / 2
            }

            return Math.sqrt(
                Math.pow(center1.x - center2.x, 2) + Math.pow(center1.y - center2.y, 2)
            )
        },
        /**
         * 关闭配置面板
         */
        handleCloseConfig() {
            this.resetEdgeAndNodeStyles()
            this.isRelationConfig = false
            this.graph.resize(
                this.$refs.relationChartOuter.clientWidth,
                this.$refs.relationChartOuter.clientHeight +
                    this.$refs.relationConfig.clientHeight
            )
        },
        /**
         * 重置边和节点样式
         */
        resetEdgeAndNodeStyles() {
            const nodes = this.graph.getNodes()
            const edges = this.graph.getEdges()

            nodes.forEach(node => {
                node.attr('body/stroke', '#31d0c6')
            })

            edges.forEach(edge => {
                edge.attr('line/stroke', '#5F95FF')
            })
        },
        /**
         * 获取所有的数据
         */
        getDataForAllNodesAndEdges() {
            const nodes = this.graph
                .getNodes()
                .map(nodeItem => { Object.assign(nodeItem.data, { id: nodeItem.id, type: 'node', label: nodeItem.attrs.label.text, guiX: nodeItem.position().x, guiY: nodeItem.position().y }); return nodeItem.data })
            const edges = this.graph.getEdges().map(edgeItem => {
                const sourceNode = this.graph.getCellById(edgeItem.source.cell)
                const targetNode = this.graph.getCellById(edgeItem.target.cell)
                return { id: edgeItem.id, type: 'edge', label: `${sourceNode.data.tableName} - ${targetNode.data.tableName}`, sourceNodeId: sourceNode.id, targetNodeId: targetNode.id, sourceNodeName: sourceNode.data.tableName, targetNodeName: targetNode.data.tableName, symbleList: edgeItem.data.symbleList }
            })
            this.datas = [...nodes, ...edges]
        },
        /**
         * 格式化数据
         */
        handleFormat() {
            const data = this.graph.toJSON()
            const dagreNodes = data.cells
                .filter(cell => cell.shape === 'rect')
                .map(node => ({
                    id: node.id,
                    label: node.attrs.label.text,
                    width: node.size.width,
                    height: node.size.height
                }))

            const dagreEdges = data.cells
                .filter(cell => cell.shape === 'edge')
                .map(edge => ({
                    source: edge.source.cell,
                    target: edge.target.cell
                }))

            const dagreData = {
                nodes: dagreNodes,
                edges: dagreEdges
            }

            const dagreLayout = new DagreLayout({
                type: 'dagre',
                rankdir: 'LR',
                ranksep: 100,
                nodesep: 20
            })
            const model = dagreLayout.layout(dagreData)
            model.nodes.forEach(node => {
                const graphNode = this.graph.getCellById(node.id)
                graphNode.position(node.x, node.y)
            })
            this.resize()
        },
        /**
         * 格式化数据居中处理
         */
        centerNodesVertically() {
            const containerHeight = this.$refs.relationChartOuter.clientHeight
            const nodes = this.graph.getNodes()
            let minY = Infinity
            let maxY = -Infinity
            nodes.forEach(node => {
                const { y, height } = node.getBBox()
                if (y < minY) minY = y
                if (y + height > maxY) maxY = y + height
            })
            const graphHeight = maxY - minY
            const offsetY = (containerHeight - graphHeight) / 2 - minY
            nodes.forEach(node => {
                const { x, y } = node.getPosition()
                node.position(x, y + offsetY)
            })
        },
        /**
         * 鼠标到节点的距离
         */
        mouseCalculateDistance(node1, mouseX, mouseY) {
            const bbox1 = node1.getBBox()
            const center1 = {
                x: bbox1.x + bbox1.width / 2,
                y: bbox1.y + bbox1.height / 2
            }
            return Math.sqrt(
                Math.pow(center1.x - mouseX, 2) + Math.pow(center1.y - mouseY, 2)
            )
        },
        /**
         * 菜单筛选
         */
        filterNode(value, data) {
            if (!value) return true
            return data.name.indexOf(value) !== -1 || data.comment.indexOf(value) !== -1
        },
        /**
         * 多文字进行折行处理
         */
        foldedText(text, len = 10) {
            if (text && text.length > len) {
                return text.slice(0, len) + '\n' + text.slice(len)
            } else {
                return text
            }
        },
        /**
         * 选择节点
         */
        onSelectChange(id) {
            this.nowData = id
            const cell = this.graph.getCellById(id)
            this.resetEdgeAndNodeStyles()
            if (cell.isNode()) {
                cell.attr('body/stroke', this.shapeSelectionColor)
            } else {
                cell.attr('line/stroke', this.shapeSelectionColor)
            }
            this.isRelationConfig = true
            this.$nextTick(() => {
                this.handleFormat()
            })
        },
        /**
         * 加载字段信息
         */
        loadFields(node) {
            this.$ajax({
                url: `${this.getTableFields}/${node.data.datasourceId}/${node.data.tableName}`,
                method: 'get',
                data: {
                }
            }).then(res => {
                if (res.code === 0) {
                    const resData = res.data.map(item => {
                        return {
                            name: item.name,
                            type: item.type,
                            alias: item.remark,
                            remark: item.remark,
                            defaultValue: item.defaultValue || '',
                            dictConfId: '',
                            dictName: ''
                        }
                    })
                    if (this.fields[node.id]) {
                        this.mergeFields(resData, this.fields[node.id], node.id)
                    } else {
                        this.$set(this.fields, node.id, resData)
                    }
                }
            })
        },
        /**
         * 查询数据库字段，更新别名
         * @param {*} newData
         * @param {*} oldData
         * @param {*} id
         */
        mergeFields(newData, oldData, id) {
            const oldDataMap = oldData.reduce((all, item) => {
                all[item.name] = item
                return all
            }, {})
            newData.forEach(item => {
                if (oldDataMap[item.name]) {
                    item.alias = oldDataMap[item.name].alias
                    item.defaultValue = oldDataMap[item.name].defaultValue || ''
                    item.dictConfId = oldDataMap[item.name].dictConfId || ''
                    item.dictName = oldDataMap[item.name].dictName || ''
                }
            })
            this.$set(this.fields, id, newData)
        },
        onJoined(id) {
            const edge = this.graph.getCellById(id)
            edge.removeLabelAt(0)
        },
        /**
         * 校验数据完整性
         */
        isValid() {
            let isValid = true

            this.datas.filter(item => item.type === 'edge').some(item => {
                return item.symbleList.some(symble => {
                    if (!symble.sourceColumnName || !symble.targetColumnName) {
                        isValid = false
                        this.onSelectChange(item.id)
                        this.$message.warning('请配置关联关系')
                        return true
                    }
                    return false
                })
            })
            const nodes = this.datas.filter(item => item.type === 'node')
            nodes.some(item => {
                // 有单独的节点
                if (nodes.length !== 1 && !this.graph.getEdges().reduce((all, edge) => {
                    if (edge.getTargetCellId() === item.id || edge.getSourceCellId() === item.id) {
                        all = true
                    }
                    return all
                }, false)) {
                    // 有未关联节点
                    isValid = false
                    this.$message.warning(`请关联${item.aliasName}节点`)
                    return true
                }
                // 有未填写的字段描述
                const result = this.fields[item.id].some(field => {
                    if (!field.alias) {
                        isValid = false
                        this.onSelectChange(item.id)
                        this.$nextTick(() => {
                            this.$refs.relationChartConfig.scrollTo(field.name)
                        })
                        this.$message.warning(`请填写${field.name}的字段描述`)
                        return true
                    }
                    return false
                })
                if (!result) {
                    // 字段描述有重复
                    const alias = this.firstDuplicateField(this.fields[item.id], 'alias')
                    if (alias) {
                        this.$message.warning(`请修改重复的字段描述：${alias}`)
                        this.onSelectChange(item.id)
                        this.$nextTick(() => {
                            const finded = this.fields[item.id].find(field => { return field.alias === alias })
                            if (finded) {
                                this.$refs.relationChartConfig.scrollTo(finded.name)
                            }
                        })
                        isValid = false
                        return true
                    }
                    return false
                }
                return false
            })

            const aliasNameEmpty = this.datas.filter(item => { return item.type === 'node' }).filter(item => {
                return !item.aliasName
            })
            if (aliasNameEmpty.length) {
                const tableNames = aliasNameEmpty.map(item => item.tableName).join('、')
                this.$message.warning(`请设置节点: ${tableNames} 的别名`)
                isValid = false
            }

            // 节点别名不能重复
            const aliasName = this.firstDuplicateField(this.datas.filter(item => { return item.type === 'node' }), 'aliasName')
            if (aliasName) {
                this.$message.warning(`请修改重复的节点别名：${aliasName}`)
                isValid = false
            }

            this.$refs.form.validate(valid => {
                if (!valid) {
                    isValid = valid
                }
            })
            return isValid
        },
        /**
         * 获取完整接口数据
         */
        getData() {
            return {
                'relationId': this.form.relationId,
                'relationName': this.form.relationName,
                'bizGroupId': this.form.bizGroupId,
                'nodeList': this.datas.filter(item => item.type === 'node').map(nodeItem => {
                    return {
                        cellId: nodeItem.id,
                        guiX: nodeItem.guiX,
                        guiY: nodeItem.guiY,
                        commentName: nodeItem.commentName,
                        tableName: nodeItem.tableName,
                        aliasName: nodeItem.aliasName,
                        datasourceId: nodeItem.datasourceId,
                        datasourceName: nodeItem.datasourceName,
                        hidden: nodeItem.hidden || false,
                        componentJson: JSON.stringify(this.fields[nodeItem.id])
                    }
                }),
                'edgeList': this.datas.filter(item => item.type === 'edge').map(edgeItem => {
                    return {
                        edgeId: edgeItem.id,
                        sourceCellNodeId: edgeItem.sourceNodeId,
                        targetCellNodeId: edgeItem.targetNodeId,
                        componentJson: JSON.stringify(edgeItem.symbleList)
                    }
                })
            }
        },
        /**
         * 缓存初始数据的md5
         * @param {*} data
         */
        initDetailMd5() {
            this.detailCache = this.getMd5(JSON.stringify(this.excludeXY(this.getData())))
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
         * 判断数据是否有变化
         */
        isChange() {
            const newDetail = this.getMd5(JSON.stringify(this.excludeXY(this.getData())))
            return newDetail !== this.detailCache
        },
        /**
         * 数据是否有变化，排除坐标
         * @param {*} data
         */
        excludeXY(data) {
            if (Array.isArray(data.nodeList)) {
                data.nodeList.forEach(item => {
                    delete item.guiX
                    delete item.guiY
                })
            }
            return data
        },
        onSave() {
            if (!this.isValid()) {
                return
            }
            const data = this.getData()
            if (!data.relationName) {
                return
            }
            this.$emit('save', data)
        },
        firstDuplicateField(arr, field) {
            const uniqueValues = new Set()
            let fieldResult = ''
            arr.some(item => {
                if (uniqueValues.has(item[field])) {
                    fieldResult = item[field]
                    return true
                } else {
                    uniqueValues.add(item[field])
                    return false
                }
            })
            return fieldResult
        },
        onAliasNameChange() {
            const targetNode = this.graph.getCellById(this.nowData)
            targetNode.updateAttrs({
                label: {
                    text: this.foldedText(targetNode.data.tableName, 13) + '\n' + this.foldedText(targetNode.data.aliasName, 10),
                    fill: '#6a6c8a'
                }
            })
            this.getDataForAllNodesAndEdges()
        }
    }
}
</script>

<style scoped lang="scss">
.relation-chart {
  display: flex;
  height: 100%;
  width: 100%;
  .relation-chart-menu {
    width: 200px;
    height: 100%;
    border-right: 1px #e8e8e8 solid;
    overflow: auto;
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
  .relation-chart-filter {
    padding: 10px;
  }
  .relation-chart-main {
    width: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    .relation-chart-canvas {
      position: relative;
      flex: 1;
      height: 0;
      width: 100%;
      .relation-chart-ins {
        width: 100%;
        height: 100%;
      }
      .relation-chart-name {
        position: absolute;
        top: 10px;
        left: 10px;
        & > .el-input {
          width: 100%;
        }
        display: flex;
        justify-content: flex-start;
        align-items: flex-start;
        gap: 0 10px;
      }
    }
  }
}

::v-deep .el-tree-node__content {
  padding-left: 0 !important;
  height: auto;
}

</style>
