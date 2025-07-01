<template>
  <AppLayout>
    <!-- Loading State -->
    <div v-if="loading" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="animate-pulse">
        <div class="h-8 bg-gray-200 rounded-sm w-1/4 mb-8"></div>
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div class="h-96 bg-gray-200 rounded-sm"></div>
          <div class="space-y-4">
            <div class="h-6 bg-gray-200 rounded-sm"></div>
            <div class="h-4 bg-gray-200 rounded-sm w-3/4"></div>
            <div class="h-20 bg-gray-200 rounded-sm"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="text-center py-12">
        <h2 class="text-2xl font-semibold text-gray-900 mb-2">
          {{ $t('annonce.not_found.title') }}
        </h2>
        <p class="text-gray-600 mb-6">
          {{ $t('annonce.not_found.description') }}
        </p>
        <router-link to="/annonces" class="btn-primary">{{
          $t('annonce.not_found.action')
        }}</router-link>
      </div>
    </div>

    <!-- Annonce Content -->
    <div v-else-if="annonce" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Breadcrumb -->
      <nav class="flex mb-8" aria-label="Breadcrumb">
        <ol class="flex items-center space-x-4">
          <li>
            <router-link to="/" class="text-gray-400 hover:text-gray-500">{{
              $t('nav.home')
            }}</router-link>
          </li>
          <li>
            <ChevronRightIcon class="h-4 w-4 text-gray-400" />
          </li>
          <li>
            <router-link to="/annonces" class="text-gray-400 hover:text-gray-500">
              {{ $t('nav.annonces') }}
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
        <div class="space-y-4 gallery-container">
          <Gallery :list="photos" :options="galleryOptions" />
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
                  {{ getStatusLabel(annonce.statut) }}
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
            <h3 class="text-lg font-medium text-gray-900 mb-3">
              {{ $t('annonce.fields.description') }}
            </h3>
            <MarkdownViewer :model-value="annonce.description || ''" />
          </div>

          <!-- Location -->
          <div v-if="annonce.ville || annonce.coordinates">
            <h3 class="text-lg font-medium text-gray-900 mb-3">
              {{ $t('annonce.fields.location') }}
            </h3>
            <div class="flex items-center space-x-2 text-gray-600">
              <MapPinIcon class="h-5 w-5" />
              <span>{{ annonce.ville || $t('location.defined') }}</span>
              <DistanceDisplay
                :distance="(annonce as any).distance"
                :coordinates="annonce.coordinates"
                empty-value=""
              />
            </div>
          </div>

          <!-- User Info -->
          <div>
            <h3 class="text-lg font-medium text-gray-900 mb-3">
              {{ $t('annonce.fields.utilisateur') }}
            </h3>
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
                    $t('user.unknown')
                  }}
                </p>
                <p class="text-sm text-gray-500">
                  {{ $t('dates.published', { date: formatSmartDate(annonce.date_creation) }) }}
                  <span
                    v-if="
                      annonce.date_modification &&
                      annonce.date_modification !== annonce.date_creation
                    "
                  >
                    •
                    {{ $t('dates.modified', { date: formatSmartDate(annonce.date_modification) }) }}
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
                {{ $t('common.actions.edit') }}
              </router-link>
              <button
                @click="confirmDelete"
                class="w-full px-4 py-2 text-red-600 border border-red-300 rounded-lg hover:bg-red-50 transition-colors"
              >
                <TrashIcon class="h-4 w-4 mr-2 inline" />
                {{ $t('common.actions.delete') }}
              </button>
            </div>
            <div v-else class="space-y-3">
              <a
                :href="`https://chat.n-peloton.fr/np/messages/@${annonce.utilisateur?.username}`"
                target="_blank"
                rel="noopener noreferrer"
                class="btn-primary w-full text-center block"
              >
                <ChatBubbleLeftIcon class="h-4 w-4 mr-2 inline" />
                {{ $t('annonce.card.contact_seller') }}
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'
import { annoncesApi } from '../services/api'
import { Annonce, AnnonceStatut } from '../api'
import AppLayout from '../components/AppLayout.vue'
import MarkdownViewer from '../components/MarkdownViewer.vue'
import DistanceDisplay from '../components/DistanceDisplay.vue'
import {
  MapPinIcon,
  PencilIcon,
  TrashIcon,
  ChatBubbleLeftIcon,
  ChevronRightIcon,
} from '@heroicons/vue/24/outline'
import { useI18nFormatters } from '../composables/useI18nFormatters'
import { useAnnonceLabels } from '../composables/useAnnonceLabels'
import { Gallery } from 'vue-preview-imgs'

interface Props {
  id: string
}

const props = defineProps<Props>()
const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()
const { formatSmartDate, formatPrice } = useI18nFormatters()
const { getTypeLabel, getNatureLabel, getStatusLabel } = useAnnonceLabels()

const annonce = ref<Annonce>()
const loading = ref(true)
const error = ref(false)

const isOwner = computed(() => {
  return authStore.user?.id === annonce.value?.utilisateur?.id
})

const photos = computed(
  () =>
    annonce.value?.photos.map((photo) => ({
      href: photo.fullUrl,
      thumbnail: photo.thumbUrl,
      width: photo.width,
      height: photo.height,
      cropped: true,
    })) || [],
)

const galleryOptions = {}

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

// Using formatPrice from useI18nFormatters composable

const getStatutBadgeClass = (statut?: AnnonceStatut) => {
  const classes = {
    [AnnonceStatut.Active]: 'badge badge-active',
    [AnnonceStatut.Suspendue]: 'badge badge-suspended',
    [AnnonceStatut.Vendue]: 'badge badge-sold',
  }
  return statut ? classes[statut] : 'badge'
}

const confirmDelete = () => {
  if (confirm(t('annonce.delete.confirm', { title: annonce.value?.titre || '' }))) {
    deleteAnnonce()
  }
}

const deleteAnnonce = async () => {
  try {
    await annoncesApi.deleteAnnonce(props.id)
    router.push('/my-annonces')
  } catch (error) {
    console.error('Failed to delete annonce:', error)
    alert(t('annonce.delete.error'))
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
