<template>
    <div v-loading="loading" class="report-datasets">
        <div class="pb-d">
            <el-button type="primary" icon="el-icon-plus" size="mini" @click="addDataset()">新增</el-button>
        </div>
        <div class="vxe-gird-box">
            <el-table ref="table" size="mini" width="100%" height="100%" :data="datasetList" border stripe fit>
                <el-table-column 
                    prop="name"
                    label="名称"
                />
                <el-table-column 
                    label="操作"
                >
                    <template slot-scope="scope">
                        <el-button type="text" icon="el-icon-edit" size="mini" @click="editDataset(scope.$index)">编辑</el-button>
                        <!-- <el-button v-if="businessType === '0'" type="text" icon="el-icon-user-solid" size="mini" @click="handleAuth(scope.row)"> 授权 </el-button> -->
                        <el-button type="text" icon="el-icon-delete" size="mini" @click="deleteDataset(scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <div class="page-wrap">
            <el-pagination :current-page="page.current" :page-sizes="[10, 20, 30, 50]" :page-size="page.size" layout="total, sizes, prev, pager, next, jumper" :total="page.total" @size-change="handleSizeChange" @current-change="handleCurrentChange" />
        </div>
        <el-dialog v-loading="datasetLoading" :visible.sync="datasetVisible" class="relation-dialog" :fullscreen="isDatasetFullscreen" width="50%" :close-on-click-modal="false" :before-close="datasetBeforeClose" append-to-body>
            <div slot="title" class="report-dialog-header">
                <div class="report-dialog-title">{{ title }}</div>
                <div class="report-dialog-btns">
                    <i class="el-icon-full-screen" @click.stop="changDatasetFullScreen" />
                </div>
            </div>
            <dataset v-if="datasetVisible && businessType === '0'" ref="dataset" key="designerDataset" business-type="0" :relation-id="relationId" :dataset-id="datasetId" :datasets="datasets" @change="onDatasetChange" />
            <dataset v-if="datasetVisible && businessType === '1'" ref="formDataset" key="formDataset" business-type="1" :relation-id="relationId" :dataset-id="datasetId" :datasets="datasets" @change="onDatasetChange" />
        </el-dialog>
    </div>
</template>
<script>
import Dataset from './dataset/index.vue'
export default {
    name: 'ReportDatasetList',
    components: {
        Dataset
    },
    props: {
        relationId: {
            type: String,
            default: ''
        },
        deptUrl: {
            type: String,
            default: '/admin/dept/tree'
        },
        authAddUrl: {
            type: String,
            default: '/ve'
        },
        authListUrl: {
            type: String,
            default: '/auth/list'
        },
        datasetSaveUrl: {
            type: String,
            default: '/reportInfo/saveDataSet' 
        },
        businessType: {
            type: String,
            default: '0'
        }
    },
    data() {
        return {
            loading: false,
            datasetList: [],
            datasetLoading: false,
            datasetVisible: false,
            isDatasetFullscreen: true,
            title: '',
            page: {
                total: 0, // 总页数
                current: 1, // 当前页数
                size: 20 // 每页显示多少条
            },
            tab: '',
            datasetId: '',
            datasets: []
        }
    },
    mounted() {
        this.getList()
    },
    methods: {
        datasetBeforeClose(done) {
            done()
        },
        changDatasetFullScreen() {
            this.isDatasetFullscreen = !this.isDatasetFullscreen   
        },
        editDataset(index) {
            this.datasetId = this.datasetList[index].id
            const dataset = this.datasetList[index]
            const fields = []
            dataset.fieldJsonArray && JSON.parse(dataset.fieldJsonArray).forEach(table => {
                table.sqlFieldList.forEach(field => {
                    fields.push(Object.assign({
                        datasetName: dataset.name,
                        aliasName: table.aliasName,
                        tableName: table.tableName,
                        cellId: table.cellId,
                        dataSourceId: dataset.dataSourceId,
                        key: `${table.aliasName}_${field.name}`,
                        checked: true
                    }, field))
                })
            })
            this.datasets = [{
                datasetId: dataset.id,
                datasetName: dataset.name,
                relationId: dataset.relationId,
                dataSourceId: dataset.dataSourceId,
                type: dataset.type + '',
                businessType: (dataset.businessType || '0') + '',
                fields,
                dataFilter: dataset.dataFilter ? JSON.parse(dataset.dataFilter) : []
            }]
            this.title = dataset.name
            this.datasetVisible = true
        },
        handleSizeChange(val) {
            this.page.size = val
            this.getList()
        },
        handleCurrentChange(val) {
            this.page.current = val
            this.getList()
        },
        getList() {
            this.loading = true
            const vm = this
            vm.$ajax({
                url: '/reportInfo/getDataSetPage',
                method: 'post',
                data: {
                    relationId: this.relationId,
                    businessType: this.businessType,
                    current: this.page.current,
                    size: this.page.size
                }
            }).then(res => {
                if (res.code === 0 && res.data && res.data.total) {
                    vm.datasetList = res.data.records
                    vm.page.total = res.data.total
                    return
                }
                vm.datasetList = []
                vm.page.total = 0
            }).finally(() => {
                this.loading = false
            })
        },  
        
        onDatasetChange(datasets) {
            const [dataset] = datasets
            if (dataset) {
                this.$ajax({
                    url: this.datasetSaveUrl,
                    method: 'post',
                    data: dataset
                }).then(res => {
                    if (res.code === 0) {
                        this.$message.success('保存成功')
                        this.datasetVisible = false
                        this.getList()
                    }
                })
            }
        },
        addDataset() {
            this.datasetId = ''
            this.title = '新增'
            this.datasets = []
            this.datasetVisible = true
        },
        deleteDataset(row) {
            const vm = this
            vm.$confirm(`确认删除数据集： ${row.name} 吗？`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
                closeOnClickModal: false
            }).then(() => {
                vm.loading = true
                vm.$ajax({
                    url: `/reportInfo/deleteDataSet/${row.id}`,
                    method: 'post'
                }).then(res => {
                    if (res.code === 0) {
                        vm.$message.success('删除成功')
                        vm.getList()
                    }
                }).finally(() => {
                    this.loading = false
                })
            })
        }
    }
}
</script>
<style lang="scss" scoped>
.report-datasets {
    width: 100%;
    height: 100%;
    padding: 10px;
    .pb-d {
        margin-bottom: 10px;
    }
    display: flex;
    flex-direction: column;
    .vxe-gird-box {
        flex: 1;
        height: 0;
    }
}
.page-wrap {
    margin-top: 20px;
    text-align: right;
}
p,
ul {
    margin-block-start: 0;
    margin-block-end: 0;
}
.relation-dialog {
  ::v-deep .el-dialog {
    height: 90%;
    margin-top: 4.5vh !important;

    &.is-fullscreen {
        height: 100%;
        margin: 0 !important;
    }

    .el-dialog__header {
      padding-top: 10px;
      line-height: 1;
    }

    .el-dialog__headerbtn {
      top: 10px;
    }

    .report-dialog-header {
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-right: 20px;

        i {
            color: #909399;
            font-size: 16px;
            cursor: pointer;
        }
    }

    .el-dialog__body {
      height: calc(100% - 39px);
      padding: 0;
      position: relative;
    }
  }
}
.configWindow {
  ::v-deep .el-dialog {
    width: 600px;
    margin-bottom: 0;

    .el-dialog__body {
      padding: 15px;
    }

    .form-container {
      display: flex;
      flex-wrap: wrap;
      width: 100%;

      ul {
        list-style-type: none;
        padding-left: 0;
        display: flex;
        width: 45%;
        margin: 10px 0;

        &.U1 {
          width: 90%;
        }

        .label {
          width: 100px;
          line-height: 32px;
          text-align: center
        }

        .content {
          flex: 1;

          p {
            line-height: 32px;
          }

          .array-dynamic-wrap {
            max-height: 225px;
            overflow-x: hidden;
            overflow-y: auto;
            padding-right: 10px;

            .array-dynamic-item {
              display: flex;
              align-items: center;
              margin-bottom: 6px;
              padding-bottom: 6px;
              border-bottom: 1px dashed #eee;

              .el-input {
                margin-right: 10px;
                vertical-align: top;
                width: 200px;
              }

              .el-textarea {
                margin-right: 10px;
                vertical-align: top;
                width: 400px;
              }
            }
          }

          .ORGSelect {
            width: 100%;

            ul {
              width: 100%;
            }

            .ORGOption {
              .el-scrollbar {
                .el-select-dropdown__wrap {
                  max-height: inherit;

                  .el-select-dropdown__list {
                    flex-direction: column;
                    padding: 10px 20px 20px 10px;

                    .ORGFilterInput {
                      margin-bottom: 5px;
                    }

                    .el-tree {
                      padding-right: 5px;
                      max-height: 208px;
                      overflow-x: hidden;
                      overflow-y: auto;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    .form-operation {
      display: flex;
      justify-content: center;
      border-top: 1px solid #dcdfe6;
      padding-top: 15px;

      .el-button {
        margin-right: 20px;
      }
    }
  }
}
</style>