<template>
  <AppLayout>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Annonces</h1>
      </div>

      <!-- Filters -->
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-8">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <!-- Search -->
          <div>
            <label for="search" class="block text-sm font-medium text-gray-700 mb-1">
              Recherche
            </label>
            <input
              id="search"
              v-model="filters.search"
              type="text"
              placeholder="Titre ou description..."
              class="form-input"
              @input="debouncedSearch"
            />
          </div>

          <!-- Type -->
          <div>
            <label for="type" class="block text-sm font-medium text-gray-700 mb-1"> Type </label>
            <select id="type" v-model="filters.type" class="form-input" @change="debouncedSearch">
              <option value="">Tous</option>
              <option :value="AnnonceType.Vente">Vente</option>
              <option :value="AnnonceType.Location">Location</option>
            </select>
          </div>

          <!-- Nature -->
          <div>
            <label for="nature" class="block text-sm font-medium text-gray-700 mb-1">
              Nature
            </label>
            <select
              id="nature"
              v-model="filters.nature"
              class="form-input"
              @change="debouncedSearch"
            >
              <option value="">Toutes</option>
              <option :value="AnnonceNature.Offre">Offre</option>
              <option :value="AnnonceNature.Demande">Demande</option>
            </select>
          </div>

          <!-- Sort -->
          <div>
            <label for="sort" class="block text-sm font-medium text-gray-700 mb-1">
              Trier par
            </label>
            <select id="sort" v-model="sortOption" class="form-input" @change="debouncedSearch">
              <option value="date_creation_desc">Plus récent</option>
              <option value="date_creation_asc">Plus ancien</option>
              <option value="prix_asc">Prix croissant</option>
              <option value="prix_desc">Prix décroissant</option>
              <option value="titre_asc">Titre A-Z</option>
              <option value="titre_desc">Titre Z-A</option>
            </select>
          </div>
        </div>

        <!-- Price Range -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
          <div>
            <label for="prixMin" class="block text-sm font-medium text-gray-700 mb-1">
              Prix minimum
            </label>
            <input
              id="prixMin"
              v-model="filters.prixMin"
              type="number"
              step="0.01"
              min="0"
              placeholder="0"
              class="form-input"
              @input="debouncedSearch"
            />
          </div>
          <div>
            <label for="prixMax" class="block text-sm font-medium text-gray-700 mb-1">
              Prix maximum
            </label>
            <input
              id="prixMax"
              v-model="filters.prixMax"
              type="number"
              step="0.01"
              min="0"
              placeholder="Aucune limite"
              class="form-input"
              @input="debouncedSearch"
            />
          </div>
        </div>

        <!-- Location Filters -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
          <LocationField v-model="selectedLocation" @change="handleLocationChange" />

          <div>
            <label for="distance" class="block text-sm font-medium text-gray-700 mb-1">
              Distance max: {{ distanceLabel }}
            </label>
            <div class="space-y-2">
              <input
                id="distance-slider"
                v-model="distanceSlider"
                type="range"
                min="1"
                max="100"
                step="1"
                :disabled="!hasCoordinates"
                :class="[
                  'w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer slider',
                  !hasCoordinates && 'opacity-50 cursor-not-allowed',
                ]"
                @input="handleDistanceSliderChange"
              />
              <input
                id="distance-input"
                v-model="distanceInput"
                type="number"
                min="1"
                max="100"
                step="1"
                placeholder="Illimitée"
                :disabled="!hasCoordinates"
                :class="[
                  'form-input w-full text-center',
                  !hasCoordinates && 'opacity-50 cursor-not-allowed',
                ]"
                @input="handleDistanceInputChange"
              />
              <p v-if="!hasCoordinates" class="text-xs text-gray-500 text-center">
                Sélectionnez une localisation pour activer le filtre distance
              </p>
            </div>
          </div>
        </div>

        <!-- Active Filters -->
        <div v-if="hasActiveFilters" class="flex flex-wrap gap-2 mt-4">
          <span class="text-sm text-gray-500">Filtres actifs:</span>
          <button
            v-if="filters.search"
            @click="clearFilter('search')"
            class="inline-flex items-center px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded-full"
          >
            Recherche: "{{ filters.search }}"
            <XMarkIcon class="h-3 w-3 ml-1" />
          </button>
          <button
            v-if="filters.type"
            @click="clearFilter('type')"
            class="inline-flex items-center px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded-full"
          >
            Type: {{ getTypeLabel(filters.type) }}
            <XMarkIcon class="h-3 w-3 ml-1" />
          </button>
          <button
            v-if="filters.nature"
            @click="clearFilter('nature')"
            class="inline-flex items-center px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded-full"
          >
            Nature: {{ getNatureLabel(filters.nature) }}
            <XMarkIcon class="h-3 w-3 ml-1" />
          </button>
          <button
            @click="clearAllFilters"
            class="text-xs text-blue-600 hover:text-blue-800 underline"
          >
            Effacer tous les filtres
          </button>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <div v-for="i in 9" :key="i" class="card animate-pulse">
          <div class="h-48 bg-gray-200"></div>
          <div class="p-4 space-y-3">
            <div class="h-4 bg-gray-200 rounded"></div>
            <div class="h-4 bg-gray-200 rounded w-2/3"></div>
            <div class="h-4 bg-gray-200 rounded w-1/3"></div>
          </div>
        </div>
      </div>

      <!-- Results -->
      <div v-else>
        <!-- Results count -->
        <div class="flex justify-between items-center mb-6">
          <p class="text-gray-600">{{ pagination?.total_elements || 0 }} annonce(s) trouvée(s)</p>
        </div>

        <!-- Annonces Grid -->
        <div
          v-if="annonces.length > 0"
          class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3"
        >
          <AnnonceCard
            v-for="annonce in annonces"
            :key="annonce.id"
            :annonce="annonce"
            :show-modification-date="true"
          />
        </div>

        <!-- Empty State -->
        <div v-else class="text-center py-12">
          <div class="text-gray-400 mb-4">
            <MagnifyingGlassIcon class="h-16 w-16 mx-auto" />
          </div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">Aucune annonce trouvée</h3>
          <p class="text-gray-600 mb-6">Essayez de modifier vos critères de recherche</p>
          <button @click="clearAllFilters" class="btn-primary">Effacer les filtres</button>
        </div>

        <!-- Pagination -->
        <div v-if="pagination && pagination.total_pages > 1" class="mt-12">
          <nav class="flex justify-center">
            <div class="flex space-x-2">
              <!-- Previous -->
              <button
                :disabled="pagination.page_courante <= 1"
                @click="changePage(pagination.page_courante - 1)"
                class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Précédent
              </button>

              <!-- Page numbers -->
              <button
                v-for="page in getVisiblePages()"
                :key="page"
                :class="[
                  'px-3 py-2 text-sm font-medium rounded-md',
                  page === pagination.page_courante
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-500 bg-white border border-gray-300 hover:bg-gray-50',
                ]"
                @click="changePage(typeof page === 'number' ? page : 1)"
              >
                {{ page }}
              </button>

              <!-- Next -->
              <button
                :disabled="pagination.page_courante >= pagination.total_pages"
                @click="changePage(pagination.page_courante + 1)"
                class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Suivant
              </button>
            </div>
          </nav>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useDebounceFn } from '@vueuse/core'
import { useAuthStore } from '../stores/auth'
import { annoncesApi } from '../services/api'
import {
  AnnonceList,
  AnnonceType,
  AnnonceNature,
  Pagination,
  ListAnnoncesSortByEnum,
  ListAnnoncesSortOrderEnum,
} from '../api'
import AppLayout from '../components/AppLayout.vue'
import AnnonceCard from '../components/AnnonceCard.vue'
import LocationField from '../components/LocationField.vue'
import { PlusIcon, XMarkIcon, MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import { SelectedLocation } from '@/types/location'

const route = useRoute()
const authStore = useAuthStore()

const annonces = ref<AnnonceList[]>([])
const pagination = ref<Pagination>()
const loading = ref(false)
const sortOption = ref('date_creation_desc')
const distanceSlider = ref(50)
const distanceInput = ref('')
const selectedLocation = ref<SelectedLocation | null>(null)

const filters = ref({
  search: '',
  type: '',
  nature: '',
  prixMin: '',
  prixMax: '',
  distanceMax: '50',
  latitude: null as number | null,
  longitude: null as number | null,
})

const distanceLabel = computed(() => {
  const distance = distanceInput.value || distanceSlider.value
  return distance ? `${distance} km` : 'Illimitée'
})

const hasActiveFilters = computed(() => {
  return (
    filters.value.search ||
    filters.value.type ||
    filters.value.nature ||
    filters.value.prixMin ||
    filters.value.prixMax ||
    filters.value.distanceMax
  )
})

const hasCoordinates = computed(() => {
  return filters.value.latitude !== null && filters.value.longitude !== null
})

const fetchAnnonces = async (page = 1) => {
  loading.value = true
  try {
    const [sortBy, sortOrder] = sortOption.value.split('_')
    const realSortBy = sortBy === 'date' ? `${sortBy}_creation` : sortBy

    const typeParam = (filters.value.type as AnnonceType | undefined) || undefined
    const natureParam = filters.value.nature || undefined

    const response = await annoncesApi.listAnnonces(
      typeParam,
      undefined, // statut
      natureParam as AnnonceNature | undefined,
      page,
      12, // limit
      filters.value.search || undefined,
      undefined, // userId
      filters.value.prixMin ? parseFloat(filters.value.prixMin) : undefined,
      filters.value.prixMax ? parseFloat(filters.value.prixMax) : undefined,
      filters.value.latitude || undefined,
      filters.value.longitude || undefined,
      filters.value.distanceMax ? Math.round(parseFloat(filters.value.distanceMax)) : undefined,
      realSortBy as ListAnnoncesSortByEnum,
      sortOrder === 'desc' ? ListAnnoncesSortOrderEnum.Desc : ListAnnoncesSortOrderEnum.Asc,
    )

    annonces.value = response.data.data || []
    pagination.value = response.data.pagination
  } catch (error) {
    console.error('Failed to fetch annonces:', error)
  } finally {
    loading.value = false
  }
}

const debouncedSearch = useDebounceFn(() => {
  fetchAnnonces()
}, 500)

const handleDistanceSliderChange = () => {
  distanceInput.value = distanceSlider.value.toString()
  filters.value.distanceMax = distanceSlider.value.toString()
  debouncedSearch()
}

const handleDistanceInputChange = () => {
  const value = parseInt(distanceInput.value)
  if (!isNaN(value) && value >= 1 && value <= 100) {
    distanceSlider.value = value
    filters.value.distanceMax = value.toString()
    debouncedSearch()
  } else if (distanceInput.value === '') {
    filters.value.distanceMax = ''
    debouncedSearch()
  }
}

const changePage = (page: number) => {
  fetchAnnonces(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const clearFilter = (filterKey: keyof typeof filters.value) => {
  ;(filters.value[filterKey] as string) = ''
  fetchAnnonces()
}

const clearAllFilters = () => {
  filters.value = {
    search: '',
    type: '',
    nature: '',
    prixMin: '',
    prixMax: '',
    distanceMax: '',
    latitude: null,
    longitude: null,
  }
  selectedLocation.value = null
  distanceSlider.value = 50
  distanceInput.value = ''
  sortOption.value = 'date_creation_desc'
  fetchAnnonces()
}

const handleLocationChange = (location: SelectedLocation | null) => {
  if (location) {
    filters.value.longitude = location.coordinates[0]
    filters.value.latitude = location.coordinates[1]
  } else {
    filters.value.longitude = null
    filters.value.latitude = null
    filters.value.distanceMax = ''
    distanceSlider.value = 50
    distanceInput.value = ''
  }
  debouncedSearch()
}

const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = { vente: 'Vente', location: 'Location' }
  return labels[type] || type
}

const getNatureLabel = (nature: string) => {
  const labels: Record<string, string> = { offre: 'Offre', demande: 'Demande' }
  return labels[nature] || nature
}

const getVisiblePages = () => {
  if (!pagination.value) return []

  const current = pagination.value.page_courante
  const total = pagination.value.total_pages
  const delta = 2

  const range = []
  const rangeWithDots = []

  for (let i = Math.max(2, current - delta); i <= Math.min(total - 1, current + delta); i++) {
    range.push(i)
  }

  if (current - delta > 2) {
    rangeWithDots.push(1, '...')
  } else {
    rangeWithDots.push(1)
  }

  rangeWithDots.push(...range)

  if (current + delta < total - 1) {
    rangeWithDots.push('...', total)
  } else if (total > 1) {
    rangeWithDots.push(total)
  }

  return rangeWithDots.filter((page, index, arr) => arr.indexOf(page) === index)
}

onMounted(() => {
  // Initialize filters from URL query params
  const query = route.query
  if (query.search) filters.value.search = query.search as string
  if (query.type) filters.value.type = query.type as string
  if (query.nature) filters.value.nature = query.nature as string

  fetchAnnonces()
})

// Watch for route changes
watch(
  () => route.query,
  () => {
    if (route.name === 'annonces') {
      fetchAnnonces()
    }
  },
)
</script>

<style scoped>
.slider::-webkit-slider-thumb {
  appearance: none;
  height: 20px;
  width: 20px;
  border-radius: 50%;
  background: #3b82f6;
  cursor: pointer;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.slider::-moz-range-thumb {
  height: 20px;
  width: 20px;
  border-radius: 50%;
  background: #3b82f6;
  cursor: pointer;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.slider::-webkit-slider-track {
  height: 8px;
  border-radius: 4px;
  background: #e5e7eb;
}

.slider::-moz-range-track {
  height: 8px;
  border-radius: 4px;
  background: #e5e7eb;
}
</style>
