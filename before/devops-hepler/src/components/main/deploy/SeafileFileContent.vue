<template>
  <el-scrollbar max-height="600">
    <div>
      <highlightjs :language="fileType" :code='content'></highlightjs>
    </div>
  </el-scrollbar>
</template>
<script setup lang="ts">
import 'highlight.js/styles/atom-one-dark.css';

interface Props {
  content: string;
  fileType: string
}

withDefaults(defineProps<Props>(), {
  content: '',
  fileType: ''
})

</script>