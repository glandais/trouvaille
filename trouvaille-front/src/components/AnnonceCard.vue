<template>
  <div :class="['card hover:shadow-lg transition-shadow', 'cursor-pointer']" @click="goToDetail()">
    <!-- Image -->
    <div class="aspect-w-16 aspect-h-9 bg-gray-200 relative">
      <img
        v-if="photoUrl && !photoError"
        :src="photoUrl"
        :alt="annonce.titre"
        :class="['w-full h-48 object-cover', 'cursor-pointer']"
        @click="goToDetail()"
        @error="onImageError"
      />
      <div
        v-else-if="photoLoading"
        :class="['w-full h-48 flex items-center justify-center bg-gray-100', 'cursor-pointer']"
        @click="goToDetail()"
      >
        <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
      </div>
      <div
        v-else-if="photoError"
        :class="['w-full h-48 flex items-center justify-center bg-red-50', 'cursor-pointer']"
        @click="goToDetail()"
      >
        <div class="text-center">
          <PhotoIcon class="h-12 w-12 text-red-400 mx-auto mb-1" />
          <p class="text-xs text-red-600">Erreur image</p>
        </div>
      </div>
      <div
        v-else
        :class="['w-full h-48 flex items-center justify-center bg-gray-100', 'cursor-pointer']"
        @click="goToDetail()"
      >
        <PhotoIcon class="h-12 w-12 text-gray-400" />
      </div>

      <!-- Status Badge (owner mode only) -->
      <div v-if="isOwner" class="absolute top-2 left-2">
        <span :class="getStatutBadgeClass(annonce.statut)">
          {{ getStatutLabel(annonce.statut) }}
        </span>
      </div>

      <!-- Quick Actions (owner mode only) -->
      <div
        v-if="isOwner"
        class="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity"
      >
        <div class="flex space-x-1">
          <button
            @click.stop="handleEdit"
            class="p-2 bg-white bg-opacity-90 rounded-full text-gray-600 hover:text-blue-600 transition-colors"
            title="Modifier"
          >
            <PencilIcon class="h-4 w-4" />
          </button>
          <button
            @click.stop="handleDelete"
            class="p-2 bg-white bg-opacity-90 rounded-full text-gray-600 hover:text-red-600 transition-colors"
            title="Supprimer"
          >
            <TrashIcon class="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div class="p-4">
      <!-- Title and Price -->
      <div class="flex justify-between items-start mb-2">
        <h3
          :class="[
            'text-lg font-medium text-gray-900 line-clamp-2 flex-1',
            'cursor-pointer hover:text-blue-600',
          ]"
          @click="goToDetail()"
        >
          {{ annonce.titre }}
        </h3>
        <div class="ml-2 text-right">
          <p class="text-lg font-bold text-blue-600">
            {{ formatPrice(annonce.prix, annonce.periode_location) }}
          </p>
        </div>
      </div>

      <!-- Meta info -->
      <div
        :class="[
          'flex items-center text-xs text-gray-500',
          isOwner ? 'justify-between mb-3' : 'justify-between',
        ]"
      >
        <div :class="['flex items-center', isOwner ? 'space-x-2' : 'space-x-4']">
          <!-- Type and Nature badges -->
          <span class="badge bg-blue-100 text-blue-800">
            {{ getTypeLabel(annonce.type) }}
          </span>
          <span class="badge bg-green-100 text-green-800">
            {{ getNatureLabel(annonce.nature) }}
          </span>
        </div>

        <!-- Distance (if available and not in owner mode) -->
        <div v-if="annonce.distance || annonce.coordinates" class="flex items-center">
          <MapPinIcon class="h-3 w-3 mr-1" />
          <DistanceDisplay :distance="annonce.distance" :coordinates="annonce.coordinates" />
        </div>
      </div>

      <!-- Date and Actions -->
      <div :class="[isOwner ? 'flex items-center justify-between' : 'mt-2']">
        <div class="text-xs text-gray-400">
          {{ formatDate(annonce.date_creation) }}
          <span
            v-if="annonce.date_modification && annonce.date_modification !== annonce.date_creation"
          >
            • Modifié {{ formatDate(annonce.date_modification) }}
          </span>
        </div>
        <div class="text-xs text-gray-400">
          Publié par {{ annonce.utilisateur?.nickname || annonce.utilisateur?.username }}
        </div>

        <!-- Status Actions (owner mode only) -->
        <div v-if="isOwner" class="flex space-x-1">
          <button
            v-if="annonce.statut === AnnonceStatut.Active"
            @click.stop="handleChangeStatus(AnnonceStatut.Suspendue)"
            class="text-xs text-yellow-600 hover:text-yellow-700 underline"
            title="Suspendre"
          >
            Suspendre
          </button>
          <button
            v-if="annonce.statut === AnnonceStatut.Suspendue"
            @click.stop="handleChangeStatus(AnnonceStatut.Active)"
            class="text-xs text-green-600 hover:text-green-700 underline"
            title="Réactiver"
          >
            Réactiver
          </button>
          <button
            v-if="annonce.statut === AnnonceStatut.Active && annonce.type === AnnonceType.Vente"
            @click.stop="handleChangeStatus(AnnonceStatut.Vendue)"
            class="text-xs text-gray-600 hover:text-gray-700 underline"
            title="Marquer comme vendu"
          >
            Vendu
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {
  AnnonceList,
  AnnonceType,
  AnnonceWithStatut,
  AnnonceNature,
  AnnonceStatut,
  PeriodeLocation,
} from '../api'
import { PhotoIcon, MapPinIcon, PencilIcon, TrashIcon } from '@heroicons/vue/24/outline'
import { usePhoto } from '../composables/usePhoto'
import { annoncesApi } from '../services/api'
import DistanceDisplay from './DistanceDisplay.vue'
const authStore = useAuthStore()

interface Props {
  annonce: AnnonceList
}

interface Emits {
  (e: 'updated'): void
  (e: 'deleted', annonceId: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const router = useRouter()

const isOwner = computed(() => {
  return authStore.user?.id === props.annonce.utilisateur?.id
})

const goToDetail = () => {
  router.push(`/annonces/${props.annonce.id}`)
}

// Utiliser le composable pour charger la première photo
const firstPhotoId = computed(() => props.annonce.photos?.[0] || null)

const { url: photoUrl, loading: photoLoading, error: photoError } = usePhoto(firstPhotoId, 'thumb')

// Fonction pour gérer les erreurs d'images
const onImageError = () => {
  // L'erreur sera gérée par le composable usePhoto
}

const formatPrice = (prix?: number, periode?: PeriodeLocation) => {
  if (!prix) return 'Prix non spécifié'

  const formattedPrice = new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR',
  }).format(prix)

  if (periode) {
    const periodLabels = {
      [PeriodeLocation.Jour]: '/jour',
      [PeriodeLocation.Semaine]: '/semaine',
      [PeriodeLocation.Mois]: '/mois',
    }
    return `${formattedPrice}${periodLabels[periode] || ''}`
  }

  return formattedPrice
}

const getTypeLabel = (type?: AnnonceType) => {
  const labels = {
    [AnnonceType.Vente]: 'Vente',
    [AnnonceType.Location]: 'Location',
  }
  return type ? labels[type] : 'N/A'
}

const getNatureLabel = (nature?: AnnonceNature) => {
  const labels = {
    [AnnonceNature.Offre]: 'Offre',
    [AnnonceNature.Demande]: 'Demande',
  }
  return nature ? labels[nature] : 'N/A'
}

const getStatutLabel = (statut?: AnnonceStatut) => {
  const labels = {
    [AnnonceStatut.Active]: 'Active',
    [AnnonceStatut.Suspendue]: 'Suspendue',
    [AnnonceStatut.Vendue]: 'Vendue',
  }
  return statut ? labels[statut] : 'N/A'
}

const getStatutBadgeClass = (statut?: AnnonceStatut) => {
  const classes = {
    [AnnonceStatut.Active]: 'badge badge-active',
    [AnnonceStatut.Suspendue]: 'badge badge-suspended',
    [AnnonceStatut.Vendue]: 'badge badge-sold',
  }
  return statut ? classes[statut] : 'badge'
}

const handleEdit = () => {
  router.push(`/annonces/${props.annonce.id}/edit`)
}

const handleDelete = () => {
  if (!props.annonce.id) return

  const confirmed = confirm(
    `Êtes-vous sûr de vouloir supprimer "${props.annonce.titre}" ? Cette action est irréversible.`,
  )

  if (confirmed) {
    deleteAnnonce()
  }
}

const deleteAnnonce = async () => {
  if (!props.annonce.id) return

  try {
    await annoncesApi.deleteAnnonce(props.annonce.id)
    emit('deleted', props.annonce.id)
  } catch (error) {
    console.error('Failed to delete annonce:', error)
    alert("Erreur lors de la suppression de l'annonce")
  }
}

const handleChangeStatus = async (newStatut: AnnonceStatut) => {
  if (!props.annonce.id) return

  try {
    const updateData: AnnonceWithStatut = {
      ...props.annonce,
      statut: newStatut,
    }
    updateData.statut = newStatut

    await annoncesApi.putAnnonce(props.annonce.id, updateData)

    // Update local annonce object
    props.annonce.statut = newStatut
    emit('updated')
  } catch (error) {
    console.error('Failed to update annonce status:', error)
    alert('Erreur lors de la mise à jour du statut')
  }
}

const formatDate = (dateString?: string) => {
  if (!dateString) return ''

  const date = new Date(dateString)
  const now = new Date()
  const diffTime = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays === 0) return "Aujourd'hui"
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

.group:hover .group-hover\:opacity-100 {
  opacity: 1;
}
</style>
