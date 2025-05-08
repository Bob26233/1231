<template>
  <div>
    <el-affix >
      <Header></Header>
    </el-affix>
    <RouterView>

    </RouterView>
    <Footer></Footer>
  </div>
</template>

<script lang="ts" setup>
import { RouterView } from 'vue-router';
import Footer from './views/Footer.vue';

import Header from '@/views/Header.vue';
</script>

<style scoped lang="scss">
</style>    