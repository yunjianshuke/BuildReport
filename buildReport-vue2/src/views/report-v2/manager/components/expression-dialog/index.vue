<script>
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api'
import SvgIcon from '@/components/SvgIcon/index.vue'
// 定义编辑器主题
monaco.editor.defineTheme('rule-config', {
    base: 'vs',
    inherit: true,
    rules: [
        { token: '', foreground: '000000', background: 'fffffe' }
    ],
    colors: {
        'editorIndentGuide.background': '#fffa05',
        'editorIndentGuide.activeBackground': '#04fc08',
        // 'editorIndentGuide.activeBackground': '#04fc08',
        'editor.foreground': '#0be3bd', // 输入法输入的时候，输入文本的颜色
        'editor.inactiveSelectionBackground': '#ee6a09', // 编辑器失焦时，被选中的文本的颜色
        'editorCursor.foreground': '#771818', // 光标颜色
        'editorCursor.background': '#fc0a0a', // 光标颜色
        'editor.selectionBackground': '#add6ff', // 文本被选中的颜色
        'editor.lineHighlightBackground': '#fdfdd4', // 选中的当前行的颜色
        'editorLineNumber.foreground': '#0B216F', // 行号颜色
        'editorLineNumber.activeForeground': '#237893', // 选中的行号颜色
        // 'editor.selectionBackground': '#fdfdd4',
        // 'editor.selectionHighlightBackground': '#ADD6FF26'
        'editor.selectionHighlightBackground': '#c41e71'
    }
})
monaco.editor.setTheme('rule-config')
export default {
    name: 'RuleConfig',
    components: { SvgIcon },
    props: {
        funcUrl: {
            type: String,
            default: '/etl2/func'
        },
        symbolUrl: {
            type: String,
            default: '/etl2/symbol'
        }
    },
    data() {
        return {
            dialogVisible: false,
            fullScreen: false,
            inputFields: [],
            func: [],
            symbol: [],
            collapse: { inputFields: false },
            funName: '',
            activeNames: '',
            defaultProps: {
                children: 'funList',
                label: 'funName'
            }
        }
    },
    computed: {
        treeData() {
            return this.func.filter(item => item.funList).map(item => {
                return {
                    id: item.id,
                    funName: item.groupName,
                    funList: item.funList.map(item => { 
                        item.isLeaf = true
                        return item
                    }),
                    isLeaf: false
                }
            })
        }
    },
    watch: {
        funName(val) {
            this.$refs.tree.filter(val)
        }
    },
    methods: {
        open(params) {
            this.expression = params.expression
            this.inputFields = params.inputFields
            this.dialogVisible = true
            this.$nextTick(() => {
                this.initFun()
                this.initEditor()
            })
        },
        initFun() {
            Promise.all([this.$ajax({
                url: `${this.funcUrl}`,
                method: 'get'
            }), this.$ajax({
                url: `${this.symbolUrl}`,
                method: 'get'
            })]).then(([res1, res2]) => {
                if (res1.code === 0 && res2.code === 0) {
                    this.func = res1.data
                    this.symbol = res2.data
                }
            })
        },
        initEditor() {
            if (!this.editor) {
                this.editor = monaco.editor.create(this.$refs.editor, {
                    language: 'javascript',
                    automaticLayout: true,
                    minimap: { enabled: false },
                    fixedOverflowWidgets: false,
                    // renderValidationDecorations: 'off',
                    overviewRulerBorder: false,
                    // theme: 'vs',
                    theme: 'rule-config',
                    codeLens: false,
                    tabSize: 2,
                    fontSize: '16px'
                })
            }
            this.editor.setValue(this.expression)
        },
        handleClose() {
            this.dialogVisible = false
        },
        handleSubmit() {
            this.$emit('change', this.editor.getValue())
        },
        onInputDragEnd(event) {
            this.addTextToMonacoEditor(event)
        },
        addTextToMonacoEditor(item) {
            const selection = this.editor.getSelection()
            const textToInsert = `${item.text}`
            this.editor.executeEdits('my-source', [{ range: selection, text: textToInsert }])
            this.editor.focus()
        },
        handleSymbol(item) {
            const selection = this.editor.getSelection()
            const textToInsert = `${item.symbolCode}`
            this.editor.executeEdits('my-source', [{ range: selection, text: textToInsert }])
        },
        filterNode(value, data) {
            if (!value) return true
            return data.funName.indexOf(value) !== -1
        }
    }
}
</script>

<template>
    <el-dialog
        class="ruleConfigPopup"
        title="表达式"
        :before-close="handleClose"
        :visible="dialogVisible"
        :fullscreen="fullScreen"
        :close-on-click-modal="false"
        :show-close="false"
        top="10vh"
        append-to-body
    >
        <div class="rule-wrap">
            <div class="left">
                <div class="label">字段列表</div>
                <div class="label-desc">拖拽下方字段到编辑器区域</div>
                <div class="fields-container">
                    <div class="input-fields">
                        <div class="field-node">
                            <drag
                                v-for="(fieldItem, nodeIndex) in inputFields"
                                :key="nodeIndex"
                                :transfer-data="{ text: `${fieldItem.coordinates}` }"
                            >
                                <div
                                    class="node-com"
                                >
                                    <div class="icon">
                                        <svg-icon :name="`icondf-iconfield_${fieldItem.type}`" />
                                    </div>
                                    <el-tooltip placement="top" effect="light" :content="fieldItem.remark" :disabled="!fieldItem.remark">
                                        <span class="field-name">{{ fieldItem.name }}({{ fieldItem.coordinates }})</span>
                                    </el-tooltip>
                                </div>
                            </drag>
                        </div>
                    </div>
                </div>
            </div>
            <div class="center">
                <div class="symbol">
                    常用符号 <el-tooltip v-for="item in symbol" :key="item.id" placement="top" effect="light" :content="item.symbolDescription" :disabled="!item.symbolDescription"><div class="symbol-item" @click.stop="handleSymbol(item)">{{ item.symbolName }}</div></el-tooltip>
                </div>
                <drop style="width: 100%;height: 100%;" @drop="onInputDragEnd">
                    <div ref="editor" class="editor-container" />
                </drop>
            </div>
            <div class="right">
                <div class="right-header">
                    <div class="title">转换函数</div>
                    <div class="sub-title">拖拽转换函数到编辑器区域</div>
                    <div class="search"><el-input v-model="funName" left-icon="icon-search" placeholder="搜索函数名" /></div>
                </div>
                <div class="right-body">
                    <div class="group">
                        <el-tree
                            ref="tree"
                            class="filter-tree"
                            :data="treeData"
                            :props="defaultProps"
                            default-expand-all
                            :filter-node-method="filterNode"
                        >
                            <div slot-scope="{ data }" class="custom-tree-node">
                                <span v-if="!data.isLeaf">{{ data.funName }}</span>
                                <el-tooltip v-else placement="left" effect="light" popper-class="fun-tip">
                                    <div slot="content" class="fun-tip-body">
                                        <div v-html="data.funCode" />
                                        <hr>
                                        <span class="title"><i class="el-icon-info" />  介绍：</span><br>
                                        <div v-html="data.introduce" />
                                        <br>
                                        <span class="title"><i class="el-icon-info" />  用法：</span><br>
                                        <div v-html="data.instruct" />
                                        <br>
                                        <span class="title"><i class="el-icon-info" />  示例：</span><br>
                                        <div v-html="data.example" />
                                    </div>
                                    <drag
                                        :transfer-data="{ text: data.funCode }"
                                        style="padding-left: 6px; width: 100%;"
                                    >
                                        <div class="title-container">
                                            <div class="title text-line-1">{{ data.funName }}</div>
                                            <div class="sub-title text-line-1">{{ data.funDescription }}</div>
                                        </div>
                                    </drag>
                                </el-tooltip>
                            </div>
                        </el-tree>
                        <!-- <el-collapse v-model="activeNames">
              <el-collapse-item v-for="item in treeData" v-if="item.funList" :key="item.key" :title="item.groupName" :name="item.id">
                <el-tooltip v-for="fun in item.funList" :key="item.id + fun.id" placement="left" effect="light" popper-class="fun-tip">
                  <div slot="content" class="fun-tip-body">
                    <div v-html="fun.funCode"></div>
                    <hr />
                    <span class="title"><i class="el-icon-info"></i>  介绍：</span><br />
                    <div v-html="fun.introduce"></div>
                    <br />
                    <span class="title"><i class="el-icon-info"></i>  用法：</span><br />
                    <div v-html="fun.instruct"></div>
                    <br />
                    <span class="title"><i class="el-icon-info"></i>  示例：</span><br />
                    <div v-html="fun.example"></div>
                  </div>
                  <drag
                    :transfer-data="{ text: fun.funCode }"
                  >
                    <div class="title-container">
                      <div class="title text-line-1">{{ fun.funName }}</div>
                      <div class="sub-title text-line-1">{{ fun.funDescription }}</div>
                    </div>
                  </drag>
                </el-tooltip>
              </el-collapse-item>
            </el-collapse> -->
                    </div>
                </div>
            </div>
        </div>
        <template #footer>
            <el-button @click.stop.prevent="handleClose">取消</el-button>
            <el-button type="primary" @click.stop.prevent="handleSubmit">确定</el-button>
        </template>
    </el-dialog>
</template>

<style scoped lang="scss">
.ruleConfigPopup {
  ::v-deep .el-dialog {
    width: 90%;
    height: 80%;
    min-width: 1200px;
    max-width: 1520px;
    .el-dialog__header {
      padding: 15px;
    }
    .el-dialog__body {
      padding: 15px;
      height: calc(100% - 108px);
      width: 100%;
      .rule-wrap {
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: space-between;
        .left, .right {
          width: 20%;
          height: calc(100% - 20px);
          border: 1px solid #d5dbe1;
          padding: 10px;
          margin-top: 20px;
        }
        .left {
          line-height: 1.5715;
          .label {
            font-size: 14px;
            color: #424242;
          }
          .label-desc {
            font-size: 12px;
            color: #939ca5;
          }
          .fields-container {
            height: calc(100% - 41px);
            overflow-y: auto;
            overflow-x: hidden;
            .input-fields, .output-fields {
              .fields-collapse {
                cursor: pointer;
                color: rgba(0,0,0,.6);
                padding: 12px 10px 8px;
                display: flex;
                align-items: center;
                i {
                  font-size: 12px;
                  margin-right: 5px;
                  transition: all .2s;
                  &.isCollapse {
                    transform: rotateZ(-90deg);
                  }
                }
                span {
                  font-size: 14px;
                }
              }
              .field-node {
                .node-com {
                  width: 100%;
                  display: flex;
                  margin-bottom: 6px;
                  cursor: move;
                  &:hover { background-color: #f7f8fa; }
                  .icon {
                    width: 20px;
                    height: 20px;
                    font-size: 20px;
                    margin-right: 5px;
                    display: flex;
                    .svg-icon {
                      vertical-align: initial;
                    }
                  }
                  .field-name {
                    overflow: hidden;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                    font-size: 14px;
                    color: rgba(0,0,0,.6);
                  }
                }
              }
            }
          }
        }
        .center {
          width: calc(60% - 20px);
          height: 100%;
          .symbol {
            height: 20px;
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: flex-start;
            .symbol-item {
              cursor: pointer;
              padding: 0 10px;
              height: 16px;
              line-height: 16px;
              border-right: 1px solid #d5dbe1;
              &:first {
                border-left: 1px solid #d5dbe1;
              }
            }
          }
          .editor-container {
            width: 100%;
            height: calc(100% - 20px);
            border: 1px solid #d5dbe1;
          }
        }
        .right {
          .right-header {
            width: 100%;
            height: 80px;
            .title {
              font-size: 14px;
              color: #424242;
            }
            .sub-title {
              font-size: 12px;
              color: #939ca5;
            }

            .search {
              margin: 10px 0;
            }
          }
          .right-body {
            width: 100%;
            height: calc(100% - 80px);
            overflow: hidden auto;
            margin-top: 10px;
            .title-container {
              width: 100%;
              padding: 6px 0;
              cursor: move;
              &:hover { background-color: #f7f8fa; }
            }

            .title {
              width: 100%;
              font-size: 14px;
              color: #424242;
              line-height: 16px;
            }
            .sub-title {
              width: 100%;
              font-size: 12px;
              color: #939ca5;
              line-height: 14px;
            }

            .text-line-1 {
              overflow: hidden;
              text-overflow: ellipsis;
              display: flex;
              display: -webkit-flex;
              line-clamp: 1; //控制几行隐藏
              -webkit-line-clamp: 1; //控制几行隐藏
              -webkit-box-orient: vertical;
            }

            .el-tree.filter-tree {
              .el-tree-node__content {
                height: auto;
                .el-tree-node__expand-icon.is-leaf {
                  display: none;
                }
              }
            }
          }
        }
      }
    }
  }
}

.fun-tip-body {
  width: 250px;
  line-height: 20px;
  hr {
    border-color: #d5dbe1;
    outline-color: #d5dbe1;
  }
  .title {
    color: #939ca5;
  }
  max-height: 400px;
  overflow-y: auto;
}
</style>
<style>
.el-tooltip__popper.is-light.fun-tip {
  border: 1px solid #d5dbe1;
}
.el-tooltip__popper.is-light.fun-tip[x-placement^=left] .popper__arrow {
  border-left-color: #d5dbe1;
}
</style>

