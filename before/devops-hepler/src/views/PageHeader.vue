<template>
<el-page-header @back="onBack">
  <template #breadcrumb>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.name" :to="item.path">
        {{item.name}}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </template>
  <template #content>
    <el-tooltip content="文档" placement="bottom" effect="light">
      <el-link
          href="http://10.0.45.171/display/~yinhuasheng@unicloud.com/Devops+Helper"
          target="_blank"
          :underline="false">
        <el-icon size="large">
          <question-filled></question-filled>
        </el-icon>
      </el-link>
    </el-tooltip>
  </template>
</el-page-header>
</template>

<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {ref, watchEffect} from "vue";
import {QuestionFilled} from "@element-plus/icons-vue";
const router = useRouter();
function onBack(){
  router.back();
}

interface BreadcrumbItem{
  path: string,
  name: any
}
const route = useRoute();
const breadcrumbs = ref<BreadcrumbItem[]>([]);
watchEffect(()=>{
  const matched = route.matched.filter(m => m.meta && m.meta.breadcrumb)
  breadcrumbs.value = matched.map(m => ({
    path: m.path,
    name: m.meta.breadcrumb
  }))
})
</script>

<style>
.el-page-header__content{
  font-size: 0;
}
</style>