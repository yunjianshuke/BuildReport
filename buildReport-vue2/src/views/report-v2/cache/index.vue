<template>
    <AbsoluteContainer>
        <report-cache v-bind="urls" />
    </AbsoluteContainer>
</template>
  
<script>
import ReportCache from './components/index.vue'
import AbsoluteContainer from '@/components/AbsoluteContainer/index.vue'
export default {
    name: 'ReportCacheIns',
    components: {
        AbsoluteContainer,
        ReportCache
    },
    data() {
        return {
            urls: {
                listUrl: '/reportDict/getPage',
                detailUrl: '/reportDict/datail',
                addUrl: '/reportDict/save',
                deleteUrl: '/reportDict/remove'
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
  
