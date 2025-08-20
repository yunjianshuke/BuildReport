<template>
    <div class="relation-config-topbar">
        <el-select v-model="current" size="mini" @change="onSelectChange">
            <el-option-group v-for="group in groupOptions" :key="group.label" :label="group.label">
                <el-option v-for="item in group.options" :key="item.id" :label="item.type === 'node' ? item.label + ' (' + item.datasourceName + ')': item.label" :value="item.id" />
            </el-option-group>
        </el-select>
        <div class="collapse" @click="onCollapse">
            <svg-icon :name="isCollapse ? 'el-icon-arrow-up': 'el-icon-arrow-down'" />
        </div>
    </div>
</template>
  
<script>
import SvgIcon from '@/components/SvgIcon'
export default {
    name: 'ChartConfigTopbar',
    components: {
        SvgIcon
    },
    props: {
        datas: {
            type: Array,
            default() {
                return []
            }
        },
        nowData: {
            type: String,
            default: ''
        } 
    },
    data() {
        return {
            current: '',
            isCollapse: false
        }
    },
    computed: {
        // 组装分组下拉列表
        groupOptions() {
            const nodes = this.datas.filter(item => { return item.type === 'node' })
            const edges = this.datas.filter(item => { return item.type === 'edge' })
            const result = []
            if (nodes.length) {
                result.push({ label: '逻辑表', options: nodes })
            }
            if (edges.length) {
                result.push({ label: '关系', options: edges })
            }
            return result
        }
    },
    watch: {
        // 监听当前选中节点或边id变化
        nowData(value) {
            this.current = value
        }
    },
    mounted() {
        this.current = this.nowData
    },
    methods: {
        /**
         * 切换选中的节点或边
         */
        onSelectChange() {
            this.$emit('selectChange', this.current)
        },
        /**
     * 折叠
     */
        onCollapse() {
            this.isCollapse = !this.isCollapse
            this.$emit('collapse', this.isCollapse)
        }
    }
}
</script>
<style scoped lang="scss">
.relation-config-topbar {
    height: 36px;
    background-color: #eee;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 10px;
    .collapse {
      cursor: pointer;
    }
}
</style>
  
