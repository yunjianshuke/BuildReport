<template>
    <div class="sub-title">
        <div v-if="showBack" class="arrow-left" @click.stop="$emit('back')"><i class="el-icon-arrow-left" /></div>
        <div class="title">{{ title }}</div>
    </div>
</template>
  
<script>
export default {
    name: 'SubTitle',
    props: {
        title: { type: String, default: '' },
        showBack: { type: Boolean, default: true }
    }
}
</script>
  <style lang="scss" scoped>
.sub-title {
    width: 100%;
    height: 40px;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 10px;
    position: relative;
    .title {
      width: 100%;
      font-size: 14px;
      font-weight: 500;
      color: #333333;
      line-height: 20px;
      text-align: center;
    }
    .arrow-left {
      position: absolute;
      top: 0;
      left: 0;
      width: 40px;
      height: 40px;
      line-height: 40px;
      text-align: center;
      cursor:  pointer;
      i {
        font-size: 16px;
        color: #333333;
      }
    }
}
</style>
  
