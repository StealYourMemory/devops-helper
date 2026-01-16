<template>
  <svg t="1715346431342" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
       p-id="4435" :width="width" :height="height">
    <path
        d="M512 64C264.576 64 64 264.576 64 512c0 247.424 200.576 448 448 448s448-200.576 448-448C960 264.576 759.424 64 512 64zM512 848c-185.536 0-336-150.464-336-336S326.464 176 512 176 848 326.464 848 512 697.536 848 512 848zM512 400c-30.912 0-56 25.088-56 56l0 224C456 710.912 481.088 736 512 736s56-25.088 56-56l0-224C568 425.088 542.912 400 512 400zM512 232c-30.912 0-56 25.088-56 56S481.088 344 512 344 568 318.912 568 288 542.912 232 512 232z"
        p-id="4436"></path>
  </svg>
</template>
<script setup lang="ts">
import type SvgConfig from '@/@types/svgConfig';

withDefaults(defineProps<SvgConfig>(), {
  width: 24,
  height: 24
})
</script>