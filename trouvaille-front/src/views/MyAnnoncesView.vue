<template>
  <AppLayout>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Mes annonces</h1>
        <router-link to="/annonces/create" class="btn-primary">
          <PlusIcon class="h-4 w-4 mr-2" />
          Créer une annonce
        </router-link>
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
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            ]"
          >
            {{ status.label }}
            <span
              v-if="status.count !== undefined"
              :class="[
                'ml-2 py-0.5 px-2 rounded-full text-xs',
                selectedStatus === status.value
                  ? 'bg-blue-100 text-blue-600'
                  : 'bg-gray-100 text-gray-600'
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
            <div class="h-4 bg-gray-200 rounded"></div>
            <div class="h-4 bg-gray-200 rounded w-2/3"></div>
            <div class="h-4 bg-gray-200 rounded w-1/3"></div>
          </div>
        </div>
      </div>

      <!-- Annonces Grid -->
      <div v-else-if="filteredAnnonces.length > 0" class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <div
          v-for="annonce in filteredAnnonces"
          :key="annonce.id"
          class="card hover:shadow-lg transition-shadow"
        >
          <!-- Image -->
          <div class="aspect-w-16 aspect-h-9 bg-gray-200 relative">
            <img
              v-if="annonce.photos && annonce.photos.length > 0"
              :src="getPhotoUrl(annonce.photos[0])"
              :alt="annonce.titre"
              class="w-full h-48 object-cover cursor-pointer"
              @click="goToDetail(annonce.id!)"
              @error="onImageError"
            />
            <div v-else class="w-full h-48 flex items-center justify-center bg-gray-100 cursor-pointer" @click="goToDetail(annonce.id!)">
              <PhotoIcon class="h-12 w-12 text-gray-400" />
            </div>

            <!-- Status Badge -->
            <div class="absolute top-2 left-2">
              <span :class="getStatutBadgeClass(annonce.statut)">
                {{ getStatutLabel(annonce.statut) }}
              </span>
            </div>

            <!-- Quick Actions -->
            <div class="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity">
              <div class="flex space-x-1">
                <button
                  @click="editAnnonce(annonce.id!)"
                  class="p-2 bg-white bg-opacity-90 rounded-full text-gray-600 hover:text-blue-600 transition-colors"
                  title="Modifier"
                >
                  <PencilIcon class="h-4 w-4" />
                </button>
                <button
                  @click="confirmDelete(annonce)"
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
                class="text-lg font-medium text-gray-900 line-clamp-2 flex-1 cursor-pointer hover:text-blue-600"
                @click="goToDetail(annonce.id!)"
              >
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
            <div class="flex items-center justify-between text-xs text-gray-500 mb-3">
              <div class="flex items-center space-x-2">
                <span class="badge bg-blue-100 text-blue-800">
                  {{ getTypeLabel(annonce.type) }}
                </span>
                <span class="badge bg-green-100 text-green-800">
                  {{ getNatureLabel(annonce.nature) }}
                </span>
              </div>
            </div>

            <!-- Date and Actions -->
            <div class="flex items-center justify-between">
              <div class="text-xs text-gray-400">
                {{ formatDate(annonce.dateCreation) }}
                <span v-if="annonce.dateModification && annonce.dateModification !== annonce.dateCreation">
                  • Modifié {{ formatDate(annonce.dateModification) }}
                </span>
              </div>

              <!-- Status Actions -->
              <div class="flex space-x-1">
                <button
                  v-if="annonce.statut === AnnonceStatut.Active"
                  @click="changeStatut(annonce, AnnonceStatut.Suspendue)"
                  class="text-xs text-yellow-600 hover:text-yellow-700 underline"
                  title="Suspendre"
                >
                  Suspendre
                </button>
                <button
                  v-if="annonce.statut === AnnonceStatut.Suspendue"
                  @click="changeStatut(annonce, AnnonceStatut.Active)"
                  class="text-xs text-green-600 hover:text-green-700 underline"
                  title="Réactiver"
                >
                  Réactiver
                </button>
                <button
                  v-if="annonce.statut === AnnonceStatut.Active && annonce.type === AnnonceType.Vente"
                  @click="changeStatut(annonce, AnnonceStatut.Vendue)"
                  class="text-xs text-gray-600 hover:text-gray-700 underline"
                  title="Marquer comme vendu"
                >
                  Vendu
                </button>
              </div>
            </div>
          </div>
        </div>
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
      <div v-if="pagination && pagination.totalPages > 1" class="mt-12">
        <nav class="flex justify-center">
          <div class="flex space-x-2">
            <button
              :disabled="pagination.pageCourante <= 1"
              @click="changePage(pagination.pageCourante - 1)"
              class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Précédent
            </button>

            <button
              v-for="page in getVisiblePages()"
              :key="page"
              :class="[
                'px-3 py-2 text-sm font-medium rounded-md',
                page === pagination.pageCourante
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-500 bg-white border border-gray-300 hover:bg-gray-50'
              ]"
              @click="changePage(page)"
            >
              {{ page }}
            </button>

            <button
              :disabled="pagination.pageCourante >= pagination.totalPages"
              @click="changePage(pagination.pageCourante + 1)"
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { annoncesApi } from '../services/api'
import { AnnonceList, AnnonceType, AnnonceNature, AnnonceStatut, PeriodeLocation, Pagination } from '../api'
import AppLayout from '../components/AppLayout.vue'
import {
  PlusIcon,
  PhotoIcon,
  PencilIcon,
  TrashIcon,
  DocumentTextIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
const authStore = useAuthStore()

const annonces = ref<AnnonceList[]>([])
const pagination = ref<Pagination>()
const loading = ref(false)
const selectedStatus = ref('')

const statusTabs = computed(() => [
  { label: 'Toutes', value: '', count: annonces.value.length },
  { 
    label: 'Actives', 
    value: AnnonceStatut.Active, 
    count: annonces.value.filter(a => a.statut === AnnonceStatut.Active).length 
  },
  { 
    label: 'Suspendues', 
    value: AnnonceStatut.Suspendue, 
    count: annonces.value.filter(a => a.statut === AnnonceStatut.Suspendue).length 
  },
  { 
    label: 'Vendues', 
    value: AnnonceStatut.Vendue, 
    count: annonces.value.filter(a => a.statut === AnnonceStatut.Vendue).length 
  }
])

const filteredAnnonces = computed(() => {
  if (!selectedStatus.value) return annonces.value
  return annonces.value.filter(annonce => annonce.statut === selectedStatus.value)
})

const fetchMyAnnonces = async (page = 1) => {
  if (!authStore.user?.pseudo) return

  loading.value = true
  try {
    const response = await annoncesApi.listAnnonces({
      page,
      limit: 12,
      userPseudo: authStore.user.pseudo,
      statut: selectedStatus.value as AnnonceStatut || undefined,
      sortBy: 'date_creation',
      sortOrder: 'desc'
    })

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

const goToDetail = (id: string) => {
  router.push(`/annonces/${id}`)
}

const editAnnonce = (id: string) => {
  router.push(`/annonces/${id}/edit`)
}

const changeStatut = async (annonce: AnnonceList, newStatut: AnnonceStatut) => {
  if (!annonce.id) return

  try {
    await annoncesApi.putAnnonce(annonce.id, {
      statut: newStatut
    })
    
    // Update local state
    const index = annonces.value.findIndex(a => a.id === annonce.id)
    if (index > -1) {
      annonces.value[index].statut = newStatut
    }
  } catch (error) {
    console.error('Failed to update annonce status:', error)
    alert('Erreur lors de la mise à jour du statut')
  }
}

const confirmDelete = (annonce: AnnonceList) => {
  if (confirm(`Êtes-vous sûr de vouloir supprimer "${annonce.titre}" ? Cette action est irréversible.`)) {
    deleteAnnonce(annonce.id!)
  }
}

const deleteAnnonce = async (id: string) => {
  try {
    await annoncesApi.deleteAnnonce(id)
    
    // Remove from local state
    const index = annonces.value.findIndex(a => a.id === id)
    if (index > -1) {
      annonces.value.splice(index, 1)
    }
  } catch (error) {
    console.error('Failed to delete annonce:', error)
    alert('Erreur lors de la suppression de l\'annonce')
  }
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

const getStatutLabel = (statut?: AnnonceStatut) => {
  const labels = {
    [AnnonceStatut.Active]: 'Active',
    [AnnonceStatut.Suspendue]: 'Suspendue',
    [AnnonceStatut.Vendue]: 'Vendue'
  }
  return statut ? labels[statut] : 'N/A'
}

const getStatutBadgeClass = (statut?: AnnonceStatut) => {
  const classes = {
    [AnnonceStatut.Active]: 'badge badge-active',
    [AnnonceStatut.Suspendue]: 'badge badge-suspended',
    [AnnonceStatut.Vendue]: 'badge badge-sold'
  }
  return statut ? classes[statut] : 'badge'
}

const formatDate = (dateString?: string) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diffTime = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays === 0) return 'aujourd\'hui'
  if (diffDays === 1) return 'hier'
  if (diffDays < 7) return `il y a ${diffDays} jours`
  
  return date.toLocaleDateString('fr-FR')
}

const getEmptyStateTitle = () => {
  const titles = {
    '': 'Aucune annonce',
    [AnnonceStatut.Active]: 'Aucune annonce active',
    [AnnonceStatut.Suspendue]: 'Aucune annonce suspendue',
    [AnnonceStatut.Vendue]: 'Aucune annonce vendue'
  }
  return (titles as any)[selectedStatus.value] || 'Aucune annonce'
}

const getEmptyStateMessage = () => {
  const messages = {
    '': 'Vous n\'avez pas encore créé d\'annonce. Commencez dès maintenant !',
    [AnnonceStatut.Active]: 'Vous n\'avez pas d\'annonce active actuellement.',
    [AnnonceStatut.Suspendue]: 'Vous n\'avez pas d\'annonce suspendue.',
    [AnnonceStatut.Vendue]: 'Vous n\'avez pas encore vendu d\'objets.'
  }
  return (messages as any)[selectedStatus.value] || 'Aucune annonce trouvée.'
}

const getVisiblePages = () => {
  if (!pagination.value) return []
  
  const current = pagination.value.pageCourante
  const total = pagination.value.totalPages
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
  fetchMyAnnonces()
})

// Watch status changes
watch(selectedStatus, () => {
  fetchMyAnnonces()
})
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