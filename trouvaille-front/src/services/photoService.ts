export type PhotoSize = 'full' | 'thumb'

const baseUrl = window.location.origin
class PhotoService {
  getPhotoUrl(photoId: string, size: PhotoSize = 'thumb'): string {
    return baseUrl + '/api/v1/photos/' + photoId + '/' + size
  }
}

export const photoService = new PhotoService()
