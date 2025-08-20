<template>
    <div class="report-guide">
        <div class="report-guide-highlight" :style="stepStyle" />
        <div class="report-guide-image">
            <video :src="guideVideoPath" aria-hidden="true" muted="" class="login-mp4" autoplay="" loop="" playsinline="" draggable="true" />
        </div>
        <div class="report-guide-arrow" :style="arrowLeft" />
        <div class="report-guide-step" :style="stepLeft">
            {{ stepText }} <el-button v-if="step !== 0" size="mini" @click="handlePre">上一步</el-button> <el-button size="mini" @click="handleNext">{{ stepBtnName }}</el-button> <el-button type="text" @click="handleClose">跳过</el-button>
        </div>
    </div>
</template>
<script>
import DatasetMp4 from '../../assets/dataset.mp4'
import DesignerMp4 from '../../assets/designer.mp4'
import FormMp4 from '../../assets/form.mp4'
const stepTexts = ['步骤1. 创建数据集（1/3）', '步骤2.设计报表（2/3）', '步骤3.设计报表筛选器（3/3）']
const stepImages = [DatasetMp4, DesignerMp4, FormMp4]

export default {
    data() {
        return {
            top: 0,
            left: 0,
            width: 0,
            height: 0,
            step: 0
        }
    },
    computed: {
        stepStyle() {
            return {
                top: `${this.top}px`,
                left: `${this.left}px`,
                width: `${this.width}px`,
                height: `${this.height}px`
            }
        },
        stepLeft() {
            return {
                left: `${this.left + 180}px`
            }
        },
        arrowLeft() {
            return {
                left: `${this.left + 85}px`
            }
        },
        stepText() {
            return this.step < 3 ? stepTexts[this.step] : ''
        },
        guideVideoPath() {
            return this.step < 3 ? stepImages[this.step] : ''
        },
        stepBtnName() {
            return this.step < 2 ? '下一步' : '关闭'
        }
    },
    created() {
        this.width = 100
        this.height = 30
        this.top = document.body.clientHeight - 30
    },
    methods: {
        handleNext() {
            this.step = this.step + 1
            if (this.step === 1) {
                this.left = 100
            } else if (this.step === 2) {
                this.left = 200
            } else if (this.step === 3) {
                this.handleClose()
            }
        },
        handlePre() {
            this.step = this.step - 1
            if (this.step === 1) {
                this.left = 100
            } else if (this.step === 2) {
                this.left = 200
            } else {
                this.left = 0
            }
        },
        handleClose() {
            this.$emit('close')
        }
    }
}
</script>
<style lang="scss" scoped>
    .report-guide {
        position: absolute;
        z-index: 99999999;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        .report-guide-highlight {
            position: absolute;
            box-shadow: 0 0 0 5000px rgba(0, 0, 0, 0.5);
            border-radius: 5px;
            transition: all 0.3s ease-out;
        }
        .report-guide-image {
            position: absolute;
            width: 76%;
            height: 76%;
            top: 5%;
            left: 13%;
          .login-mp4 {
              max-width: 100%;
              min-width: 100%;
              height: auto;
              position: fixed;
              top:0;
              left:0;
              visibility: visible;
              position: relative;
              object-fit: cover;
              transition-property:opacity;
              transition-timing-function: cubic-bezier(.4,0,.2,1);
              transition-duration: .3s;
              opacity:1;
              vertical-align: middle;
              pointer-events: none;
          }
        }
        .report-guide-step {
            color: #FFFFFF;
            position: absolute;
            bottom: 75px;
        }
        .report-guide-arrow {
            position: absolute;
            width: 100px;
            height: 100px;
            background-size: 100px;
            background-repeat: no-repeat;
            background-position: top center;
            bottom: 20px;
            background-image: url('../x-spreadsheet/assets/arrow.svg');
            transform: rotate(140deg);
        }
    }
</style>
