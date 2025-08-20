<template>
    <el-dialog
        ref="bdialog"
        v-dialogDrag
        append-to-body
        v-bind="$attrs"
        class="BusinessDialog"
        :before-close="handleClose"
        :close-on-click-modal="false"
        :fullscreen="isFullscreen"
        :modal="modal"
        :show-close="showClose"
        :title="title"
        :visible="visible"
        :width="width"
        :class="{'fullscreenDialog': isFullscreen}"
        v-on="$listeners"
    >
        <dialog-header slot="title" :dialog-tittle="title" :fullscreen="isFullscreen" @is-fullscreen="_onFullscreen">
            <template #button>
                <slot name="btnName" />
            </template>
        </dialog-header>
        <div
            :style="{height: isFullscreen ? '100%' : localContentHeight || '60vh'}"
        >
            <div :class="preset_className">
                <div>
                    <slot name="title" />
                </div>
                <div class="fm-form">
                    <slot name="body" />
                </div>
                <div v-if="footer" class="formActions">
                    <div v-if="footerContent" class="footerContent">
                        <slot name="bottomContent" />
                    </div>
                    <div slot="btns" align="right">
                        <slot name="dialogBtns">
                            <el-button v-show="!options.cancelBtnNotShow" type="default" size="small" @click="_close">{{ !options.cancelBtn?'取消':options.cancelBtn }}</el-button>
                            <el-button type="primary" size="small" @click="_confirm">{{ !options.sureBtn?'确定':options.sureBtn }}</el-button>
                        </slot>
                    </div>
                </div>
                <div v-if="!footer&&footerContent" class="formActions">
                    <div v-if="footerContent" class="footerContent">
                        <slot name="bottomContent" />
                    </div>
                </div>
            </div>
        </div>
    </el-dialog>
</template>

<script>
import dialogHeader from '@/components/Dialog/dialogHeader'
export default {
    name: 'BusinessDialog',
    components: {
        'dialog-header': dialogHeader
    },
    props: {
        visible: {
            type: Boolean,
            default() {
                return false
            }
        },
        title: {
            type: String,
            default() {
                return ''
            }
        },
        /**
         * sureBtn: 确认按钮文案,
         * cancelBtn: 取消按钮文案,
         * confirm: 确认方法事件
         */
        options: {
            type: Object,
            default() {
                return {}
            }
        },
        modelclick: {
            type: Boolean,
            default() {
                return false
            }
        },
        fullscreen: {
            type: Boolean,
            default() {
                return false
            }
        },
        width: {
            type: String,
            default() {
                return '50%'
            }
        },
        footer: {
            type: Boolean,
            default() {
                return false
            }
        },
        footerContent: {
            type: Boolean,
            default() {
                return false
            }
        },
        showClose: {
            type: Boolean,
            default() {
                return true
            }
        },
        modal: {
            type: Boolean,
            default() {
                return true
            }
        },
        contentHeight: {
            type: String,
            default() {
                return ''
            }
        }
    },
    data() {
        return {
            childComponent: {},
            isFullscreen: false,
            localContentHeight: this.contentHeight
        }
    },
    computed: {
        preset_className() {
            // 弹窗或抽屉模式下的表单容器样式类名
            return this.fullscreen ? 'dialogContent' : 'formContainer'
        }
    },
    watch: {
        fullscreen: {
            handler(newVal) {
                this._onFullscreen(newVal)
            },
            immediate: true
        }
    },

    methods: {
        // 是否为全屏函数
        _onFullscreen(fullscreen) {
            console.log(this.localContentHeight, this.contentHeight, '60vh')
            this.isFullscreen = fullscreen
            // 如果是全屏，需要将 dialog 的 top 和 left 属性移除，否则会出现位置偏移
            if (this.isFullscreen) {
                this.$nextTick(() => {
                    this.localContentHeight = '100%'
                    const dialogElement = this.$refs.bdialog.$el// 获取 el-dialog 元素
                    // 获取dialogelement下的第一个div元素
                    const dialogHeaderEl = dialogElement.querySelector('.el-dialog')
                    if (dialogHeaderEl) {
                        dialogHeaderEl.style.removeProperty('top') // 移除 top 属性
                        dialogHeaderEl.style.removeProperty('left') // 移除 left 属性
                    }
                })
            } else {
                // 如果不是全屏，恢复原始的 contentHeight
                this.localContentHeight = this.contentHeight ||  '60vh'
                console.log(this.localContentHeight, this.contentHeight, '60vh1111')
            }
            this.$emit('fullscreenChange', fullscreen)
        },
        /**
         * @description: 关闭弹窗
         * @return {*}
         */
        handleClose() {
            this.$emit('update:visible', false)
        },
        _close() {
            this.handleClose()
        },
        _confirm() {
            this.options.confirm()
            // this.handleClose()
        }

    }
}
</script>
<style lang="scss" scoped>
.fm-form {
    height: 100%;
}
::v-deep .el-dialog {
    margin: 0 auto;
}
::v-deep .el-dialog__body {
    height: calc(100% - 58px);
    padding: 10px 0 54px;
}
::v-deep .el-dialog__header {
    padding: 16px 24px;
    color: rgba(0, 0, 0, 0.65);
    background: #fff;
    border-bottom: 1px solid #e8e8e8;
    border-radius: 4px 4px 0 0;
}
::v-deep .el-dialog__header {
    background: #EEF4FF;
}
::v-deep .formContainer .formActions{
    border-top: 1px solid #e8e8e8;
    padding: 0 24px;
    width: 100%;
    height: 59px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: flex-end;
    position: absolute;
    right: 0;
    box-sizing: border-box;
}
</style>