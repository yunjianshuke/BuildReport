<template>
    <div class="data-in">
        <el-collapse v-if="!current" v-model="activeNames" @change="handleChange">
            <el-collapse-item title="数据源" name="1">
                <div class="source-container">
                    <div :class="current === 'Mysql' ? 'current': ''" class="source-item" @click="handleSource('Mysql')"><img class="source-icon icon-mysql" :src="mysqlImage"> Mysql</div>
                    <div :class="current === 'Oracle' ? 'current': ''" class="source-item" @click="handleSource('Oracle')"><img class="source-icon icon-oracle" :src="oracleImage"> Oracle</div>
                    <div :class="current === 'Postgresql' ? 'current': ''" class="source-item" @click="handleSource('Postgresql')"><img class="source-icon icon-postgresql" :src="postgresqlImage"> Postgresql</div>
                </div>
            </el-collapse-item>
        </el-collapse>
        <div v-if="current" class="data-in-body">
            <sub-title :show-back="!config.id" :title="title" @back="onBack" />
            <component :is="current" :config="config" @save="onSave" />
        </div>
    </div>
</template>
<script>
import SubTitle from './SubTitle'
import Mysql from './Mysql'
import Oracle from './Oracle'
import Postgresql from './Postgresql'
import MysqlImage from './../icons/mysql.png'
import OracleImage from './../icons/oracle.png'
import PostgresqlImage from './../icons/postgresql.png'
export default {
    name: 'ReportUpsert',
    components: {
        Mysql,
        Oracle,
        Postgresql,
        SubTitle
    },
    props: {
        config: { type: Object, default: () => { return {} } }
    },
    data() {
        return {
            activeNames: ['1'],
            current: '',
            mysqlImage: MysqlImage,
            oracleImage: OracleImage,
            postgresqlImage: PostgresqlImage
        }
    },
    computed: {
        title() {
            return this.current.toUpperCase()
        }
    },
    mounted() {
        this.init()
    },
    methods: {
        init() {
            if (this.config.sourceType) {
                this.current = this.config.sourceType
            }
        },
        handleSource(component) {
            this.current = component
        },
        handleChange() {
        
        },
        onBack() {
            this.current = ''
        },
        onSave(form) {
            this.$emit('save', form)
        }
    }
}
</script>
<style scoped lang="scss">
  .data-in, .data-in-body {
    height: 100%;
    width: 100%;
  }
  .data-in {
    .el-collapse-item {
       ::v-deep .el-collapse-item__header {
         padding: 10px;
       }
       ::v-deep .el-collapse-item__content {
        padding: 10px;
       }
    }

    .source-container {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: flex-start;
      justify-content: flex-start;
      gap: 10px;
    }

    .source-item {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      border: 1px solid #cccccc;
      border-radius: 4px;
      padding: 4px 10px;
      cursor: pointer;

      &:hover {
        border-color: #66b1ff;
      }
    }

    .source-icon {
      width: auto;
      height: 20px;
    }
  }
</style>
