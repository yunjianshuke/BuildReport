<template>
    <div
        class="designer-v2-menu"
        :style="{ width: menuWidth + 'px', minWidth: menuMinWidth + 'px' }"
    >
        <div class="tool-bar">
            <el-button
                class="dataset-add"
                size="mini"
                @click="onAddDataset"
            >
                选择数据集
            </el-button>
        </div>
        <div v-for="item, index in treeData" :key="item.datasetId" class="dataset-tables">
            <div class="dataset-name" @click="onDatasetExpand(index)">
                <i v-if="item.expand" class="el-icon-caret-bottom" />
                <i v-else class="el-icon-caret-right" />
                {{ item.datasetName }}
            </div>
            <template v-if="item.expand">
                <div v-for="table in item.tables" :key="table.key" class="dataset-table">
                    <template v-if="!table.hidden">
                        <div class="dataset-table-name">
                            <div>{{ table.aliasName }}</div>
                        </div>
                        <div class="dataset-table-fields">
                            <div
                                v-for="field in table.sqlFieldList"
                                :key="field.key"
                                class="el-checkbox el-checkbox--small is-bordered"
                                :title="field.name + '\n' + (field.alias || field.datasetName)"
                                draggable="true"
                                @dragstart="handleDragStart(field, $event)"
                            >
                                <div class="el-checkbox__label">
                                    {{ field.alias ||
                                        field.datasetName }}
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </template>
        </div>
        <div class="resize-handle-horizontal" :style="{ left: (menuWidth - 5) + 'px' }" @mousedown.prevent="startResizeHorizontal" />
        <div ref="moveNode" class="move-node">{{ alias }}</div>
    </div>
</template>

<script lang="js">
export default {
    name: 'DesignerTree',
    props: {
        data: {
            type: Array,
            default() {
                return []
            }
        }
    },
    data() {
        return {
            menuWidth: 200,
            menuMinWidth: 0,
            resizing: false,
            startX: 0,
            alias: ''
        }
    },
    computed: {
        treeData() {
            return this.data.map(item => {
                return {
                    ...item,
                    expand: true
                } 
            })
        }
    },
    methods: {
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
     * 左右拖动
     * @param {*} event 
     */
        doResizeHorizontal(event) {
            if (this.resizing) {
                const offset = event.clientX - this.startX
                this.menuWidth += offset
                if (this.menuWidth < this.menuMinWidth) {
                    this.menuWidth = this.menuMinWidth
                }
                this.startX = event.clientX
                this.$emit('resize')
            }
        },
        /**
     * 左右拖动结束
     */
        stopResizeHorizontal() {
            this.resizing = false
            document.removeEventListener('mousemove', this.doResizeHorizontal)
            document.removeEventListener('mouseup', this.stopResizeHorizontal)
        },
        onAddDataset() {
            this.$emit('adddataset')
        },
        onDatasetExpand(index) {
            this.treeData[index].expand = !this.treeData[index].expand
            this.$forceUpdate()
        },
        /**
     * 拖拽字段开始事件
    */
        handleDragStart(data, e) {
            const { datasetName, alias, key, aliasName } = data
            if (datasetName === alias) {
                e.preventDefault()
                return
            }
            e.dataTransfer.setData('dataset', datasetName)
            e.dataTransfer.setData('key', key)
            e.dataTransfer.setData('text/plain', alias)
            e.dataTransfer.setData('aliasName', aliasName)
            this.alias = alias
            this.$nextTick(() => {
                e.dataTransfer.setDragImage(this.$refs.moveNode, 20, 10)
            })
        }
    }
}
</script>

<style lang="scss" scoped>
.designer-v2-menu {
    height: 100%;
    border-right: 1px #e8e8e8 solid;
    flex-shrink: 0;
    overflow-y: auto;
    overflow-x: hidden;
    position: relative;
    .tool-bar {
      height: 40px;
      width: 100%;
      background-color: #f5f6f7;
      padding: 0 10px;
      display: flex;
      align-items: center;
      justify-content: flex-start;
    }

    ::v-deep .el-tree-node__content {
        padding-left: 0 !important;
    }

    .custom-tree-node {
        cursor: pointer;
        width: 0;
        height: 20px;
        line-height: 20px;
        user-select: none;
        -webkit-user-select: none;
        justify-content: flex-start;
    }

    .dataset-tables {
        width: 100%;
        display: flex;
        flex-direction: column;
        .dataset-name {
            font-size: 14px;
            width: 100%;
            line-height: 20px;
            padding: 10px;
            font-weight: bold;
            color: #409eff;
            cursor: pointer;
        }

        .dataset-table {
            .dataset-table-name {
                width: 100%;
                line-height: 20px;
                font-size: 14px;
                padding: 10px;
                padding-top: 0px;
                font-weight: bold;
                display: flex;
                align-items: center;
                justify-content: space-between;
            }

            .dataset-table-fields {
                padding: 10px;
                padding-top: 0px;
                
                .el-checkbox {
                    box-sizing: border-box;
                    width: 160px;
                    padding: 5px 10px;
                    margin-left: 0px;
                    margin-right: 10px;
                    margin-bottom: 10px;

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

    .resize-handle-horizontal {
        width: 5px;
        cursor: ew-resize;
        background-color: transparent;
        height: 100%;
        position: fixed;
        top: 0;
        right: 0;
    }
    .move-node {
        position: absolute;
        left:0;
        z-index: -1;
    }
}
</style>
