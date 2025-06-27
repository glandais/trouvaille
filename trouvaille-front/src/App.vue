<template>
  <div id="app">
    <!-- Always show router-view for OAuth callback -->
    <router-view v-if="$route.name === 'oauth-callback'" />

    <!-- Show loading while authenticating -->
    <div
      v-else-if="authStore.isAuthenticating"
      class="min-h-screen flex items-center justify-center"
    >
      <div class="text-center">
        <div
          class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"
        ></div>
        <p class="text-gray-600">Authentification en cours...</p>
      </div>
    </div>

    <!-- Show login if not authenticated -->
    <div
      v-else-if="!authStore.isAuthenticated"
      class="min-h-screen flex items-center justify-center bg-gray-50"
    >
      <div class="max-w-md w-full space-y-8 p-8">
        <div class="text-center">
          <h1 class="text-3xl font-bold text-gray-900 mb-2">Trouvaille</h1>
          <p class="text-gray-600 mb-8">Plateforme de petites annonces privée</p>
          <button @click="authStore.login" class="btn-primary w-full py-3 text-lg">
            Se connecter
          </button>
        </div>
      </div>
    </div>

    <!-- Main app when authenticated -->
    <div v-else class="min-h-screen">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from './stores/auth'
import { useLocationStore } from './stores/location'

const authStore = useAuthStore()
const locationStore = useLocationStore()

onMounted(async () => {
  await authStore.initializeAuth()

  // Initialize user location once authenticated
  if (authStore.isAuthenticated) {
    locationStore.initializeUserLocation()
  }
})
</script>
