<template>
  <div
    v-if="show"
    class="fixed inset-0 z-50 bg-black bg-opacity-90 flex items-center justify-center"
    @click="$emit('close')"
  >
    <!-- Photo Container -->
    <div
      ref="containerRef"
      class="relative w-full h-full overflow-hidden"
      @click.stop
      @wheel="handleWheel"
      @mousedown="handleMouseDown"
      @mousemove="handleMouseMove"
      @mouseup="handleMouseUp"
      @mouseleave="handleMouseUp"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove"
      @touchend="handleTouchEnd"
    >
      <!-- Photo -->
      <img
        v-if="photoUrl"
        ref="imageRef"
        :src="photoUrl"
        :alt="alt"
        class="absolute select-none pointer-events-none top-1/2 left-1/2"
        :style="imageStyle"
        @load="onImageLoad"
        @error="$emit('error')"
      />

      <!-- Loading -->
      <div v-else-if="loading" class="absolute inset-0 flex items-center justify-center text-white">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-white"></div>
        <span class="ml-3">Chargement...</span>
      </div>

      <!-- Error -->
      <div v-else class="absolute inset-0 flex items-center justify-center text-white">
        <div class="text-center">
          <PhotoIcon class="h-16 w-16 mx-auto mb-2 text-gray-400" />
          <p>Erreur de chargement</p>
        </div>
      </div>
    </div>

    <!-- Close Button -->
    <button
      @click="$emit('close')"
      class="absolute top-2 right-2 z-10 p-2 rounded-full bg-black bg-opacity-50 text-white hover:bg-opacity-70 transition-all"
    >
      <XMarkIcon class="h-6 w-6" />
    </button>

    <!-- Navigation Buttons -->
    <button
      v-if="showPrevious"
      @click.stop="$emit('previous')"
      class="absolute left-0 top-0 h-full w-1/3 flex items-center justify-start pl-2 text-white hover:bg-black hover:bg-opacity-20 transition-all group"
    >
      <div class="p-3 rounded-full bg-black bg-opacity-50 group-hover:bg-opacity-70 transition-all">
        <ChevronLeftIcon class="h-8 w-8" />
      </div>
    </button>

    <button
      v-if="showNext"
      @click.stop="$emit('next')"
      class="absolute right-0 top-0 h-full w-1/3 flex items-center justify-end pr-2 text-white hover:bg-black hover:bg-opacity-20 transition-all group"
    >
      <div class="p-3 rounded-full bg-black bg-opacity-50 group-hover:bg-opacity-70 transition-all">
        <ChevronRightIcon class="h-8 w-8" />
      </div>
    </button>

    <!-- Zoom Controls -->
    <div class="absolute bottom-2 left-1/2 transform -translate-x-1/2 flex space-x-2">
      <button
        @click="zoomOut"
        :disabled="scale <= optimalScale"
        class="p-2 rounded-full bg-black bg-opacity-50 text-white hover:bg-opacity-70 transition-all disabled:opacity-50"
      >
        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
        </svg>
      </button>

      <button
        @click="resetZoom"
        class="px-3 py-2 rounded-full bg-black bg-opacity-50 text-white hover:bg-opacity-70 transition-all text-sm"
      >
        {{ Math.round(scale * 100) }}%
      </button>

      <button
        @click="zoomIn"
        :disabled="scale >= maxScale"
        class="p-2 rounded-full bg-black bg-opacity-50 text-white hover:bg-opacity-70 transition-all disabled:opacity-50"
      >
        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M12 4v16m8-8H4"
          />
        </svg>
      </button>
    </div>

    <!-- Photo Info -->
    <div
      v-if="currentIndex !== undefined && totalCount"
      class="absolute bottom-2 right-2 px-3 py-2 rounded-full bg-black bg-opacity-50 text-white text-sm"
    >
      {{ currentIndex + 1 }} / {{ totalCount }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { PhotoIcon, XMarkIcon, ChevronLeftIcon, ChevronRightIcon } from '@heroicons/vue/24/outline'

interface Props {
  show: boolean
  photoUrl?: string
  alt?: string
  loading?: boolean
  showPrevious?: boolean
  showNext?: boolean
  currentIndex?: number
  totalCount?: number
}

const props = withDefaults(defineProps<Props>(), {
  alt: 'Photo',
  loading: false,
  showPrevious: false,
  showNext: false,
})

const emit = defineEmits<{
  close: []
  previous: []
  next: []
  error: []
}>()

// Refs
const containerRef = ref<HTMLElement>()
const imageRef = ref<HTMLImageElement>()

// State
const scale = ref(1)
const translateX = ref(0)
const translateY = ref(0)
const isDragging = ref(false)
const lastMouseX = ref(0)
const lastMouseY = ref(0)
const imageLoaded = ref(false)
const naturalWidth = ref(0)
const naturalHeight = ref(0)
const optimalScale = ref(1)

// Constants
const maxScale = 5
const zoomStep = 0.3

// Touch handling
const lastTouchDistance = ref(0)
const lastTouchX = ref(0)
const lastTouchY = ref(0)

// Computed
const imageStyle = computed(() => ({
  transform: `translate(calc(-50% + ${translateX.value}px), calc(-50% + ${translateY.value}px)) scale(${scale.value})`,
  transformOrigin: 'center center',
}))

// Watch for photo changes to reset view
watch(
  () => props.photoUrl,
  () => {
    resetView()
  },
)

watch(
  () => props.currentIndex,
  () => {
    resetView()
  },
)

watch(
  () => props.show,
  (newShow) => {
    if (newShow) {
      resetView()
      nextTick(() => {
        // Add keyboard listener
        document.addEventListener('keydown', handleKeyDown)
      })
    } else {
      document.removeEventListener('keydown', handleKeyDown)
    }
  },
)

// Methods
const resetView = () => {
  translateX.value = 0
  translateY.value = 0
  imageLoaded.value = false
  // Don't reset scale here, let fitImageToContainer handle it on image load
}

const resetZoom = () => {
  fitImageToContainer()
}

const zoomIn = () => {
  const newScale = Math.min(scale.value + zoomStep, maxScale)
  setScale(newScale)
}

const zoomOut = () => {
  const newScale = Math.max(scale.value - zoomStep, optimalScale.value)
  setScale(newScale)
}

const setScale = (newScale: number) => {
  scale.value = newScale
  constrainPosition()
}

const centerImage = () => {
  translateX.value = 0
  translateY.value = 0
}

const fitImageToContainer = () => {
  if (!containerRef.value || !imageLoaded.value) return

  const containerRect = containerRef.value.getBoundingClientRect()
  const containerWidth = containerRect.width
  const containerHeight = containerRect.height

  // Calculate scale to fit image in container (borderless)
  const scaleX = containerWidth / naturalWidth.value
  const scaleY = containerHeight / naturalHeight.value

  // Use the smaller scale to ensure the entire image is visible
  const calculatedOptimalScale = Math.min(scaleX, scaleY) // Allow scaling up to fill container

  // Save optimal scale as minimum scale
  optimalScale.value = calculatedOptimalScale
  scale.value = calculatedOptimalScale
  centerImage()
}

const constrainPosition = () => {
  if (!containerRef.value || !imageLoaded.value) return

  const containerRect = containerRef.value.getBoundingClientRect()
  const scaledWidth = naturalWidth.value * scale.value
  const scaledHeight = naturalHeight.value * scale.value

  // Calculate max translation values
  const maxX = Math.max(0, (scaledWidth - containerRect.width) / 2)
  const maxY = Math.max(0, (scaledHeight - containerRect.height) / 2)

  // Constrain translation
  translateX.value = Math.max(-maxX, Math.min(maxX, translateX.value))
  translateY.value = Math.max(-maxY, Math.min(maxY, translateY.value))
}

const onImageLoad = () => {
  if (imageRef.value && containerRef.value) {
    naturalWidth.value = imageRef.value.naturalWidth
    naturalHeight.value = imageRef.value.naturalHeight
    imageLoaded.value = true
    fitImageToContainer()
  }
}

// Mouse events
const handleWheel = (event: WheelEvent) => {
  event.preventDefault()

  const delta = -event.deltaY / 1000
  const newScale = Math.max(optimalScale.value, Math.min(maxScale, scale.value + delta))

  if (newScale !== scale.value) {
    // Zoom towards mouse position
    const rect = containerRef.value?.getBoundingClientRect()
    if (rect) {
      const mouseX = event.clientX - rect.left
      const mouseY = event.clientY - rect.top
      const centerX = rect.width / 2
      const centerY = rect.height / 2

      // Calculate offset from center
      const offsetX = mouseX - centerX
      const offsetY = mouseY - centerY

      // Adjust translation to zoom towards mouse
      const scaleRatio = newScale / scale.value
      translateX.value = (translateX.value - offsetX) * scaleRatio + offsetX
      translateY.value = (translateY.value - offsetY) * scaleRatio + offsetY
    }

    scale.value = newScale
    constrainPosition()
  }
}

const handleMouseDown = (event: MouseEvent) => {
  if (scale.value > 1) {
    isDragging.value = true
    lastMouseX.value = event.clientX
    lastMouseY.value = event.clientY
    event.preventDefault()
  }
}

const handleMouseMove = (event: MouseEvent) => {
  if (isDragging.value && scale.value > 1) {
    const deltaX = event.clientX - lastMouseX.value
    const deltaY = event.clientY - lastMouseY.value

    translateX.value += deltaX
    translateY.value += deltaY

    lastMouseX.value = event.clientX
    lastMouseY.value = event.clientY

    constrainPosition()
  }
}

const handleMouseUp = () => {
  isDragging.value = false
}

// Touch events
const getTouchDistance = (touches: TouchList) => {
  if (touches.length < 2) return 0
  const touch1 = touches[0]
  const touch2 = touches[1]
  return Math.sqrt(
    Math.pow(touch2.clientX - touch1.clientX, 2) + Math.pow(touch2.clientY - touch1.clientY, 2),
  )
}

const getTouchCenter = (touches: TouchList) => {
  if (touches.length === 1) {
    return { x: touches[0].clientX, y: touches[0].clientY }
  } else if (touches.length === 2) {
    return {
      x: (touches[0].clientX + touches[1].clientX) / 2,
      y: (touches[0].clientY + touches[1].clientY) / 2,
    }
  }
  return { x: 0, y: 0 }
}

const handleTouchStart = (event: TouchEvent) => {
  event.preventDefault()

  if (event.touches.length === 1) {
    // Single touch - start dragging
    if (scale.value > 1) {
      isDragging.value = true
      lastTouchX.value = event.touches[0].clientX
      lastTouchY.value = event.touches[0].clientY
    }
  } else if (event.touches.length === 2) {
    // Two touches - start pinch zoom
    isDragging.value = false
    lastTouchDistance.value = getTouchDistance(event.touches)
    const center = getTouchCenter(event.touches)
    lastTouchX.value = center.x
    lastTouchY.value = center.y
  }
}

const handleTouchMove = (event: TouchEvent) => {
  event.preventDefault()

  if (event.touches.length === 1 && isDragging.value) {
    // Single touch - drag
    const deltaX = event.touches[0].clientX - lastTouchX.value
    const deltaY = event.touches[0].clientY - lastTouchY.value

    translateX.value += deltaX
    translateY.value += deltaY

    lastTouchX.value = event.touches[0].clientX
    lastTouchY.value = event.touches[0].clientY

    constrainPosition()
  } else if (event.touches.length === 2) {
    // Two touches - pinch zoom
    const currentDistance = getTouchDistance(event.touches)
    const center = getTouchCenter(event.touches)

    if (lastTouchDistance.value > 0) {
      const scaleChange = currentDistance / lastTouchDistance.value
      const newScale = Math.max(optimalScale.value, Math.min(maxScale, scale.value * scaleChange))

      if (newScale !== scale.value) {
        // Zoom towards touch center
        const rect = containerRef.value?.getBoundingClientRect()
        if (rect) {
          const touchX = center.x - rect.left
          const touchY = center.y - rect.top
          const centerX = rect.width / 2
          const centerY = rect.height / 2

          const offsetX = touchX - centerX
          const offsetY = touchY - centerY

          const scaleRatio = newScale / scale.value
          translateX.value = (translateX.value - offsetX) * scaleRatio + offsetX
          translateY.value = (translateY.value - offsetY) * scaleRatio + offsetY
        }

        scale.value = newScale
        constrainPosition()
      }
    }

    lastTouchDistance.value = currentDistance
    lastTouchX.value = center.x
    lastTouchY.value = center.y
  }
}

const handleTouchEnd = (event: TouchEvent) => {
  if (event.touches.length === 0) {
    isDragging.value = false
    lastTouchDistance.value = 0
  } else if (event.touches.length === 1) {
    // Continue with single touch
    lastTouchX.value = event.touches[0].clientX
    lastTouchY.value = event.touches[0].clientY
    lastTouchDistance.value = 0
  }
}

// Keyboard events
const handleKeyDown = (event: KeyboardEvent) => {
  switch (event.key) {
    case 'Escape':
      emit('close')
      break
    case 'ArrowLeft':
      if (props.showPrevious) emit('previous')
      break
    case 'ArrowRight':
      if (props.showNext) emit('next')
      break
    case '+':
    case '=':
      event.preventDefault()
      zoomIn()
      break
    case '-':
    case '_':
      event.preventDefault()
      zoomOut()
      break
    case '0':
      event.preventDefault()
      resetZoom()
      break
  }
}
</script>

<style scoped>
.select-none {
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

.pointer-events-none {
  pointer-events: none;
}
</style>
