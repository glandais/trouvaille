<template>
  <div class="relative">
    <label v-if="label" :for="id" class="block text-sm font-medium text-gray-700 mb-1">
      {{ label }}
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
          title="Supprimer la localisation"
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
    <div class="relative">
      <input
        :id="id"
        v-model="locationSearch"
        type="text"
        :placeholder="placeholder"
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
        title="Utiliser ma position"
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
        <span class="text-sm text-gray-500 mt-2 block">Recherche...</span>
      </div>

      <!-- Results -->
      <button
        v-for="result in geocodingResults"
        :key="result.properties.id"
        @mousedown="selectLocation(result)"
        class="w-full px-3 py-2 text-left hover:bg-gray-50 border-b border-gray-100 last:border-b-0 focus:outline-none focus:bg-gray-50"
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
        Aucun résultat trouvé
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useDebounceFn } from '@vueuse/core'
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
  autoDetect?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: SelectedLocation | null): void
  (e: 'change', value: SelectedLocation | null): void
}

const props = withDefaults(defineProps<Props>(), {
  id: 'location',
  label: 'Localisation',
  placeholder: 'Adresse, ville...',
  disabled: false,
  autoDetect: true,
})

const emit = defineEmits<Emits>()
const locationStore = useLocationStore()

const locationSearch = ref('')
const geocodingResults = ref<GeocodingResult[]>([])
const showGeocoding = ref(false)
const geocodingLoading = ref(false)
const geolocationLoading = ref(false)

const selectedLocation = computed({
  get: () => props.modelValue || null,
  set: (value: SelectedLocation | null) => {
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
  locationSearch.value = ''
  showGeocoding.value = false
  geocodingResults.value = []
}

const clearLocation = () => {
  selectedLocation.value = null
  locationSearch.value = ''
}

const getCurrentLocation = async () => {
  // If user location is already available in store, use it
  if (locationStore.hasUserLocation) {
    selectedLocation.value = locationStore.userLocation
  } else {
    await locationStore.initializeUserLocation()
  }
}

const hideGeocodingWithDelay = () => {
  setTimeout(() => {
    showGeocoding.value = false
  }, 200)
}

// Auto-detect location on component mount
onMounted(() => {
  // If auto-detect is enabled and no location is selected, use store's location or trigger detection
  if (props.autoDetect && !selectedLocation.value) {
    if (locationStore.userLocation) {
      // Use existing location from store
      selectedLocation.value = locationStore.userLocation
    } else if (!locationStore.hasAttemptedDetection) {
      // Trigger location detection if not attempted yet
      locationStore.initializeUserLocation()
    }
  }
})

// Watch for external changes to clear the search input
watch(
  () => props.modelValue,
  (newValue) => {
    if (!newValue) {
      locationSearch.value = ''
    }
  },
)
</script>
