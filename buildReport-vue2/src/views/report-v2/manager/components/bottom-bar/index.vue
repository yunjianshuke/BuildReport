<template>
    <div class="designer-v2-bottombar">
        <div v-for="item in list" :key="item.value" class="bottombar-item" :class="{ 'current': value === item.value }" @click="onChange(item.value)">{{ item.label }}</div>
    </div>
</template>

<script>
export default {
    name: 'DesignerBottomBar',
    props: {
        value: {
            type: String,
            default: 'designer'
        },
        validChange: {
            type: Function,
            default: null
        },
        list: {
            type: Array,
            default() {
                return []
            }
        }
    },
    methods: {
        onChange(current) {
            if (this.validChange && !this.validChange(current, this.value)) {
                return 
            }
            this.$emit('update:value', current)
            this.$emit('change', current)
        }
    }
}
</script>

<style scoped lang="scss">
.designer-v2-bottombar {
    width: 100%;
    height: 30px;
    display: flex;
    border-top: 1px solid #CCC;
    border-bottom: 1px solid #CCC;
    .bottombar-item {
        text-align: center;
        width: 100px;
        line-height: 30px;
        border-right: 1px solid #CCC;
        cursor: pointer;
        &.current {
            background-color: #FFFFFF;
        }
    }
    background-color: #e6e6e6;
}
</style>
