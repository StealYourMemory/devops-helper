<template>
    <svg t="1715002459212" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
        p-id="8150" :width="width" :height="height">
        <path d="M192 192m96 0l448 0q96 0 96 96l0 0q0 96-96 96l-448 0q-96 0-96-96l0 0q0-96 96-96Z" fill="#262626"
            opacity=".9" p-id="8151"></path>
        <path d="M192 448m96 0l448 0q96 0 96 96l0 0q0 96-96 96l-448 0q-96 0-96-96l0 0q0-96 96-96Z" fill="#262626"
            opacity=".9" p-id="8152"></path>
        <path d="M192 704m96 0l448 0q96 0 96 96l0 0q0 96-96 96l-448 0q-96 0-96-96l0 0q0-96 96-96Z" fill="#262626"
            opacity=".9" p-id="8153"></path>
        <path
            d="M608 320a32 32 0 1 0 0-64 32 32 0 0 0 0 64zM608 576a32 32 0 1 0 0-64 32 32 0 0 0 0 64zM608 832a32 32 0 1 0 0-64 32 32 0 0 0 0 64z"
            fill="#FFFFFF" opacity=".9" p-id="8154"></path>
    </svg>
</template>

<script setup lang="ts">
import type SvgConfig from '@/@types/svgConfig';
withDefaults(defineProps<SvgConfig>(), {
    width: 24,
    height: 24
})
</script>