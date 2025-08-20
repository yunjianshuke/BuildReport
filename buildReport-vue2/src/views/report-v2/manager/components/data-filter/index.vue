<template>
    <div v-if="params.length > 0" class="data-filter">
        <div class="data-filter-body">
            <el-form size="mini" label-width="140px" style="margin-top: 8px;margin-left: 10px;display: flex; flex-wrap: wrap; align-items: flex-start; justify-content: flex-start;">
                <template v-for="(item, index) in params">
                    <el-form-item v-if="item.component.type" :key="index" :label="item.component.label">
                        <el-date-picker
                            v-if="item.type === 'DATETIME'"
                            v-model="item.component.value"
                            style="width: 100%;"
                            :type="item.dateType"
                            :value-format="item.dateFormat"
                            :format="item.dateFormat"
                            placeholder="选择默认值"
                            range-separator="至"
                            start-placeholder="选择默认值"
                            end-placeholder="选择默认值"
                            clearable
                        />
                        <el-date-picker
                            v-else-if="item.type === 'DATE'"
                            v-model="item.component.value"
                            style="width: 100%;"
                            :type="item.dateType"
                            :value-format="item.dateFormat"
                            :format="item.dateFormat"
                            placeholder="选择默认值"
                            clearable
                        />
                        <el-time-picker
                            v-else-if="item.type === 'TIME'"
                            v-model="item.component.value"
                            :picker-options="{
                                selectableRange: '00:00:00 - 23:59:59'
                            }"
                            :value-format="item.dateFormat"
                            placeholder="选择默认值"
                            clearable
                        />
                        <el-radio-group v-if="item.component.type === 'radio'" v-model="item.component.value">
                            <el-radio v-for="(obj) in item.component.options.data" :key="obj.value" :label="obj.value">{{ obj.label }}</el-radio>
                        </el-radio-group>
                        <el-checkbox-group v-if="item.component.type === 'checkbox'" v-model="item.component.value">
                            <el-checkbox v-for="(obj) in item.component.options.data" :key="obj.value" :label="obj.value">{{ obj.label }}</el-checkbox>
                        </el-checkbox-group>
                        <el-input
                            v-if="item.component.type === 'text'"
                            v-model="item.component.value"
                            style="width: 200px;"
                            placeholder="请输入内容"
                            clearable
                        />
                        <el-select
                            v-if="item.component.type === 'select'"
                            v-model="item.component.value"
                            placeholder="请选择"
                            filterable
                            :multiple="item.component.options.multiple"
                            :collapse-tags="item.component.options.multiple"
                            style="width: 200px;"
                            clearable
                        >
                            <el-option v-for="obj in item.component.options.data" :key="obj.value" :label="obj.label" :value="obj.value" />
                        </el-select>
                    </el-form-item>
                </template>
            </el-form>
        </div>
        <div class="data-filter-search">
            <div><el-button type="primary" size="mini" style="width: 60px;" @click="search">搜索</el-button> <el-button type="text" icon="el-icon-close" @click="close" /></div>
            <div><el-button size="mini" style="width: 60px;" @click="reset">重置</el-button></div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'ReportDataFilter',
    props: {
        params: {
            type: Array,
            default() {
                return []
            }
        }
    },
    methods: {
        search() {
            this.$emit('search')
        },
        reset() {
            this.params.forEach(item => {
                item.component.value = Array.isArray(item.component.value) ? [] : ''
            })
            this.search()
        },
        close() {
            this.$emit('close')
        }
    }
}
</script>
<style lang="scss" scoped>
.data-filter {
    width: 100%;
    height: 100px;
    background: #f5f6f7;
    position: absolute;
    left: 0;
    top: 0;
    display: flex;
    overflow-y: auto;
    .data-filter-body {
      flex: 1;
      width: 0;
      height: 100%;
    }
    .data-filter-search { 
      width: 96px;
      height: 70px;
      display: flex;
      flex-direction: column;
      position: sticky;
      position: -webkit-sticky;
      top: 8px;
      right: 10px;
    }
}
</style>
