<template>
    <div v-if="visible" class="Bdialog">
        <el-dialog
            ref="bdialog"
            v-bind="$attrs"
            v-dialogDrag
            :class="[
                'Bdialog',
                {
                    'dialogClass_his': !isFullscreen,
                    'BDialogFullscreen': bodyFill && isFullscreen && footer,
                    'BDialogFullscreenNoFooter': bodyFill && isFullscreen && !footer,
                    'bodyNoPadding': bodyNoPadding
                }
            ]"
            :visible.sync="dialog.visible"
            :width="width"
            :fullscreen="isFullscreen"
            :title="title"
            :close-on-click-modal="modelclick"
            v-on="$listeners"
            @close="_close"
        >
            <template v-if="fullscreen" #title>
                <dialog-header :dialog-tittle="title" :fullscreen="isFullscreen" @is-fullscreen="_onFullscreen" />
            </template>
            <slot name="body" />
            <div v-if="footer" slot="footer" class="dialog-footer">
                <div slot="btns" align="right">
                    <slot name="otherBtns" />
                    <slot name="dialogBtns">
                        <el-button type="default" size="small" @click="_close">{{ !options.cancelBtn?'取消':options.cancelBtn }}</el-button>
                        <el-button type="primary" size="small" @click="options.confirm">{{ !options.sureBtn?'确定':options.sureBtn }}</el-button>
                    </slot>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import dialogHeader from '@/components/Dialog/dialogHeader'
export default {
    name: 'Bdialog',
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
        openfullscreen: {
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
        bodyFill: {
            type: Boolean,
            default: () => false
        },
        bodyNoPadding: {
            type: Boolean,
            default: () => false
        },
        footer: {
            type: Boolean,
            default() {
                return false
            }
        }
    },
    data() {
        return {
            isFullscreen: this.openfullscreen,
            dialog: {
                visible: true
            }
        }
    },
    watch: {
        visible: {
            handler(val) {
                this.dialog.visible = val
            },
            deep: true
        }
    },
    created() {},
    methods: {
        // 是否为全屏函数
        _onFullscreen(fullscreen) {
            this.isFullscreen = fullscreen
            this.$emit('fullscreen', this.isFullscreen)
            this.$emit('update:openfullscreen', this.isFullscreen)
            // 如果是全屏，需要将 dialog 的 top 和 left 属性移除，否则会出现位置偏移
            if (this.isFullscreen) {
                this.$nextTick(() => {
                    const dialogElement = this.$refs.bdialog.$el // 获取 el-dialog 元素
                    // 获取dialogelement下的第一个div元素
                    const dialogHeaderEl = dialogElement.querySelector('.el-dialog')
                    if (dialogHeaderEl) {
                        dialogHeaderEl.style.removeProperty('top') // 移除 top 属性
                        dialogHeaderEl.style.removeProperty('left') // 移除 left 属性
                    }
                })
            }
        },
        _close() {
            if (this.options.cancel) return this.options.cancel()
            this.$emit('update:visible', false)
            this.$nextTick(() => {
                this.$emit('close')
            })
        }
    }
}
</script>
<style lang="scss" scoped>
    .Bdialog ::v-deep .el-dialog .el-dialog__header {
        padding: 16px 24px;
        color: rgba(0, 0, 0, 0.65);
        background: #EEF4FF;
        border-bottom: 1px solid #e8e8e8;
        border-radius: 4px 4px 0 0;
    }
    .Bdialog ::v-deep .el-dialog {
        margin: 0 auto;
    }
    .Bdialog ::v-deep .el-dialog__footer {
        padding: 0 !important;
    }
    .Bdialog .dialog-footer {
        padding: 10px 16px;
        text-align: right;
        background: transparent;
        border-top: 1px solid #e8e8e8;
        border-radius: 0 0 4px 4px;
    }
    .Bdialog ::v-deep .bodyNoPadding .el-dialog__body {
        padding: 0;
    }
    .Bdialog ::v-deep .BDialogFullscreen .el-dialog__body {
        overflow: hidden;
        padding: var(--el-button-small-padding-vertical) var(--el-button-small-padding-horizontal);
        font-size: var(--el-button-small-font-size);
        height: calc(100% - var(--el-border-width-base) - var(--el-border-width-base) - var(--el-button-small-padding-vertical) - var(--el-button-small-padding-vertical) - var(--el-button-small-font-size) - 20px - var(--el-font-line-height-primary) - 34px);
    }
    .Bdialog ::v-deep .BDialogFullscreenNoFooter .el-dialog__body {
        overflow: hidden;
        height: calc(100% - var(--el-font-line-height-primary) - 34px);
    }
</style>