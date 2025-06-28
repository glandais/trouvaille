<template>
  <span v-if="formattedDistance" class="text-sm text-gray-500">
    {{ formattedDistance }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useLocationStore } from '../stores/location'
import { useI18nFormatters } from '@/composables/useI18nFormatters'

interface Props {
  // Either provide a pre-calculated distance in kilometers
  distance?: number
  // Or provide coordinates to calculate distance from user's location
  coordinates?: {
    latitude: number
    longitude: number
  }
}

const props = defineProps<Props>()
const locationStore = useLocationStore()
const { formatDistance } = useI18nFormatters()

// Calculate distance using Haversine formula
const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number): number => {
  const R = 6371 // Radius of the Earth in kilometers
  const dLat = toRadians(lat2 - lat1)
  const dLon = toRadians(lon2 - lon1)

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2)

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  const distance = R * c // Distance in kilometers

  return distance
}

const toRadians = (degrees: number): number => {
  return degrees * (Math.PI / 180)
}

// Using i18n formatDistance from composable instead of local function

const formattedDistance = computed(() => {
  // If distance is provided directly, use it
  if (props.distance !== undefined) {
    return formatDistance(props.distance)
  }

  // If coordinates are provided, calculate distance from user's location
  if (props.coordinates && locationStore.userCoordinates) {
    const [userLon, userLat] = locationStore.userCoordinates
    const distance = calculateDistance(
      userLat,
      userLon,
      props.coordinates.latitude,
      props.coordinates.longitude,
    )
    return formatDistance(distance)
  }

  // No distance available
  return null
})
</script>
