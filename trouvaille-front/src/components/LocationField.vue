<template>
  <div class="relative">
    <label v-if="computedLabel" :for="id" class="block text-sm font-medium text-gray-700 mb-1">
      {{ computedLabel }}
    </label>

    <!-- Selected Location Display -->
    <div v-if="selectedLocation" class="mb-2 p-3 bg-green-50 border border-green-200 rounded-lg">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <MapPinIcon class="h-4 w-4 text-green-600" />
          <span class="text-sm text-green-800">{{ selectedLocation.label }}</span>
        </div>
        <button
          @click="clearLocation"
          class="text-green-600 hover:text-green-800"
          :title="$t('location.actions.remove')"
          type="button"
        >
          <XMarkIcon class="h-4 w-4" />
        </button>
      </div>
      <div class="text-xs text-green-600 mt-1">
        {{ selectedLocation.coordinates[1].toFixed(4) }},
        {{ selectedLocation.coordinates[0].toFixed(4) }}
      </div>
    </div>

    <!-- Location Search Input -->
    <div v-if="!selectedLocation" class="relative">
      <input
        :id="id"
        v-model="locationSearch"
        type="text"
        :placeholder="computedPlaceholder"
        :disabled="!!selectedLocation || disabled"
        :class="[
          'form-input pr-10',
          !!selectedLocation || disabled ? 'opacity-50 cursor-not-allowed' : '',
        ]"
        @input="handleLocationSearch"
        @focus="showGeocoding = true"
        @blur="hideGeocodingWithDelay"
      />

      <!-- Geolocation Button -->
      <button
        @click="getCurrentLocation"
        :disabled="!!selectedLocation || geolocationLoading || disabled"
        class="absolute right-2 top-1/2 transform -translate-y-1/2 p-1 text-gray-400 hover:text-blue-600 disabled:opacity-50 disabled:cursor-not-allowed"
        :title="$t('location.actions.use_current')"
        type="button"
      >
        <GlobeAltIcon v-if="!geolocationLoading" class="h-5 w-5" />
        <div v-else class="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"></div>
      </button>
    </div>

    <!-- Geocoding Results Dropdown -->
    <div
      v-if="showGeocoding && (geocodingResults.length > 0 || geocodingLoading)"
      class="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
    >
      <!-- Loading -->
      <div v-if="geocodingLoading" class="p-3 text-center">
        <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mx-auto"></div>
        <span class="text-sm text-gray-500 mt-2 block">{{ $t('location.searching') }}</span>
      </div>

      <!-- Results -->
      <button
        v-for="result in geocodingResults"
        :key="result.properties.id"
        @mousedown="selectLocation(result)"
        class="w-full px-3 py-2 text-left hover:bg-gray-50 border-b border-gray-100 last:border-b-0 focus:outline-hidden focus:bg-gray-50"
        type="button"
      >
        <div class="text-sm text-gray-900">{{ result.properties.label }}</div>
        <div class="text-xs text-gray-500">{{ result.properties.context }}</div>
      </button>

      <!-- No Results -->
      <div
        v-if="!geocodingLoading && geocodingResults.length === 0 && locationSearch"
        class="p-3 text-center text-sm text-gray-500"
      >
        {{ $t('location.no_results') }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import { useI18n } from 'vue-i18n'
import { MapPinIcon, XMarkIcon, GlobeAltIcon } from '@heroicons/vue/24/outline'
import { SelectedLocation } from '@/types/location'
import { useLocationStore } from '../stores/location'

interface GeocodingResult {
  properties: {
    id: string
    label: string
    city: string
    context: string
  }
  geometry: {
    coordinates: [number, number] // [longitude, latitude]
  }
}

interface Props {
  modelValue?: SelectedLocation | null
  id?: string
  label?: string
  placeholder?: string
  disabled?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: SelectedLocation | null): void
  (e: 'change', value: SelectedLocation | null): void
}

const props = withDefaults(defineProps<Props>(), {
  id: 'location',
  label: undefined, // Will use $t('location.title') if not provided
  placeholder: undefined, // Will use $t('location.placeholder') if not provided
  disabled: false,
})

const emit = defineEmits<Emits>()
const locationStore = useLocationStore()
const { t } = useI18n()

// Computed properties for default values
const computedLabel = computed(() => props.label || t('location.title'))
const computedPlaceholder = computed(() => props.placeholder || t('location.placeholder'))

const locationSearch = ref('')
const geocodingResults = ref<GeocodingResult[]>([])
const showGeocoding = ref(false)
const geocodingLoading = ref(false)
const geolocationLoading = ref(false)

const selectedLocation = computed({
  get: () => props.modelValue || null,
  set: (value: SelectedLocation | null) => {
    locationSearch.value = ''
    emit('update:modelValue', value)
    emit('change', value)
  },
})

// Geocoding functionality
const debouncedGeocoding = useDebounceFn(async (query: string) => {
  if (!query || query.length < 2) {
    geocodingResults.value = []
    return
  }

  geocodingLoading.value = true
  try {
    // Build URL with geographical priority if user coordinates are available
    let url = `https://api-adresse.data.gouv.fr/search/?q=${encodeURIComponent(query)}&limit=5`
    if (locationStore.userCoordinates) {
      const [lon, lat] = locationStore.userCoordinates
      url += `&lat=${lat}&lon=${lon}`
    }

    const response = await fetch(url)
    const data = await response.json()
    geocodingResults.value = data.features || []
  } catch (error) {
    console.error('Geocoding error:', error)
    geocodingResults.value = []
  } finally {
    geocodingLoading.value = false
  }
}, 500)

const handleLocationSearch = () => {
  if (selectedLocation.value) return
  debouncedGeocoding(locationSearch.value)
}

const selectLocation = (result: GeocodingResult) => {
  const location: SelectedLocation = {
    label: result.properties.label,
    city: result.properties.city,
    coordinates: result.geometry.coordinates, // [longitude, latitude]
  }

  selectedLocation.value = location

  // Clear search and hide dropdown
  showGeocoding.value = false
  geocodingResults.value = []
}

const clearLocation = () => {
  selectedLocation.value = null
}

const getCurrentLocation = async () => {
  // If user location is already available in store, use it
  if (locationStore.hasUserLocation) {
    selectedLocation.value = locationStore.userLocation
  } else {
    // Trigger location detection - this will update the store and the watcher will handle updating selectedLocation
    await locationStore.initializeUserLocation()
    if (locationStore.hasUserLocation) {
      selectedLocation.value = locationStore.userLocation
    }
  }
}

const hideGeocodingWithDelay = () => {
  setTimeout(() => {
    showGeocoding.value = false
  }, 200)
}
</script>
