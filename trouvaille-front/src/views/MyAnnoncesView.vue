<template>
  <AppLayout>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Mes annonces</h1>
      </div>

      <!-- Status Filter Tabs -->
      <div class="border-b border-gray-200 mb-8">
        <nav class="-mb-px flex space-x-8">
          <button
            v-for="status in statusTabs"
            :key="status.value"
            @click="selectedStatus = status.value"
            :class="[
              'whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors',
              selectedStatus === status.value
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
            ]"
          >
            {{ status.label }}
            <span
              v-if="status.count !== undefined"
              :class="[
                'ml-2 py-0.5 px-2 rounded-full text-xs',
                selectedStatus === status.value
                  ? 'bg-blue-100 text-blue-600'
                  : 'bg-gray-100 text-gray-600',
              ]"
            >
              {{ status.count }}
            </span>
          </button>
        </nav>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <div v-for="i in 6" :key="i" class="card animate-pulse">
          <div class="h-48 bg-gray-200"></div>
          <div class="p-4 space-y-3">
            <div class="h-4 bg-gray-200 rounded-sm"></div>
            <div class="h-4 bg-gray-200 rounded-sm w-2/3"></div>
            <div class="h-4 bg-gray-200 rounded-sm w-1/3"></div>
          </div>
        </div>
      </div>

      <!-- Annonces Grid -->
      <div
        v-else-if="annonces.length > 0"
        class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3"
      >
        <AnnonceCard
          v-for="annonce in annonces"
          :key="annonce.id"
          :annonce="annonce"
          :show-status="true"
          @updated="handleAnnonceUpdated"
          @deleted="handleAnnonceDeleted"
        />
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-12">
        <div class="text-gray-400 mb-4">
          <DocumentTextIcon class="h-16 w-16 mx-auto" />
        </div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">
          {{ getEmptyStateTitle() }}
        </h3>
        <p class="text-gray-600 mb-6">
          {{ getEmptyStateMessage() }}
        </p>
        <router-link v-if="selectedStatus === ''" to="/annonces/create" class="btn-primary">
          Créer ma première annonce
        </router-link>
      </div>

      <!-- Pagination -->
      <div v-if="pagination && pagination.total_pages > 1" class="mt-12">
        <nav class="flex justify-center">
          <div class="flex space-x-2">
            <button
              :disabled="pagination.page_courante <= 1"
              @click="changePage(pagination.page_courante - 1)"
              class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Précédent
            </button>

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
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '../stores/auth'
import { annoncesApi } from '../services/api'
import {
  AnnonceList,
  AnnonceStatut,
  Pagination,
  AnnonceSearch,
  AnnonceSearchSortBy,
  AnnonceSearchSortOrder,
} from '../api'
import AppLayout from '../components/AppLayout.vue'
import AnnonceCard from '../components/AnnonceCard.vue'
import { DocumentTextIcon } from '@heroicons/vue/24/outline'

const authStore = useAuthStore()

const annonces = ref<AnnonceList[]>([])
const pagination = ref<Pagination>()
const loading = ref(false)
const selectedStatus = ref('')
const statusCounts = ref<Record<string, number>>({
  '': 0,
  [AnnonceStatut.Active]: 0,
  [AnnonceStatut.Suspendue]: 0,
  [AnnonceStatut.Vendue]: 0,
})

const statusTabs = computed(() => [
  { label: 'Toutes', value: '', count: statusCounts.value[''] },
  {
    label: 'Actives',
    value: AnnonceStatut.Active,
    count: statusCounts.value[AnnonceStatut.Active],
  },
  {
    label: 'Suspendues',
    value: AnnonceStatut.Suspendue,
    count: statusCounts.value[AnnonceStatut.Suspendue],
  },
  {
    label: 'Vendues',
    value: AnnonceStatut.Vendue,
    count: statusCounts.value[AnnonceStatut.Vendue],
  },
])

const fetchStatusCounts = async () => {
  if (!authStore.user?.id) return

  try {
    // Count all annonces
    const allCount = await annoncesApi.countAnnonces({
      user_id: authStore.user.id,
    })
    statusCounts.value[''] = allCount.data

    // Count by status
    for (const status of [AnnonceStatut.Active, AnnonceStatut.Suspendue, AnnonceStatut.Vendue]) {
      const count = await annoncesApi.countAnnonces({
        user_id: authStore.user.id,
        statut: status,
      })
      statusCounts.value[status] = count.data
    }
  } catch (error) {
    console.error('Failed to fetch status counts:', error)
  }
}

const fetchMyAnnonces = async (page = 1) => {
  if (!authStore.user?.id) return

  loading.value = true

  const annonceSearch: AnnonceSearch = {
    type: undefined,
    statut: (selectedStatus.value as AnnonceStatut) || undefined,
    nature: undefined,
    page: page,
    limit: 12,
    search: undefined,
    user_id: authStore.user.id,
    prix_min: undefined,
    prix_max: undefined,
    latitude: undefined,
    longitude: undefined,
    distance_max: undefined,
    sort_by: AnnonceSearchSortBy.DateCreation,
    sort_order: AnnonceSearchSortOrder.Desc,
  }

  try {
    const response = await annoncesApi.listAnnonces(annonceSearch)
    annonces.value = response.data.data || []
    pagination.value = response.data.pagination
  } catch (error) {
    console.error('Failed to fetch my annonces:', error)
  } finally {
    loading.value = false
  }
}

const changePage = (page: number) => {
  fetchMyAnnonces(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleAnnonceDeleted = (annonceId: string) => {
  // Remove from local state
  const index = annonces.value.findIndex((a) => a.id === annonceId)
  if (index > -1) {
    annonces.value.splice(index, 1)
  }
  // Refresh counts
  fetchStatusCounts()
}

const handleAnnonceUpdated = (updatedAnnonce?: AnnonceList) => {
  if (updatedAnnonce) {
    // Update the annonce in local state
    const index = annonces.value.findIndex((a) => a.id === updatedAnnonce.id)
    if (index > -1) {
      annonces.value[index] = { ...annonces.value[index], ...updatedAnnonce }
    }
  }
  // Refresh counts and data
  fetchStatusCounts()
  fetchMyAnnonces()
}

const getEmptyStateTitle = () => {
  const titles: Record<string, string> = {
    '': 'Aucune annonce',
    [AnnonceStatut.Active]: 'Aucune annonce active',
    [AnnonceStatut.Suspendue]: 'Aucune annonce suspendue',
    [AnnonceStatut.Vendue]: 'Aucune annonce vendue',
  }
  return titles[selectedStatus.value] || 'Aucune annonce'
}

const getEmptyStateMessage = () => {
  const messages: Record<string, string> = {
    '': "Vous n'avez pas encore créé d'annonce. Commencez dès maintenant !",
    [AnnonceStatut.Active]: "Vous n'avez pas d'annonce active actuellement.",
    [AnnonceStatut.Suspendue]: "Vous n'avez pas d'annonce suspendue.",
    [AnnonceStatut.Vendue]: "Vous n'avez pas encore vendu d'objets.",
  }
  return messages[selectedStatus.value] || 'Aucune annonce trouvée.'
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

onMounted(async () => {
  await fetchStatusCounts()
  await fetchMyAnnonces()
})

// Watch status changes
watch(selectedStatus, () => {
  fetchMyAnnonces(1) // Reset to page 1 when changing status
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
