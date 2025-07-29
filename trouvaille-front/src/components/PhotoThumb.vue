<template>
  <div>
    <img :src="thumb" :alt="alt2" loading="lazy" />
  </div>
</template>
<script setup lang="ts">
import { Photo } from '@/api'
import { computed } from 'vue'

const thumb = computed(() => {
  return '/api/v1/photos/' + props.photo.id + '/1024/1024'
})

const alt2 = computed(() => {
  return props.alt || ''
})

const props = defineProps<{
  photo: Photo
  alt?: string
}>()
</script>
<style scoped>
.photo-container {
  width: 100%;
  height: 100%;
}
</style>
