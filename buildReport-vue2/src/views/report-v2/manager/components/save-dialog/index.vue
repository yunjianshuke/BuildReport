<template>
    <el-dialog
        title="保存报表"
        :visible.sync="dialogVisible"
        append-to-body
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        width="400px"
        class="report-designer-dialog"
    >
        <el-form ref="saveReprotRuleForm" :rules="rules" :model="form" label-width="90px" size="small">
            <el-form-item label="报表名称:" prop="name">
                <el-input v-model="form.name" placeholder="请输入内容" size="small" />
            </el-form-item>
            <el-form-item label="报表分组:" prop="groupId">
                <el-cascader
                    v-model="form.groupId"
                    style="width: 100%;"
                    :options="datas"
                    :props="{ checkStrictly: true, emitPath: false, label: 'name', value: 'id' }"
                    placeholder="请选择分组"
                    clearable
                />
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button size="small" @click="dialogVisible=false">取消</el-button>
            <el-button type="primary" size="small" @click="save">确定</el-button>
        </span>
    </el-dialog>
</template>
  
<script>
export default {
    data: function() {
        return {
            dialogVisible: false,
            form: {
                name: '',
                groupId: ''
            },
            rules: {
                name: [
                    { required: true, message: '请输入报表名称', trigger: 'blur' }
                ],
                groupId: [
                    { required: true, message: '请输入报表分组', trigger: 'blur' }
                ]
            },
            datas: [],
            cb: null
        }
    },
    methods: {
        handleSave(params, cb) {
            this.cb = cb
            this.params = params
            const groupId = params.groupId
            const name = params.name

            if (groupId) {
                this.form.groupId = groupId
            }
            if (name) {
                this.form.name = name
            }
            if (!name) {
                this.loadTree()
                this.dialogVisible = true
            } else {
                this.doSave()
            }
        },
        save() {
            this.$refs['saveReprotRuleForm'].validate(valid => {
                if (valid) {
                    this.doSave()
                }
            })
        },
        async doSave() {
            this.$ajax({
                url: '/reportInfo/save',
                method: 'post',
                data: Object.assign(this.params, this.form)
            }).then(res => {
                if (res.code === 0) {
                    this.dialogVisible = false
                    this.$emit('saved', res.data)
                    if (this.cb) {
                        this.cb()
                    }
                }
            })
        },
        // 获取tree
        loadTree(data = {}) {
            this.$ajax({
                url: '/reportGroup/tree',
                method: 'post',
                data: {
                    ...data
                } 
            }).then(data => {
                if (data.code === 0 && data.data) {
                    const datas = data.data
                    this.datas = datas
                } else if (data.code === 0 && !data.data) {
                    this.datas = []
                }
            })
        }
    }
}
</script>
  
