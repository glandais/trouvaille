<template>
  <AppLayout>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900">{{ $t('nav.annonces') }}</h1>
      </div>

      <!-- Filters -->
      <div class="bg-white rounded-lg shadow-xs border border-gray-200 p-6 mb-8">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <!-- Search -->
          <div>
            <label for="search" class="block text-sm font-medium text-gray-700 mb-1">
              {{ $t('annonce.search.filters.search') }}
            </label>
            <input
              id="search"
              v-model="filters.search"
              type="text"
              :placeholder="$t('annonce.placeholders.search')"
              class="form-input"
              @input="debouncedSearch"
            />
          </div>

          <!-- Type -->
          <div>
            <label for="type" class="block text-sm font-medium text-gray-700 mb-1">{{
              $t('annonce.fields.type')
            }}</label>
            <select id="type" v-model="filters.type" class="form-input" @change="debouncedSearch">
              <option :value="undefined">Tous</option>
              <option :value="AnnonceType.Vente">{{ $t('annonce.types.vente') }}</option>
              <option :value="AnnonceType.Location">{{ $t('annonce.types.location') }}</option>
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
              <option :value="undefined">Toutes</option>
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
              <option value="date_creation!desc">Plus récent</option>
              <option value="date_creation!asc">Plus ancien</option>
              <option value="prix!asc">Prix croissant</option>
              <option value="prix!desc">Prix décroissant</option>
              <option value="titre!asc">Titre A-Z</option>
              <option value="titre!desc">Titre Z-A</option>
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
              v-model="filters.prix_min"
              type="number"
              min="0"
              placeholder="0"
              class="form-input no-spinners"
              @input="debouncedSearch"
            />
          </div>
          <div>
            <label for="prixMax" class="block text-sm font-medium text-gray-700 mb-1">
              Prix maximum
            </label>
            <input
              id="prixMax"
              v-model="filters.prix_max"
              type="number"
              min="0"
              placeholder="Aucune limite"
              class="form-input no-spinners"
              @input="debouncedSearch"
            />
          </div>
        </div>

        <!-- Location Filters -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
          <LocationField v-model="selectedLocation" @change="handleLocationChange" />

          <div class="relative">
            <label for="distance" class="block text-sm font-medium text-gray-700 mb-1">
              Distance max:
              <DistanceDisplay :distance="filters.distance_max" empty-value="Illimitée" />
            </label>
            <div class="space-y-2">
              <input
                id="distance-slider"
                v-model="filters.distance_max"
                type="range"
                min="1"
                max="100"
                step="1"
                :disabled="!hasCoordinates"
                :class="[
                  'w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer slider',
                  !hasCoordinates && 'opacity-50 cursor-not-allowed',
                ]"
                @input="debouncedSearch"
              />
              <div class="relative">
                <input
                  id="distance-input"
                  v-model="filters.distance_max"
                  type="number"
                  min="1"
                  max="100"
                  step="1"
                  placeholder="Illimitée"
                  :disabled="!hasCoordinates"
                  :class="[
                    'form-input w-full text-center no-spinners',
                    !hasCoordinates && 'opacity-50 cursor-not-allowed',
                    hasDistanceFilter && 'pr-8',
                  ]"
                  @input="debouncedSearch"
                />
                <button
                  v-if="hasDistanceFilter && hasCoordinates"
                  @click="clearDistanceFilter"
                  type="button"
                  class="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  title="Supprimer le filtre distance"
                >
                  <XMarkIcon class="h-4 w-4" />
                </button>
              </div>
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
            <div class="h-4 bg-gray-200 rounded-sm"></div>
            <div class="h-4 bg-gray-200 rounded-sm w-2/3"></div>
            <div class="h-4 bg-gray-200 rounded-sm w-1/3"></div>
          </div>
        </div>
      </div>

      <!-- Results -->
      <div v-else>
        <!-- Results count -->
        <div class="flex justify-between items-center mb-6">
          <p class="text-gray-600">
            {{ $t('annonce.search.results', pagination?.total_elements || 0) }}
          </p>
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
import { annoncesApi } from '../services/api'
import {
  AnnonceList,
  AnnonceType,
  AnnonceNature,
  Pagination,
  AnnonceSearch,
  AnnonceSearchSortBy,
  AnnonceSearchSortOrder,
} from '../api'
import DistanceDisplay from '../components/DistanceDisplay.vue'
import { useAnnonceLabels } from '@/composables/useAnnonceLabels'
import AppLayout from '../components/AppLayout.vue'
import AnnonceCard from '../components/AnnonceCard.vue'
import LocationField from '../components/LocationField.vue'
import { XMarkIcon, MagnifyingGlassIcon } from '@heroicons/vue/24/outline'
import { SelectedLocation } from '@/types/location'
import { useRouteParams } from '@/composables/useRouteParams'
import { RouterParam } from '@/types/routeParams'

const route = useRoute()
const routeParams = useRouteParams()
const { getTypeLabel, getNatureLabel } = useAnnonceLabels()

const annonces = ref<AnnonceList[]>([])
const pagination = ref<Pagination>()
const loading = ref(false)
const sortOption = ref('date_creation!desc')
const currentPage = ref<number>(1)
const selectedLocation = ref<SelectedLocation | null>(null)
const updatingRoute = ref(false)

const filterInitial: AnnonceSearch = {
  search: undefined,
  type: undefined,
  nature: undefined,
  prix_min: undefined,
  prix_max: undefined,
  distance_max: undefined,
  latitude: undefined,
  longitude: undefined,
}

const filters = ref<AnnonceSearch>({
  ...filterInitial,
})

const getOrInitSelectedLocation = () => {
  if (selectedLocation.value) {
    return selectedLocation.value
  } else {
    selectedLocation.value = { label: '', city: '', coordinates: [0, 0] }
    return selectedLocation.value
  }
}

const routerParams = [
  new RouterParam<number>(
    'page',
    1,
    (s) => (currentPage.value = parseInt(s)),
    () => routeParams.asString(currentPage.value),
  ),
  new RouterParam<string | undefined>(
    'search',
    undefined,
    (s) => (filters.value.search = s),
    () => filters.value.search,
  ),
  new RouterParam<string | undefined>(
    'type',
    undefined,
    (s) => (filters.value.type = s as AnnonceType),
    () => filters.value.type,
  ),
  new RouterParam<string | undefined>(
    'nature',
    undefined,
    (s) => (filters.value.nature = s as AnnonceNature),
    () => filters.value.nature,
  ),
  new RouterParam<string | undefined>(
    'prix_min',
    undefined,
    (s) => (filters.value.prix_min = parseInt(s)),
    () => routeParams.asString(filters.value.prix_min),
  ),
  new RouterParam<string | undefined>(
    'prix_max',
    undefined,
    (s) => (filters.value.prix_max = parseInt(s)),
    () => routeParams.asString(filters.value.prix_max),
  ),
  new RouterParam<string | undefined>(
    'location',
    undefined,
    (s) => {
      const sl = getOrInitSelectedLocation()
      sl.label = s
      sl.city = s
    },
    () => (selectedLocation.value ? selectedLocation.value.city : undefined),
  ),
  new RouterParam<string | undefined>(
    'longitude',
    undefined,
    (s) => {
      const sl = getOrInitSelectedLocation()
      sl.coordinates[0] = parseFloat(s)
      filters.value.longitude = parseFloat(s)
    },
    () =>
      selectedLocation.value
        ? routeParams.asString(selectedLocation.value.coordinates[0])
        : undefined,
  ),
  new RouterParam<string | undefined>(
    'latitude',
    undefined,
    (s) => {
      const sl = getOrInitSelectedLocation()
      sl.coordinates[1] = parseFloat(s)
      filters.value.latitude = parseFloat(s)
    },
    () =>
      selectedLocation.value
        ? routeParams.asString(selectedLocation.value.coordinates[1])
        : undefined,
  ),
  new RouterParam<string | undefined>(
    'distance_max',
    undefined,
    (s) => (filters.value.distance_max = parseInt(s)),
    () => (selectedLocation.value ? routeParams.asString(filters.value.distance_max) : undefined),
  ),
  new RouterParam<string | undefined>(
    'sortOption',
    undefined,
    (s) => (sortOption.value = s),
    () => sortOption.value,
  ),
]

const initFromQuery = () => {
  if (!updatingRoute.value) {
    filters.value = {
      ...filterInitial,
    }
    selectedLocation.value = null
    sortOption.value = 'date_creation!desc'
    routeParams.initFromRouterParams(routerParams)
    fetchAnnonces()
  }
}

const hasActiveFilters = computed(() => {
  return (
    filters.value.search ||
    filters.value.type ||
    filters.value.nature ||
    filters.value.prix_min ||
    filters.value.prix_max ||
    filters.value.distance_max
  )
})

const hasDistanceFilter = computed(() => {
  return filters.value.distance_max && hasCoordinates.value
})

const hasCoordinates = computed(() => {
  return filters.value.latitude !== undefined && filters.value.longitude !== undefined
})

const fetchAnnonces = async () => {
  loading.value = true
  try {
    const [sortBy, sortOrder] = sortOption.value.split('!')

    const annonceSearch: AnnonceSearch = {
      ...filters.value,
      statut: undefined, // statut
      user_id: undefined,
      page: currentPage.value,
      limit: 12, // limit
      sort_by: sortBy as AnnonceSearchSortBy,
      sort_order: sortOrder === 'desc' ? AnnonceSearchSortOrder.Desc : AnnonceSearchSortOrder.Asc,
    }

    const response = await annoncesApi.listAnnonces(annonceSearch)
    await routeParams.applyRouterParams('annonces', routerParams, updatingRoute)

    annonces.value = response.data.data || []
    pagination.value = response.data.pagination
  } catch (error) {
    console.error('Failed to fetch annonces:', error)
  } finally {
    loading.value = false
  }
}

const debouncedSearch = useDebounceFn(() => {
  currentPage.value = 1
  fetchAnnonces()
}, 500)

const changePage = (page: number) => {
  currentPage.value = page
  fetchAnnonces()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const clearFilter = (filterKey: keyof typeof filters.value) => {
  ;(filters.value[filterKey] as string) = ''
  currentPage.value = 1
  fetchAnnonces()
}

const clearAllFilters = () => {
  filters.value = {
    ...filterInitial,
  }
  selectedLocation.value = null
  sortOption.value = 'date_creation!desc'
  currentPage.value = 1
  fetchAnnonces()
}

const clearDistanceFilter = () => {
  filters.value.distance_max = undefined
  debouncedSearch()
}

const handleLocationChange = (location: SelectedLocation | null) => {
  if (location) {
    filters.value.longitude = location.coordinates[0]
    filters.value.latitude = location.coordinates[1]
    filters.value.distance_max = 50
  } else {
    filters.value.longitude = undefined
    filters.value.latitude = undefined
    filters.value.distance_max = undefined
  }
  debouncedSearch()
}

// getTypeLabel and getNatureLabel now imported from useAnnonceLabels composable

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
  initFromQuery()
})

// Watch for route changes
watch(
  () => route.query,
  () => {
    if (route.name === 'annonces') {
      initFromQuery()
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

/* Hide number input spinners */
.no-spinners::-webkit-outer-spin-button,
.no-spinners::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.no-spinners[type='number'] {
  -moz-appearance: textfield;
}
</style>
