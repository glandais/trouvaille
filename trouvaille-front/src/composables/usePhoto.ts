import { ref, onUnmounted, watch, type Ref } from 'vue'
import { photoService, type PhotoSize } from '../services/photoService'

/**
 * Composable pour gérer l'affichage des photos avec gestion d'état
 */
export function usePhoto(photoId: string | Ref<string | null>, size: PhotoSize = 'thumb') {
  const url = ref<string>('')
  const loading = ref(false)
  const error = ref<string>('')

  const loadPhoto = async (id: string) => {
    if (!id) {
      url.value = ''
      return
    }

    loading.value = true
    error.value = ''

    try {
      const photoUrl = await photoService.getPhotoUrl(id, size)
      url.value = photoUrl
    } catch (err) {
      error.value = 'Erreur lors du chargement de la photo'
      console.error('Photo loading error:', err)
    } finally {
      loading.value = false
    }
  }

  const cleanup = () => {
    if (url.value) {
      const currentId = typeof photoId === 'string' ? photoId : photoId.value
      if (currentId) {
        photoService.revokePhotoUrl(currentId, size)
      }
      url.value = ''
    }
  }

  // Surveiller les changements d'ID de photo
  watch(
    () => (typeof photoId === 'string' ? photoId : photoId.value),
    (newId, oldId) => {
      if (oldId && url.value) {
        photoService.revokePhotoUrl(oldId, size)
        url.value = ''
      }
      if (newId) {
        loadPhoto(newId)
      }
    },
    { immediate: true },
  )

  // Nettoyer automatiquement quand le composant est démonté
  onUnmounted(cleanup)

  return {
    url,
    loading,
    error,
    loadPhoto: () => {
      const currentId = typeof photoId === 'string' ? photoId : photoId.value
      if (currentId) loadPhoto(currentId)
    },
    cleanup,
  }
}

/**
 * Composable pour gérer plusieurs photos
 */
export function usePhotos(photoIds: string[] | Ref<string[]>, size: PhotoSize = 'thumb') {
  const urls = ref<Record<string, string>>({})
  const loading = ref(false)
  const errors = ref<Record<string, string>>({})

  const loadPhotos = async (ids: string[]) => {
    if (!ids.length) {
      urls.value = {}
      return
    }

    loading.value = true
    errors.value = {}

    const promises = ids.map(async (photoId) => {
      try {
        const url = await photoService.getPhotoUrl(photoId, size)
        urls.value[photoId] = url
      } catch (err) {
        errors.value[photoId] = 'Erreur lors du chargement'
        console.error(`Photo loading error for ${photoId}:`, err)
      }
    })

    await Promise.allSettled(promises)
    loading.value = false
  }

  const cleanup = () => {
    const currentIds = Array.isArray(photoIds) ? photoIds : photoIds.value
    for (const photoId of currentIds) {
      photoService.revokePhotoUrl(photoId, size)
    }
    urls.value = {}
  }

  // Surveiller les changements d'IDs de photos
  watch(
    () => (Array.isArray(photoIds) ? photoIds : photoIds.value),
    (newIds, oldIds) => {
      // Nettoyer les anciennes photos qui ne sont plus dans la liste
      if (oldIds) {
        for (const oldId of oldIds) {
          if (!newIds.includes(oldId) && urls.value[oldId]) {
            photoService.revokePhotoUrl(oldId, size)
            delete urls.value[oldId]
          }
        }
      }
      // Charger les nouvelles photos
      if (newIds.length > 0) {
        loadPhotos(newIds)
      }
    },
    { immediate: true },
  )

  // Nettoyer automatiquement quand le composable est démonté
  onUnmounted(cleanup)

  return {
    urls,
    loading,
    errors,
    loadPhotos: () => loadPhotos(Array.isArray(photoIds) ? photoIds : photoIds.value),
    cleanup,
  }
}
