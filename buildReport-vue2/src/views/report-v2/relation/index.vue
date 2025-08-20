<template>
    <AbsoluteContainer>
        <source-relation v-bind="urls" />
    </AbsoluteContainer>
</template>

<script>
import SourceRelation from './components/index.vue'
import AbsoluteContainer from '@/components/AbsoluteContainer/index.vue'
export default {
    name: 'ReportRelationIns',
    components: {
        AbsoluteContainer,
        SourceRelation
    },
    data() {
        return {
            urls: {
                listUrl: '/relation/page',
                addUrl: '/relation/save',
                deleteUrl: '/relation/delete'
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
    padding-right: 20px;
}
</style>

