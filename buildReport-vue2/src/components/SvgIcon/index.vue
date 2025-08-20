<template>
    <i v-if="name.indexOf('el-icon-') === 0 || name.indexOf('ri-') === 0" :class="name" />
    <i v-else-if=" name.indexOf('buildicon-') === 0" :class="name" class="iconfontbuild" />
    <svg v-else class="svg-icon" aria-hidden="true" v-on="$listeners">
        <use :xlink:href="`#icon-${name}`" />
    </svg>
</template>

<script>
export default {
    name: 'SvgIcon',
    props: {
        name: {
            type: String,
            required: true
        }
    }
}
</script>

<style scoped>
.svg-icon {
    width: 1em;
    height: 1em;
    vertical-align: -0.15em;
    fill: currentColor;
    overflow: hidden;
}
</style>
