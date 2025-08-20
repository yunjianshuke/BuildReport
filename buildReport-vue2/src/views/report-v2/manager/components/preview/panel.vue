<!-- eslint-disable no-debugger -->
<template>
    <div class="designer-preview-panel" :class="{ 'show': visible, 'full-screen': isFullScreen }" :style="{ height: boxHeight + 'px', minHeight: boxMinHeight + 'px' }">
        <div class="designer-preview-panel-topbar" @mousedown.prevent="startResize">
            预览
            <div>
                <el-button type="text" icon="el-icon-full-screen" @click="handleFullScreen" />
                <el-button type="text" icon="el-icon-close" @click="close" />
            </div>
        </div>
        <div class="designer-preview-panel-body">
            <report-preview v-if="visible" ref="reportPreview" :preview-url="previewUrl" @closeParent="close" />
        </div>
    </div>
</template>

<script>
import ReportPreview from './index.vue'
export default {
    name: 'ReportPreviewPanel',
    components: {
        ReportPreview
    },
    props: {
        previewUrl: {
            type: String,
            default: '/reportInfo/preview/sync'
        }
    },
    data() {
        return {
            visible: false,
            isFullScreen: false,
            boxHeight: 300,
            boxMinHeight: 260
        }
    },
    methods: {
        /**
         * 开始上下拖动
         * @param {*} event
         */
        startResize(event) {
            event.stopPropagation()
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
            event.stopPropagation()
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
            this.$nextTick(() => {
                this.$emit('resize')
                this.$refs.reportPreview && this.$refs.reportPreview.sheetObj && this.$refs.reportPreview.sheetObj.sheetReset()
            })
        },
        // 打开弹窗，每次初始化配置以应用配置更新
        open(data) {
            if (this.visible) this.close()
            setTimeout(() => {
                this.visible = true
                this.$nextTick(() => { 
                    this.$refs.reportPreview && this.$refs.reportPreview.open(data)
                })
            }, 0)
        },
        close() {
            this.visible = false
            this.$emit('close')
        },
        handleFullScreen() {
            this.isFullScreen = !this.isFullScreen
            this.$nextTick(() => {
                this.$emit('resize')
                this.$refs.reportPreview && this.$refs.reportPreview.sheetObj && this.$refs.reportPreview.sheetObj.sheetReset()
            })
        }
    }
}
</script>
  <style lang="scss" scoped>
    .designer-preview-panel {
      width: 100%;
      position: relative;
      flex-direction: column;
      display: none;
      background-color: #FFF;
      z-index: 13;
      &.full-screen {
        position: fixed;
        top: 0;
        left: 0;
        height: 100% !important;
        z-index: 2500;
        .designer-preview-panel-topbar {
          cursor: auto;
        }
      }
      &.show {
        display: flex;
      }
      .designer-preview-panel-topbar {
        cursor: ns-resize;
        height: 36px;
        background-color: #eee;
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 10px;
      }
      .designer-preview-panel-body {
        flex: 1;
        width: 100%;
        height: 0;
      }
    }
  </style>

