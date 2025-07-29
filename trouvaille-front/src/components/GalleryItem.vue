<template>
  <a
    class="vpis-item"
    :href="full"
    :data-pswp-src="full"
    :data-pswp-width="photo.width"
    :data-pswp-height="photo.height"
    :data-cropped="true"
    target="_blank"
  >
    <PhotoThumb :photo="photo" />
  </a>
</template>
<script setup lang="ts">
import { Photo } from '@/api'
import { computed } from 'vue'
import PhotoThumb from './PhotoThumb.vue'

const full = computed(() => {
  return '/api/v1/photos/' + props.photo.id + '/4096/4096'
})

const props = defineProps<{
  photo: Photo
}>()
</script>
