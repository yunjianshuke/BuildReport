<template>
    <el-dialog
        title="添加图片"
        :visible.sync="dialogVisible"
        append-to-body
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        width="600px"
        class="report-link-dialog"
    >
        <el-form label-width="120px" size="small">
            <el-form-item label="上传图片:">
                <el-upload
                    ref="upload"
                    action="/"
                    class="avatar-uploader"
                    :multiple="false"
                    :on-change="handleChange"
                    :show-file-list="false"
                    :auto-upload="false"
                    accept="image/jpeg,image/jpg,image/png"
                >
                    <img v-if="base64" :src="base64" class="avatar">
                    <i v-else class="el-icon-plus avatar-uploader-icon" />
                </el-upload>
            </el-form-item>
            <el-form-item label="宽度:">
                <el-input v-model="width" />
            </el-form-item>
            <el-form-item label="高度:">
                <el-input v-model="height" />
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
            base64: '',
            width: '',
            height: ''
        }
    },
    methods: {
        open(params) {
            this.width = params.width
            this.height = params.height
            this.base64 = params.base64
            this.dialogVisible = true
        },
        close() {
            this.dialogVisible = false
        },
        save() {
            if (!this.base64) {
                this.$message.warning('请上传图片')
                return 
            }
            if (!this.width) {
                this.$message.warning('请填写宽度')
                return 
            }
            if (!this.height) {
                this.$message.warning('请填写高度')
                return 
            }
            this.$emit('change', {
                base64: this.base64,
                width: this.width,
                height: this.height
            })
        },
        handleChange(file) {
            if (file && file.raw) {
                const reader = new FileReader()
                reader.onload = event => {
                    const base64 = event.target.result
                    // 在这里使用base64字符串
                    this.base64 = base64
                }
                reader.readAsDataURL(file.raw) // 转换文件为Base64
            } else {
                this.base64 = ''
            }
        }
    }
}
</script>
<style lang="scss" scoped>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}
.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
  
