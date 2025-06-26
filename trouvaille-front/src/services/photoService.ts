import { photosApi } from './api'

export type PhotoSize = 'full' | 'thumb'

class PhotoService {
  private cache = new Map<string, string>()

  /**
   * Récupère une photo et retourne une URL blob pour l'affichage
   */
  async getPhotoUrl(photoId: string, size: PhotoSize = 'thumb'): Promise<string> {
    const cacheKey = `${photoId}-${size}`
    
    // Vérifier si la photo est déjà en cache
    if (this.cache.has(cacheKey)) {
      console.log(`Photo ${photoId} (${size}) loaded from cache`)
      return this.cache.get(cacheKey)!
    }

    console.log(`Loading photo ${photoId} (${size}) from API...`)

    try {
      let response
      // Configurer le responseType pour recevoir des données binaires
      const config = { responseType: 'arraybuffer' as const }
      
      if (size === 'full') {
        response = await photosApi.getPhotoFull(photoId, config)
      } else {
        response = await photosApi.getPhotoThumb(photoId, config)
      }

      console.log(`Photo ${photoId} (${size}) API response:`, {
        status: response.status,
        dataType: typeof response.data,
        dataSize: response.data instanceof ArrayBuffer ? response.data.byteLength : 'unknown',
        data: response.data instanceof ArrayBuffer ? 'Binary data' : response.data
      })

      // Vérifier que nous avons des données
      if (!response.data) {
        throw new Error('No data received from API')
      }

      // Si les données sont une chaîne, il y a probablement une erreur
      if (typeof response.data === 'string') {
        console.error(`Photo ${photoId} (${size}) received string instead of binary data:`, response.data)
        throw new Error(`Received string data instead of binary: ${response.data}`)
      }

      // Créer une URL blob à partir des données binaires
      const blob = new Blob([response.data], { type: 'image/jpeg' })
      const url = URL.createObjectURL(blob)
      
      console.log(`Photo ${photoId} (${size}) blob URL created:`, url)
      
      // Mettre en cache
      this.cache.set(cacheKey, url)
      
      return url
    } catch (error) {
      console.error(`Failed to load photo ${photoId} (${size}):`, {
        error,
        photoId,
        size,
        errorMessage: error instanceof Error ? error.message : 'Unknown error'
      })
      throw error
    }
  }

  /**
   * Nettoie une URL blob du cache
   */
  revokePhotoUrl(photoId: string, size: PhotoSize = 'thumb'): void {
    const cacheKey = `${photoId}-${size}`
    const url = this.cache.get(cacheKey)
    
    if (url) {
      URL.revokeObjectURL(url)
      this.cache.delete(cacheKey)
    }
  }

  /**
   * Nettoie toutes les URLs blob du cache
   */
  clearCache(): void {
    for (const url of this.cache.values()) {
      URL.revokeObjectURL(url)
    }
    this.cache.clear()
  }

  /**
   * Précharge une photo en cache
   */
  async preloadPhoto(photoId: string, size: PhotoSize = 'thumb'): Promise<void> {
    try {
      await this.getPhotoUrl(photoId, size)
    } catch (error) {
      // Ignorer les erreurs de préchargement
      console.warn(`Failed to preload photo ${photoId} (${size}):`, error)
    }
  }

  /**
   * Précharge plusieurs photos
   */
  async preloadPhotos(photoIds: string[], size: PhotoSize = 'thumb'): Promise<void> {
    const promises = photoIds.map(id => this.preloadPhoto(id, size))
    await Promise.allSettled(promises)
  }
}

export const photoService = new PhotoService()