<template>
    <AbsoluteContainer>
        <report-source v-bind="urls" />
    </AbsoluteContainer>
</template>
  
<script>
import ReportSource from './components/index.vue'
import AbsoluteContainer from '@/components/AbsoluteContainer/index.vue'
export default {
    name: 'ReportSourceIns',
    components: {
        AbsoluteContainer,
        ReportSource
    },
    data() {
        return {
            urls: {
                listUrl: '/datasource/page',
                detailUrl: '/datasource/datail',
                addUrl: '/datasource/create',
                editUrl: '/datasource/update',
                deleteUrl: '/datasource/delete',
                testUrl: '/datasource/test'
            }
        }
    },
    mounted() {
    },
    methods: {
      
    }
}
</script>
  
<style scoped lang="scss">
    .absolute-container {
        height: calc(100% - 0px) !important;
    }
</style>
  
