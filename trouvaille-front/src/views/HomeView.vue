<template>
  <AppLayout>
    <!-- Hero Section -->
    <div class="bg-white">
      <div class="max-w-7xl mx-auto py-16 px-4 sm:py-24 sm:px-6 lg:px-8">
        <div class="text-center">
          <h1 class="text-4xl font-extrabold text-gray-900 sm:text-5xl lg:text-6xl">Trouvaille</h1>
          <p class="mt-4 text-xl text-gray-600 max-w-3xl mx-auto">
            La plateforme privée de petites annonces pour acheter, vendre, louer et échanger en
            toute confiance
          </p>
          <div class="mt-8 flex flex-col sm:flex-row gap-4 justify-center">
            <router-link to="/annonces" class="btn-primary text-lg px-8 py-3">
              Voir les annonces
            </router-link>
            <router-link
              v-if="authStore.isAuthenticated"
              to="/annonces/create"
              class="btn-secondary text-lg px-8 py-3"
            >
              Créer une annonce
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- Features Section -->
    <div class="py-16 bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center">
          <h2 class="text-3xl font-extrabold text-gray-900">Pourquoi choisir Trouvaille ?</h2>
        </div>

        <div class="mt-12 grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3">
          <!-- Feature 1 -->
          <div class="text-center">
            <div
              class="flex items-center justify-center h-12 w-12 rounded-md bg-blue-500 text-white mx-auto"
            >
              <ShieldCheckIcon class="h-6 w-6" />
            </div>
            <h3 class="mt-4 text-lg font-medium text-gray-900">Plateforme privée</h3>
            <p class="mt-2 text-gray-600">
              Environnement sécurisé et communauté fermée pour des échanges en toute confiance
            </p>
          </div>

          <!-- Feature 2 -->
          <div class="text-center">
            <div
              class="flex items-center justify-center h-12 w-12 rounded-md bg-blue-500 text-white mx-auto"
            >
              <MapPinIcon class="h-6 w-6" />
            </div>
            <h3 class="mt-4 text-lg font-medium text-gray-900">Géolocalisation</h3>
            <p class="mt-2 text-gray-600">
              Trouvez des annonces près de chez vous grâce à la recherche par distance
            </p>
          </div>

          <!-- Feature 3 -->
          <div class="text-center">
            <div
              class="flex items-center justify-center h-12 w-12 rounded-md bg-blue-500 text-white mx-auto"
            >
              <PhotoIcon class="h-6 w-6" />
            </div>
            <h3 class="mt-4 text-lg font-medium text-gray-900">Photos multiples</h3>
            <p class="mt-2 text-gray-600">
              Ajoutez jusqu'à 10 photos par annonce pour présenter vos objets sous tous les angles
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Annonces Preview -->
    <div class="py-16 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between">
          <h2 class="text-3xl font-extrabold text-gray-900">Annonces récentes</h2>
          <router-link to="/annonces" class="text-blue-600 hover:text-blue-700 font-medium">
            Voir toutes les annonces →
          </router-link>
        </div>

        <!-- Loading state -->
        <div v-if="loading" class="mt-8">
          <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            <div v-for="i in 6" :key="i" class="card animate-pulse">
              <div class="h-48 bg-gray-200"></div>
              <div class="p-4 space-y-3">
                <div class="h-4 bg-gray-200 rounded"></div>
                <div class="h-4 bg-gray-200 rounded w-2/3"></div>
                <div class="h-4 bg-gray-200 rounded w-1/3"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Annonces grid -->
        <div v-else-if="recentAnnonces.length > 0" class="mt-8">
          <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            <AnnonceCard v-for="annonce in recentAnnonces" :key="annonce.id" :annonce="annonce" />
          </div>
        </div>

        <!-- Empty state -->
        <div v-else class="mt-8 text-center py-12">
          <p class="text-gray-500">Aucune annonce pour le moment</p>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { annoncesApi } from '../services/api'
import { AnnonceList, ListAnnoncesSortByEnum, ListAnnoncesSortOrderEnum } from '../api'
import AppLayout from '../components/AppLayout.vue'
import AnnonceCard from '../components/AnnonceCard.vue'
import { ShieldCheckIcon, MapPinIcon, PhotoIcon } from '@heroicons/vue/24/outline'

const authStore = useAuthStore()
const recentAnnonces = ref<AnnonceList[]>([])
const loading = ref(true)

const fetchRecentAnnonces = async () => {
  try {
    const response = await annoncesApi.listAnnonces(
      undefined, // type
      undefined, // statut
      undefined, // nature
      1, // page
      6, // limit
      undefined, // search
      undefined, // userId
      undefined, // prixMin
      undefined, // prixMax
      undefined, // latitude
      undefined, // longitude
      undefined, // distanceMax
      ListAnnoncesSortByEnum.DateCreation, // sortBy
      ListAnnoncesSortOrderEnum.Desc, // sortOrder
    )
    recentAnnonces.value = response.data.data || []
  } catch (error) {
    console.error('Failed to fetch recent annonces:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecentAnnonces()
})
</script>
