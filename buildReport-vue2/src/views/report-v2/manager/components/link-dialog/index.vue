<template>
    <el-dialog
        title="添加单元格链接"
        :visible.sync="dialogVisible"
        append-to-body
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        width="600px"
        class="report-link-dialog"
    >
        <el-form label-width="120px" size="small">
            <el-form-item label="选择目标报表:">
                <el-select v-model="reportId" clearable @change="(value) => { handleChange(value, true) }">
                    <el-option v-for="report in reportList" :key="report.reportId" :value="report.reportId" :label="report.name" />
                </el-select>
            </el-form-item>
            <el-form-item v-if="sourceFields.length" label="关联筛选项:">
                <el-button :disabled="filterDisabled" @click="handleAdd">添加筛选项</el-button>
            </el-form-item>
            <el-table v-if="sourceFields.length" :data="filterList" size="mini" style="width: 100%">
                <el-table-column
                    prop="target"
                    label="目标报表筛选项"
                >
                    <template slot-scope="scope">
                        <el-select v-model="scope.row.targetFieldIndex" @change="targetChange">
                            <el-option-group v-for="group in targetFields" :key="group.name" :label="group.name">
                                <el-option v-for="item in group.dataFilter" :key="item.index" :value="item.index" :label="item.alias" />
                            </el-option-group>
                        </el-select>
                    </template>
                </el-table-column>
                <el-table-column
                    prop="target"
                    label=""
                    width="30"
                >
                    <template>
                        =
                    </template>
                </el-table-column>
                <el-table-column
                    prop="source"
                    label="当前格的实际值"
                >
                    <template slot-scope="scope">
                        <el-select v-model="scope.row.sourceField" @change="sourceChange">
                            <el-option v-for="item in sourceFields" :key="item.key" :value="item.key" :label="item.alias" />
                        </el-select>
                    </template>
                </el-table-column>
        
                <el-table-column
                    label="操作"
                    width="60"
                >
                    <template slot-scope="scope">
                        <el-button type="text" size="mini" @click="handleDelete(scope.index)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button size="small" @click="dialogVisible=false">取消</el-button>
            <el-button type="primary" size="small" @click="save">确定</el-button>
        </span>
    </el-dialog>
</template>
  
<script>
export default {
    props: {
        authListUrl: {
            type: String,
            default: '/reportInfo/listByAuth'
        },
        datasetUrl: {
            type: String,
            default: '/reportInfo/getDateSet'
        }
    },
    data: function() {
        return {
            dialogVisible: false,
            reportId: '',
            reportList: [],
            filterList: [],
            sourceFields: [],
            targetFields: [],
            filterDisabled: true
        }
    },
    methods: {
        open(params) {
            this.reportId = params.targetReport
            this.filterList = params.condition.map(item => {
                item.targetFieldIndex = item.targetDatasetName + item.targetField
                return item
            })
            this.cell = params.cell
            this.sourceFields = params.sourceFields
            if (this.reportId) {
                this.handleChange(this.reportId, false)
            }
            this.$ajax({
                url: this.authListUrl,
                method: 'post'
            }).then(res => {
                if (res.code === 0) {
                    this.reportList = res.data || []
                    this.dialogVisible = true
                }
            })
        },
        close() {
            this.dialogVisible = false
        },
        save() {
            const condition = []

            this.filterList.forEach(item => {
                if (item.sourceField && item.targetField) {
                    condition.push({
                        targetField: item.targetField,
                        targetDatasetName: item.targetDatasetName,
                        sourceField: item.sourceField,
                        sourceDatasetName: item.sourceDatasetName
                    })
                }
            })
      
            this.$emit('change', {
                targetReport: this.reportId,
                condition
            })
        },
        handleAdd() {
            this.filterList.push({
                sourceField: this.cell.key,
                targetField: '',
                targetFieldIndex: '',
                sourceDatasetName: this.cell.dataset,
                targetDatasetName: ''
            })
        },
        handleDelete(index) {
            this.filterList.splice(index, 1)
        },
        handleChange(reportId, refresh) {
            this.$ajax({
                url: this.datasetUrl,
                method: 'post',
                data: {
                    reportId: reportId,
                    businessType: '0'
                }
            }).then(res => {
                if (res.code === 0) {
                    this.targetFields = res.data.map(item => {
                        const dataFilter = item.dataFilter ? JSON.parse(item.dataFilter) : []
                        const result = []
                        const dataFilterObj = {}
                        dataFilter.forEach(field => {
                            field.datasetName = item.name
                            field.index = item.name + field.key
                            if (!dataFilterObj[field.key]) {
                                dataFilterObj[field.key] = 1
                                result.push(field)
                            }
                        })
                        item.dataFilter = result
                        return item
                    })
                    if (this.targetFields.some(item => { return item.dataFilter.length > 0 })) {
                        this.filterDisabled = false
                    }
                    refresh && this.filterList.forEach(item => {
                        item.targetField = ''
                        item.targetDatasetName = ''
                        item.targetFieldIndex = ''
                    })
                }
            })
        },
        targetChange(target) {
            this.targetFields.forEach(item => {
                item.dataFilter.forEach(field => {
                    if (target === field.index) {
                        const current = this.filterList.find(sub => { return sub.targetFieldIndex === target })
                        if (current) {
                            current.targetField = field.key
                            current.targetDatasetName = field.datasetName
                        }
                    }
                }) 
            })
        },
        sourceChange(source) {
            const finded = this.sourceFields.find(item => {
                return item.key === source.key
            })
            if (finded) {
                const current = this.filterList.find(item => {
                    return item.sourceField === finded.key
                })
                if (current) {
                    current.sourceDatasetName = finded.datasetName
                }
            }
        }
    }
}
</script>
  
