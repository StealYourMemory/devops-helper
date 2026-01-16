<template>
  <el-row justify="center">
    <el-col :span="10">
      <h3>已有功能</h3>
    </el-col>
    <el-col :span="10">
      待做功能
    </el-col>
  </el-row>
  <el-row justify="center">
    <el-col :span="10">
      <el-timeline>
        <el-timeline-item v-for="item in features" :key="item.content" :timestamp="item.timestamp">
          {{ item.content }}
        </el-timeline-item>
      </el-timeline>
    </el-col>
    <el-col :span="10">
<!--      需要放个表格，表格展示的是要做的内容和提的时间以及点赞和点踩数量-->
    </el-col>
  </el-row>
</template>
<script setup lang="ts">
import {onMounted} from "vue";

interface FeatureItem {
  content: string,
  timestamp: string
}

const features: FeatureItem[] = [
  {
    content: '服务代理',
    timestamp: '2024-07-24'
  },
  {
    content: '环境管理',
    timestamp: '2024-07-11'
  },
  {
    content: '转测归档',
    timestamp: '2024-06-12'
  },
  {
    content: '开发转测',
    timestamp: '2024-05-20'
  },
  {
    content: '需求审查',
    timestamp: '2024-05-16'
  },
  {
    content: 'Bug审查',
    timestamp: '2024-05-16'
  },
  {
    content: '审计日志',
    timestamp: '2024-05-15'
  },

  {
    content: '仅打包',
    timestamp: '2024-05-13'
  }
  ,
  {
    content: '仅部署',
    timestamp: '2024-05-13'
  },
  {
    content: '打包和部署',
    timestamp: '2024-05-08'
  }
]
onMounted(() => {

})


</script>