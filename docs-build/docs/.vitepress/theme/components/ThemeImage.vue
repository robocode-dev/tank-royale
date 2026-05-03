<script setup>
const props = defineProps({
  darkSrc: { type: String, required: true },
  lightSrc: { type: String, default: null },
  alt: { type: String, default: '' },
})
</script>

<template>
  <img v-if="lightSrc" class="light-only" :src="lightSrc" :alt="alt" />
  <img class="dark-only" :src="darkSrc" :alt="alt" />
</template>
