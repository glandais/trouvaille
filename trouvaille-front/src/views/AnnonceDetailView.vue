<template>
  <AppLayout>
    <!-- Loading State -->
    <div v-if="loading" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/4 mb-8"></div>
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div class="h-96 bg-gray-200 rounded"></div>
          <div class="space-y-4">
            <div class="h-6 bg-gray-200 rounded"></div>
            <div class="h-4 bg-gray-200 rounded w-3/4"></div>
            <div class="h-20 bg-gray-200 rounded"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="text-center py-12">
        <h2 class="text-2xl font-semibold text-gray-900 mb-2">Annonce non trouvée</h2>
        <p class="text-gray-600 mb-6">
          L'annonce que vous cherchez n'existe pas ou a été supprimée.
        </p>
        <router-link to="/annonces" class="btn-primary"> Retour aux annonces </router-link>
      </div>
    </div>

    <!-- Annonce Content -->
    <div v-else-if="annonce" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Breadcrumb -->
      <nav class="flex mb-8" aria-label="Breadcrumb">
        <ol class="flex items-center space-x-4">
          <li>
            <router-link to="/" class="text-gray-400 hover:text-gray-500"> Accueil </router-link>
          </li>
          <li>
            <ChevronRightIcon class="h-4 w-4 text-gray-400" />
          </li>
          <li>
            <router-link to="/annonces" class="text-gray-400 hover:text-gray-500">
              Annonces
            </router-link>
          </li>
          <li>
            <ChevronRightIcon class="h-4 w-4 text-gray-400" />
          </li>
          <li class="text-gray-500 truncate">{{ annonce.titre }}</li>
        </ol>
      </nav>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- Photo Gallery -->
        <div class="space-y-4">
          <!-- Main Photo -->
          <div class="aspect-w-16 aspect-h-12 bg-gray-100 rounded-lg overflow-hidden">
            <img
              v-if="fullPhotoUrl && !fullPhotoError"
              :src="fullPhotoUrl"
              :alt="annonce.titre"
              class="w-full h-96 object-cover cursor-pointer"
              @click="openPhotoModal"
              @error="onImageError"
            />
            <div
              v-else-if="fullPhotoLoading"
              class="w-full h-96 flex items-center justify-center bg-gray-100"
            >
              <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <span class="ml-2 text-sm text-gray-600">Chargement de la photo...</span>
            </div>
            <div
              v-else-if="fullPhotoError"
              class="w-full h-96 flex items-center justify-center bg-red-50"
            >
              <div class="text-center">
                <PhotoIcon class="h-16 w-16 text-red-400 mx-auto mb-2" />
                <p class="text-sm text-red-600">Erreur de chargement</p>
              </div>
            </div>
            <div v-else class="w-full h-96 flex items-center justify-center bg-gray-100">
              <PhotoIcon class="h-16 w-16 text-gray-400" />
            </div>
          </div>

          <!-- Photo Thumbnails -->
          <div v-if="annonce.photos && annonce.photos.length > 1" class="grid grid-cols-4 gap-2">
            <button
              v-for="(photo, index) in annonce.photos"
              :key="photo"
              @click="currentPhotoIndex = index"
              :class="[
                'aspect-w-1 aspect-h-1 rounded-lg overflow-hidden border-2 transition-colors',
                currentPhotoIndex === index
                  ? 'border-blue-500'
                  : 'border-gray-200 hover:border-gray-300',
              ]"
            >
              <img
                v-if="thumbUrls[photo] && !thumbErrors[photo]"
                :src="thumbUrls[photo]"
                :alt="`Photo ${index + 1}`"
                class="w-full h-20 object-cover"
                @error="onImageError"
              />
              <div
                v-else-if="thumbErrors[photo]"
                class="w-full h-20 flex items-center justify-center bg-red-50"
              >
                <PhotoIcon class="h-4 w-4 text-red-400" />
              </div>
              <div v-else class="w-full h-20 flex items-center justify-center bg-gray-100">
                <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
              </div>
            </button>
          </div>
        </div>

        <!-- Annonce Details -->
        <div class="space-y-6">
          <!-- Header -->
          <div>
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center space-x-2">
                <span class="badge bg-blue-100 text-blue-800">
                  {{ getTypeLabel(annonce.type) }}
                </span>
                <span class="badge bg-green-100 text-green-800">
                  {{ getNatureLabel(annonce.nature) }}
                </span>
                <span :class="getStatutBadgeClass(annonce.statut)">
                  {{ getStatutLabel(annonce.statut) }}
                </span>
              </div>
            </div>

            <h1 class="text-3xl font-bold text-gray-900 mb-4">{{ annonce.titre }}</h1>

            <div class="text-3xl font-bold text-blue-600 mb-4">
              {{ formatPrice(annonce.prix, annonce.periode_location) }}
            </div>
          </div>

          <!-- Description -->
          <div>
            <h3 class="text-lg font-medium text-gray-900 mb-3">Description</h3>
            <MarkdownViewer :model-value="annonce.description || ''" />
          </div>

          <!-- Location -->
          <div v-if="annonce.ville || annonce.coordinates">
            <h3 class="text-lg font-medium text-gray-900 mb-3">Localisation</h3>
            <div class="flex items-center space-x-2 text-gray-600">
              <MapPinIcon class="h-5 w-5" />
              <span>{{ annonce.ville || 'Localisation définie' }}</span>
              <DistanceDisplay
                :distance="(annonce as any).distance"
                :coordinates="annonce.coordinates"
              />
            </div>
          </div>

          <!-- User Info -->
          <div>
            <h3 class="text-lg font-medium text-gray-900 mb-3">Publié par</h3>
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                <span class="text-blue-600 font-medium">
                  {{
                    (annonce.utilisateur?.nickname || annonce.utilisateur?.username || 'U')
                      ?.charAt(0)
                      ?.toUpperCase()
                  }}
                </span>
              </div>
              <div>
                <p class="font-medium text-gray-900">
                  {{
                    annonce.utilisateur?.nickname ||
                    annonce.utilisateur?.username ||
                    'Utilisateur inconnu'
                  }}
                </p>
                <p class="text-sm text-gray-500">
                  Publié {{ formatDate(annonce.date_creation) }}
                  <span
                    v-if="
                      annonce.date_modification &&
                      annonce.date_modification !== annonce.date_creation
                    "
                  >
                    • Modifié {{ formatDate(annonce.date_modification) }}
                  </span>
                </p>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="pt-6 border-t border-gray-200">
            <div v-if="isOwner" class="space-y-3">
              <router-link
                :to="`/annonces/${annonce.id}/edit`"
                class="btn-primary w-full text-center block"
              >
                <PencilIcon class="h-4 w-4 mr-2 inline" />
                Modifier l'annonce
              </router-link>
              <button
                @click="confirmDelete"
                class="w-full px-4 py-2 text-red-600 border border-red-300 rounded-lg hover:bg-red-50 transition-colors"
              >
                <TrashIcon class="h-4 w-4 mr-2 inline" />
                Supprimer l'annonce
              </button>
            </div>
            <div v-else class="space-y-3">
              <button class="btn-primary w-full">
                <ChatBubbleLeftIcon class="h-4 w-4 mr-2 inline" />
                Contacter le vendeur
              </button>
              <button class="btn-secondary w-full">
                <HeartIcon class="h-4 w-4 mr-2 inline" />
                Ajouter aux favoris
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Photo Viewer -->
      <PhotoViewer
        :show="showPhotoModal"
        :photo-url="fullPhotoUrl"
        :alt="annonce.titre"
        :loading="fullPhotoLoading"
        :show-previous="hasPhotos && currentPhotoIndex > 0"
        :show-next="hasPhotos && currentPhotoIndex < totalPhotos - 1"
        :current-index="currentPhotoIndex"
        :total-count="totalPhotos"
        @close="closePhotoModal"
        @previous="previousPhoto"
        @next="nextPhoto"
        @error="onPhotoViewerError"
      />
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { annoncesApi } from '../services/api'
import { Annonce, AnnonceType, AnnonceNature, AnnonceStatut, PeriodeLocation } from '../api'
import AppLayout from '../components/AppLayout.vue'
import PhotoViewer from '../components/PhotoViewer.vue'
import MarkdownViewer from '../components/MarkdownViewer.vue'
import DistanceDisplay from '../components/DistanceDisplay.vue'
import {
  PhotoIcon,
  MapPinIcon,
  PencilIcon,
  TrashIcon,
  ChatBubbleLeftIcon,
  HeartIcon,
  ChevronRightIcon,
  ChevronLeftIcon,
  XMarkIcon,
} from '@heroicons/vue/24/outline'
import { usePhotos, usePhoto } from '../composables/usePhoto'

interface Props {
  id: string
}

const props = defineProps<Props>()
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const annonce = ref<Annonce>()
const loading = ref(true)
const error = ref(false)
const currentPhotoIndex = ref(0)
const showPhotoModal = ref(false)

const currentPhoto = computed(() => {
  if (!annonce.value?.photos || annonce.value.photos.length === 0) return null
  return annonce.value.photos[currentPhotoIndex.value]
})

// Utiliser les composables pour charger les photos
const photoIds = computed(() => annonce.value?.photos || [])
const {
  urls: thumbUrls,
  loading: thumbsLoading,
  errors: thumbErrors,
} = usePhotos(photoIds, 'thumb')
const {
  url: fullPhotoUrl,
  loading: fullPhotoLoading,
  error: fullPhotoError,
} = usePhoto(currentPhoto, 'full')

const isOwner = computed(() => {
  return authStore.user?.id === annonce.value?.utilisateur?.id
})

const hasPhotos = computed(() => {
  return annonce.value?.photos && annonce.value.photos.length > 0
})

const totalPhotos = computed(() => {
  return annonce.value?.photos?.length || 0
})

const fetchAnnonce = async () => {
  try {
    loading.value = true
    const response = await annoncesApi.getAnnonce(props.id!)
    annonce.value = response.data
  } catch (err) {
    console.error('Failed to fetch annonce:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

// Fonction pour déboguer les erreurs d'images
const onImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  console.error('Image error:', {
    src: img.src,
    currentPhoto: currentPhoto.value,
    fullPhotoUrl: fullPhotoUrl.value,
    fullPhotoError: fullPhotoError.value,
    fullPhotoLoading: fullPhotoLoading.value,
  })
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

const formatDate = (dateString?: string) => {
  if (!dateString) return ''

  const date = new Date(dateString)
  const now = new Date()
  const diffTime = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays === 0) return "aujourd'hui"
  if (diffDays === 1) return 'hier'
  if (diffDays < 7) return `il y a ${diffDays} jours`

  return `le ${date.toLocaleDateString('fr-FR')}`
}

const openPhotoModal = () => {
  showPhotoModal.value = true
}

const closePhotoModal = () => {
  showPhotoModal.value = false
}

const previousPhoto = () => {
  if (currentPhotoIndex.value > 0) {
    currentPhotoIndex.value--
  }
}

const nextPhoto = () => {
  if (hasPhotos.value && currentPhotoIndex.value < totalPhotos.value - 1) {
    currentPhotoIndex.value++
  }
}

const onPhotoViewerError = () => {
  console.error('Photo viewer error for photo:', currentPhoto.value)
}

const confirmDelete = () => {
  if (
    confirm('Êtes-vous sûr de vouloir supprimer cette annonce ? Cette action est irréversible.')
  ) {
    deleteAnnonce()
  }
}

const deleteAnnonce = async () => {
  try {
    await annoncesApi.deleteAnnonce(props.id)
    router.push('/my-annonces')
  } catch (error) {
    console.error('Failed to delete annonce:', error)
    alert("Erreur lors de la suppression de l'annonce")
  }
}

onMounted(() => {
  fetchAnnonce()
})
</script>

<style scoped>
.prose img {
  max-width: 100%;
  height: auto;
}
</style>
