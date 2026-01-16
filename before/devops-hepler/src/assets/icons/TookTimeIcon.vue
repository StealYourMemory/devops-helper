<template>
  <svg t="1715346528025" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
       p-id="8006" :width="width" :height="height">
    <path
        d="M523.5 831a351.39 351.39 0 1 1 137.22-27.71A350.3 350.3 0 0 1 523.5 831z m0-651C358.91 180 225 313.91 225 478.5S358.91 777 523.5 777 822 643.09 822 478.5 688.09 180 523.5 180z"
        p-id="8007"></path>
    <path d="M512 539a27 27 0 0 1-27-27V303a27 27 0 0 1 54 0v209a27 27 0 0 1-27 27z" p-id="8008"></path>
    <path d="M722 539H524a27 27 0 0 1 0-54h198a27 27 0 0 1 0 54z" p-id="8009"></path>
  </svg>
</template>
<script setup lang="ts">
import type SvgConfig from '@/@types/svgConfig';

withDefaults(defineProps<SvgConfig>(), {
  width: 24,
  height: 24
})
</script>