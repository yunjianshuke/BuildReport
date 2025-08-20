<!--最大化最小化组件封装-->
<template>
    <div class="header-title">
        <span class="title-name">{{ dialogTittle }}</span>
        <span
            style="float: right; margin-right: 20px; position: relative; top: 5px; color: #909399; cursor: pointer;"
            :class="flodIconClass"
            @click="() => isFullscreen"
        />
    </div>
</template>

<script>
export default {
    props: {
        dialogTittle: {
            type: String,
            default: () => ''
        },
        fullscreen: {
            type: Boolean,
            default: () => false
        }
    },
    data() {
        return {
            header_box: {
                'display': 'flex',
                'align-items': 'center'
            }
        }
    },
    computed: {
        flodIconClass() {
            return this.fullscreen ? 'el-icon-copy-document' : 'el-icon-full-screen'
        },
        // eslint-disable-next-line vue/return-in-computed-property
        isFullscreen() {
            this.$emit('is-fullscreen', !this.fullscreen)
        }
    },
    created() {

    },
    mounted() {

    },
    methods: {

    }
}
</script>
