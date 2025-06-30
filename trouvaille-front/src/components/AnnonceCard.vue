<template>
  <div
    class="group bg-white rounded-lg shadow-sm hover:shadow-md transition-all duration-200 overflow-hidden border border-gray-100"
  >
    <!-- Image Container -->
    <div class="relative aspect-[4/3] bg-gray-100" @click="goToDetail()">
      <img
        v-if="photoUrl && !photoError"
        :src="photoUrl"
        :alt="annonce.titre"
        class="w-full h-full object-cover cursor-pointer"
        @error="onImageError"
      />
      <div
        v-else-if="photoLoading"
        class="w-full h-full flex items-center justify-center cursor-pointer"
      >
        <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
      </div>
      <div
        v-else-if="photoError"
        class="w-full h-full flex items-center justify-center bg-red-50 cursor-pointer"
      >
        <div class="text-center">
          <PhotoIcon class="h-12 w-12 text-red-400 mx-auto mb-1" />
          <p class="text-xs text-red-600">{{ $t('errors.image_load') }}</p>
        </div>
      </div>
      <div v-else class="w-full h-full flex items-center justify-center cursor-pointer">
        <PhotoIcon class="h-12 w-12 text-gray-400" />
      </div>

      <!-- Status Badge -->
      <div v-if="isOwner" class="absolute top-3 left-3">
        <span
          :class="getStatutBadgeClass(annonce.statut)"
          class="text-xs font-medium px-2 py-1 rounded-full"
        >
          {{ getStatutLabel(annonce.statut) }}
        </span>
      </div>

      <!-- Price Overlay -->
      <div class="absolute bottom-3 right-3">
        <div class="bg-white/90 backdrop-blur-sm rounded-lg px-3 py-1.5 shadow-sm">
          <p class="text-lg font-bold text-gray-900">
            {{ formatPrice(annonce.prix, annonce.periode_location) }}
          </p>
        </div>
      </div>

      <!-- Owner Actions -->
      <div
        v-if="isOwner"
        class="absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition-opacity duration-200"
      >
        <div class="flex gap-1">
          <button
            @click.stop="handleEdit"
            class="p-1.5 bg-white/90 backdrop-blur-sm rounded-full text-gray-600 hover:text-blue-600 hover:bg-white transition-all"
            :title="$t('common.actions.edit')"
          >
            <PencilIcon class="h-4 w-4" />
          </button>
          <button
            @click.stop="handleDelete"
            class="p-1.5 bg-white/90 backdrop-blur-sm rounded-full text-gray-600 hover:text-red-600 hover:bg-white transition-all"
            :title="$t('common.actions.delete')"
          >
            <TrashIcon class="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div class="p-4 space-y-3">
      <!-- Title -->
      <h3
        class="text-lg font-semibold text-gray-900 line-clamp-2 cursor-pointer hover:text-blue-600 transition-colors"
        @click="goToDetail()"
      >
        {{ annonce.titre }}
      </h3>

      <!-- Badges Row -->
      <div class="flex items-center gap-2 flex-wrap">
        <span
          class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
        >
          {{ getTypeLabel(annonce.type) }}
        </span>
        <span
          class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800"
        >
          {{ getNatureLabel(annonce.nature) }}
        </span>
      </div>

      <!-- Location & Distance -->
      <div
        v-if="annonce.ville || annonce.distance || annonce.coordinates"
        class="flex items-center text-sm text-gray-600"
      >
        <MapPinIcon class="h-4 w-4 mr-1.5 text-gray-400" />
        <span>{{ annonce.ville || $t('location.title') }}</span>
        <DistanceDisplay
          :distance="annonce.distance"
          :coordinates="annonce.coordinates"
          class="ml-2"
        />
      </div>

      <!-- Footer -->
      <div class="pt-2 border-t border-gray-100">
        <div class="flex items-center justify-between text-xs text-gray-500">
          <!-- Author & Date -->
          <div class="flex-1">
            <div class="font-medium text-gray-700">
              {{ annonce.utilisateur?.nickname || annonce.utilisateur?.username }}
            </div>
            <div class="mt-0.5">
              {{ formatSmartDate(annonce.date_creation) }}
              <span
                v-if="
                  annonce.date_modification && annonce.date_modification !== annonce.date_creation
                "
                class="text-gray-400"
              >
                • {{ $t('dates.modified', { date: formatSmartDate(annonce.date_modification) }) }}
              </span>
            </div>
          </div>

          <!-- Owner Status Actions -->
          <div v-if="isOwner" class="flex gap-2">
              <router-link
                :to="`/annonces/${annonce.id}/edit`"
                class="text-xs font-medium text-amber-600 hover:text-amber-700 px-2 py-1 rounded hover:bg-amber-50 transition-colors"
              >
                {{ $t('common.actions.edit') }}
              </router-link>
            <button
              v-if="annonce.statut === AnnonceStatut.Active"
              @click.stop="handleChangeStatus(AnnonceStatut.Suspendue)"
              class="text-xs font-medium text-amber-600 hover:text-amber-700 px-2 py-1 rounded hover:bg-amber-50 transition-colors"
              :title="$t('annonce.status.suspendue')"
            >
              {{ $t('common.actions.suspend') }}
            </button>
            <button
              v-if="annonce.statut === AnnonceStatut.Suspendue"
              @click.stop="handleChangeStatus(AnnonceStatut.Active)"
              class="text-xs font-medium text-emerald-600 hover:text-emerald-700 px-2 py-1 rounded hover:bg-emerald-50 transition-colors"
              :title="$t('common.actions.edit')"
            >
              {{ $t('common.actions.activate') }}
            </button>
            <button
              v-if="annonce.statut === AnnonceStatut.Active && annonce.type === AnnonceType.Vente"
              @click.stop="handleChangeStatus(AnnonceStatut.Vendue)"
              class="text-xs font-medium text-gray-600 hover:text-gray-700 px-2 py-1 rounded hover:bg-gray-50 transition-colors"
              :title="$t('annonce.status.vendue')"
            >
              {{ $t('common.actions.markSold') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'
import { AnnonceList, AnnonceType, AnnonceWithStatut, AnnonceStatut, PeriodeLocation } from '../api'
import { PhotoIcon, MapPinIcon, PencilIcon, TrashIcon } from '@heroicons/vue/24/outline'
import { usePhoto } from '../composables/usePhoto'
import { annoncesApi } from '../services/api'
import { useI18nFormatters } from '@/composables/useI18nFormatters'
import { useAnnonceLabels } from '@/composables/useAnnonceLabels'
import DistanceDisplay from './DistanceDisplay.vue'
const authStore = useAuthStore()
const { t } = useI18n()
const { formatSmartDate, formatPrice: formatPriceI18n } = useI18nFormatters()
const { getTypeLabel, getNatureLabel } = useAnnonceLabels()

interface Props {
  annonce: AnnonceList
}

interface Emits {
  (e: 'updated', updatedAnnonce?: AnnonceList): void
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

// Use i18n formatter for price
const formatPrice = (prix?: number, periode?: PeriodeLocation) => {
  return formatPriceI18n(prix, periode)
}

// Use i18n labels from composable

const getStatutLabel = (statut?: AnnonceStatut) => {
  const labels = {
    [AnnonceStatut.Active]: t('annonce.status.active'),
    [AnnonceStatut.Suspendue]: t('annonce.status.suspendue'),
    [AnnonceStatut.Vendue]: t('annonce.status.vendue'),
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

  const confirmed = confirm(t('annonce.delete.confirm', { title: props.annonce.titre }))

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
    alert(t('annonce.delete.error'))
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

    // Emit updated event with updated annonce
    const updatedAnnonce: AnnonceList = {
      ...props.annonce,
      statut: newStatut,
    }
    emit('updated', updatedAnnonce)
  } catch (error) {
    console.error('Failed to update annonce status:', error)
    alert(t('errors.generic'))
  }
}

// formatDate removed - using formatSmartDate from i18n composable
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
