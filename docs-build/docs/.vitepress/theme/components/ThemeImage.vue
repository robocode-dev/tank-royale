<script setup>
import { useData } from 'vitepress'

const { isDark } = useData()

const props = defineProps({
  darkSrc: { type: String, required: true },
  lightSrc: { type: String, default: null },
  alt: { type: String, default: '' },
})
</script>

<template>
  <img :src="isDark || !lightSrc ? darkSrc : lightSrc" :alt="alt" />
</template>
