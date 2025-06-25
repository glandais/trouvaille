<template>
  <div class="card hover:shadow-lg transition-shadow cursor-pointer" @click="goToDetail">
    <!-- Image -->
    <div class="aspect-w-16 aspect-h-9 bg-gray-200">
      <img
        v-if="annonce.photos && annonce.photos.length > 0"
        :src="getPhotoUrl(annonce.photos[0])"
        :alt="annonce.titre"
        class="w-full h-48 object-cover"
        @error="onImageError"
      />
      <div v-else class="w-full h-48 flex items-center justify-center bg-gray-100">
        <PhotoIcon class="h-12 w-12 text-gray-400" />
      </div>
    </div>

    <!-- Content -->
    <div class="p-4">
      <!-- Title and Price -->
      <div class="flex justify-between items-start mb-2">
        <h3 class="text-lg font-medium text-gray-900 line-clamp-2 flex-1">
          {{ annonce.titre }}
        </h3>
        <div class="ml-2 text-right">
          <p class="text-lg font-bold text-blue-600">
            {{ formatPrice(annonce.prix, annonce.periodeLocation) }}
          </p>
        </div>
      </div>

      <!-- Description -->
      <p class="text-gray-600 text-sm line-clamp-2 mb-3">
        {{ annonce.description }}
      </p>

      <!-- Meta info -->
      <div class="flex items-center justify-between text-xs text-gray-500">
        <div class="flex items-center space-x-4">
          <!-- Type and Nature badges -->
          <span class="badge bg-blue-100 text-blue-800">
            {{ getTypeLabel(annonce.type) }}
          </span>
          <span class="badge bg-green-100 text-green-800">
            {{ getNatureLabel(annonce.nature) }}
          </span>
        </div>

        <!-- Distance (if available) -->
        <span v-if="annonce.distance" class="flex items-center">
          <MapPinIcon class="h-3 w-3 mr-1" />
          {{ annonce.distance }}km
        </span>
      </div>

      <!-- Date -->
      <div class="mt-2 text-xs text-gray-400">
        {{ formatDate(annonce.dateCreation) }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { AnnonceList, AnnonceType, AnnonceNature, PeriodeLocation } from '../api'
import { PhotoIcon, MapPinIcon } from '@heroicons/vue/24/outline'

interface Props {
  annonce: AnnonceList
}

const props = defineProps<Props>()
const router = useRouter()

const goToDetail = () => {
  router.push(`/annonces/${props.annonce.id}`)
}

const getPhotoUrl = (photoId: string) => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return `${baseUrl}/api/v1/photos/${photoId}`
}

const onImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

const formatPrice = (prix?: number, periode?: PeriodeLocation) => {
  if (!prix) return 'Prix non spécifié'
  
  const formattedPrice = new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR'
  }).format(prix)

  if (periode) {
    const periodLabels = {
      [PeriodeLocation.Jour]: '/jour',
      [PeriodeLocation.Semaine]: '/semaine', 
      [PeriodeLocation.Mois]: '/mois'
    }
    return `${formattedPrice}${periodLabels[periode] || ''}`
  }

  return formattedPrice
}

const getTypeLabel = (type?: AnnonceType) => {
  const labels = {
    [AnnonceType.Vente]: 'Vente',
    [AnnonceType.Location]: 'Location'
  }
  return type ? labels[type] : 'N/A'
}

const getNatureLabel = (nature?: AnnonceNature) => {
  const labels = {
    [AnnonceNature.Offre]: 'Offre',
    [AnnonceNature.Demande]: 'Demande'
  }
  return nature ? labels[nature] : 'N/A'
}

const formatDate = (dateString?: string) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diffTime = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays === 0) return 'Aujourd\'hui'
  if (diffDays === 1) return 'Hier'
  if (diffDays < 7) return `Il y a ${diffDays} jours`
  
  return date.toLocaleDateString('fr-FR')
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>