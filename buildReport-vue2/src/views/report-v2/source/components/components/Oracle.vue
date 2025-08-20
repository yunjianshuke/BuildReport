<template>
    <div class="dataflow-page">
        <div class="dataflow-body">
            <el-tabs v-model="activeName" type="border-card" style="height: 100%;">
                <el-tab-pane label="基本" name="base">
                    <div class="connect-form">
                        <el-form ref="connectForm" :model="connectForm" :rules="connectRules" label-position="left" label-width="120px" size="mini">
                            <el-form-item label="数据源名称" prop="name">
                                <el-input v-model="connectForm.name" placeholder="请输入数据源名称" />
                            </el-form-item>
                            <el-form-item label="服务器" prop="host">
                                <el-input v-model="connectForm.host" placeholder="请输入服务器地址" />
                            </el-form-item>
                            <el-form-item label="端口" prop="port">
                                <el-input v-model="connectForm.port" placeholder="请输入端口" />
                            </el-form-item>
                            <el-form-item label="SID" prop="sid">
                                <el-input v-model="connectForm.sid" placeholder="请输入SID" />
                            </el-form-item>
                            <el-form-item label="模式" prop="database">
                                <el-input v-model="connectForm.database" placeholder="请输入模式" />
                            </el-form-item>
                            <el-form-item label="用户" prop="userName">
                                <el-input v-model="connectForm.userName" placeholder="请输入用户" />
                            </el-form-item>
                            <el-form-item label="密码" prop="password">
                                <el-input v-model="connectForm.password" type="password" placeholder="请输入密码" show-password />
                            </el-form-item>
                        </el-form>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="高级" name="high">
                    <div class="connect-form">
                        <el-form ref="highForm" :model="highForm" label-position="left" label-width="120px" size="mini">
                            <el-form-item label="逻辑删除字段" prop="logicDeleteField">
                                <el-input v-model="highForm.logicDeleteField" placeholder="请输入数据源名称" />
                            </el-form-item>
                            <el-form-item label="逻辑删除值" prop="logicDeleteValue">
                                <el-input v-model="highForm.logicDeleteValue" placeholder="请输入服务器地址" />
                            </el-form-item>
                        </el-form>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>
        <div class="dataflow-footer">
            <div />
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </div>
    </div>
</template>

<script>

import { pick } from 'lodash'
export default {
    name: 'ReportSourceOracle',
    props: {
        config: { type: Object, default: () => { return {} } }
    },
    data() {
        return {
            loading: false,
            activeName: 'base',
            connectForm: {
                id: '',
                name: '',
                userName: '',
                host: '',
                sid: '',
                user: '',
                password: '',
                port: '1521',
                driver: 'oracle.jdbc.driver.OracleDriver',
                database: '',
                type: 1
            },
            connectRules: {
                name: [
                    { required: true, message: '请输入数据源名称', trigger: 'blur' }
                ],
                host: [
                    { required: true, message: '请输入服务器地址', trigger: 'blur' }
                ],
                userName: [
                    { required: true, message: '请输入用户', trigger: 'blur' }
                ],
                password: [
                    { required: true, message: '请输入密码', trigger: 'blur' }
                ],
                port: [
                    { required: true, message: '请输入端口', trigger: 'blur' }
                ],
                sid: [
                    { required: true, message: '请输入SID', trigger: 'blur' }
                ],
                database: [
                    { required: true, message: '请输入模式', trigger: 'blur' }
                ]
            },
            highForm: {
                logicDeleteField: '',
                logicDeleteValue: ''
            }
        }
    },
    mounted() {
        this.init()
    },
    methods: {
        init() {
            if (this.config.type === 1) {
                Object.assign(this.connectForm, pick(this.config, ['id', 'name']))
                Object.assign(this.connectForm, pick(this.config.options, ['host', 'userName', 'password', 'port', 'sid', 'database']))
                Object.assign(this.highForm, pick(this.config.options, ['logicDeleteField', 'logicDeleteValue']))
            }
        },
        submit() {
            this.$refs.connectForm.validate().then(async valid => {
                if (valid) {
                    const result = {
                        options: {}
                    }
                    Object.assign(result, pick(this.connectForm, ['id', 'name', 'type']))
                    Object.assign(result.options, pick(this.connectForm, ['host', 'userName', 'password', 'port', 'sid', 'driver', 'database']))
                    Object.assign(result.options, pick(this.highForm, ['logicDeleteField', 'logicDeleteValue']))
                    this.$emit('save', result)
                }
            }).catch(error => {
                this.activeName = 'base'
                console.log(error)
            })
        }
    }
}
</script>
  <style lang="scss" scoped>
  .dataflow-page {
  width: 100%;
  height: calc(100% - 40px);
  .dataflow-body {
    width: 100%;
    height: calc(100% - 60px);
    overflow-y: auto;
    overflow-x: hidden;
    padding: 10px;
    padding-bottom: 0;

    .dataflow-title {
      font-size: 14px;
      color: #999999;
    }
  }
  .dataflow-footer {
    width: 100%;
    height: 60px;
    padding: 0 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  * {
    user-select: none;
  }
}
  .dataflow-page {
    .dataflow-body {
      padding: 0 10px;
    }
  }
  .step-container {
    width: 100%;
    .el-step {
      ::v-deep .el-step__title {
        font-size: 14px;
      }
    }
  }
  .connect-form {
    padding-top: 20px;
    .block-form-item {
      margin-bottom: 0px;
    }
  }

  .el-checkbox {
    display: block;
  }

  .m-b-20 {
    margin-bottom: 18px;
  }
  </style>

