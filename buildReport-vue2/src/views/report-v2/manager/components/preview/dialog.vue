<!-- eslint-disable no-debugger -->
<template>
    <BusinessDialog
        :title="title"
        :visible.sync="visible"
        :before-close="handleClose"
        :append-to-body="true"
        :fullscreen="false"
        :footer="false"
        :modal="false"
        :width="'60%'"
        class="xreport-preview-dialog"
        @fullscreenChange="handleFullScreen"
    >
        <template #body>
            <report-preview v-if="visible" ref="reportPreview" :preview-url="previewUrl" @closeParent="close" />
        </template>
    </BusinessDialog>
</template>

<script>
import ReportPreview from './index.vue'
import BusinessDialog from '@/components/BusinessDialog/index.vue'
export default {
    name: 'ReportPreviewDialog',
    components: {
        BusinessDialog,
        ReportPreview
    },
    props: {
        title: {
            type: String,
            default: '预览'
        },
        previewUrl: {
            type: String,
            default: '/reportInfo/preview/async'
        },
        beforeClose: {
            type: Function,
            default() {
                return done => {
                    done()
                }
            }
        }
    },
    data() {
        return {
            visible: false
        }
    },
    methods: {
        // 打开弹窗，每次初始化配置以应用配置更新
        open(data) {
            
            if (this.visible) this.close()
            this.visible = true
            this.$nextTick(() => {
                this.$refs.reportPreview.open(data)
            })
        },
        handleClose(done) {
            if (this.beforeClose) {
                this.beforeClose(done)
            } else {
                done()
            }
        },
        close() {
            this.visible = false
            this.$emit('close')
        },
        handleFullScreen() {
            this.$nextTick(() => {
                this.$refs.reportPreview && this.$refs.reportPreview.sheetObj && this.$refs.reportPreview.sheetObj.sheetReset()
            })
        }
    }
}
</script>
<style>
.xreport-preview-dialog {
  pointer-events: none;
}
</style>
<style lang="scss" scoped>

  .xreport-preview-dialog ::v-deep .el-dialog{
    pointer-events: auto;
  }
  .xreport-preview-dialog ::v-deep .el-dialog__body{
      height: calc(100% - 58px) !important;
      padding: 0;
      position: relative;
      .formContainer {
        padding: 0;
        .fm-form {
          padding: 0;
        }
      }
  }
</style>
