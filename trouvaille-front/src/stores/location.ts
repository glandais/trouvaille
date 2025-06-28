import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { SelectedLocation } from '../types/location'

export const useLocationStore = defineStore('location', () => {
  const { t } = useI18n()
  const userLocation = ref<SelectedLocation | null>(null)
  const isDetecting = ref(false)
  const hasAttemptedDetection = ref(false)
  const detectionError = ref<string | null>(null)

  const hasUserLocation = computed(() => !!userLocation.value)
  const userCoordinates = computed(() => userLocation.value?.coordinates || null)

  // Initialize user location on first app load
  const initializeUserLocation = async (): Promise<void> => {
    // Only detect once per session
    if (hasAttemptedDetection.value) {
      return
    }

    hasAttemptedDetection.value = true

    if (!navigator.geolocation) {
      detectionError.value = t('location.error')
      return
    }

    isDetecting.value = true
    detectionError.value = null

    try {
      const position = await getCurrentPosition()
      const lat = position.coords.latitude
      const lon = position.coords.longitude

      // Get address from coordinates using reverse geocoding
      const address = await reverseGeocode(lon, lat)

      userLocation.value = {
        label: address.label,
        city: address.city,
        coordinates: [lon, lat], // [longitude, latitude]
      }

      console.log('User location initialized:', userLocation.value)
    } catch (error) {
      console.warn('Failed to detect user location:', error)
      detectionError.value = getGeolocationErrorMessage(error as GeolocationPositionError)
    } finally {
      isDetecting.value = false
    }
  }

  // Manual location update (for when user explicitly sets their location)
  const updateUserLocation = (location: SelectedLocation | null): void => {
    userLocation.value = location
  }

  // Clear user location
  const clearUserLocation = (): void => {
    userLocation.value = null
    detectionError.value = null
  }

  // Reset detection state (for testing or re-initialization)
  const resetDetectionState = (): void => {
    hasAttemptedDetection.value = false
    detectionError.value = null
  }

  // Get current position as a Promise
  const getCurrentPosition = (): Promise<GeolocationPosition> => {
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: false, // Balance between accuracy and speed
        timeout: 8000, // 8 second timeout
        maximumAge: 300000, // 5 minute cache
      })
    })
  }

  // Reverse geocode coordinates to get human-readable address
  const reverseGeocode = async (
    lon: number,
    lat: number,
  ): Promise<{ label: string; city: string }> => {
    try {
      const response = await fetch(
        `https://api-adresse.data.gouv.fr/reverse/?lon=${lon}&lat=${lat}`,
      )
      const data = await response.json()

      const feature = data.features?.[0]
      if (feature) {
        return {
          label: feature.properties.label || `Position (${lat.toFixed(4)}, ${lon.toFixed(4)})`,
          city: feature.properties.city || t('location.unknown'),
        }
      }
    } catch (error) {
      console.warn('Reverse geocoding failed:', error)
    }

    // Fallback to coordinates
    return {
      label: `Position (${lat.toFixed(4)}, ${lon.toFixed(4)})`,
      city: t('location.unknown'),
    }
  }

  // Get user-friendly error message
  const getGeolocationErrorMessage = (error: GeolocationPositionError): string => {
    switch (error.code) {
      case error.PERMISSION_DENIED:
        return t('location.permission_denied')
      case error.POSITION_UNAVAILABLE:
        return t('location.error')
      case error.TIMEOUT:
        return t('errors.timeout')
      default:
        return t('location.error')
    }
  }

  return {
    // State
    userLocation: computed(() => userLocation.value),
    isDetecting: computed(() => isDetecting.value),
    hasAttemptedDetection: computed(() => hasAttemptedDetection.value),
    detectionError: computed(() => detectionError.value),

    // Computed
    hasUserLocation,
    userCoordinates,

    // Actions
    initializeUserLocation,
    updateUserLocation,
    clearUserLocation,
    resetDetectionState,
  }
})
