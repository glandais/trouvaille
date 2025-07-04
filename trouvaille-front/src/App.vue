<template>
  <div id="app">
    <!-- Show loading while authenticating -->
    <div v-if="authStore.isAuthenticating" class="min-h-screen flex items-center justify-center">
      <div class="text-center">
        <div
          class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"
        ></div>
        <p class="text-gray-600">{{ authStore.debugInfo }}</p>
      </div>
    </div>

    <!-- Show login if not authenticated -->
    <div
      v-else-if="!authStore.isAuthenticated"
      class="min-h-screen flex items-center justify-center bg-gray-50"
    >
      <div class="max-w-md w-full space-y-8 p-8">
        <div class="text-center">
          <h1 class="text-3xl font-bold text-gray-900 mb-2">{{ $t('app.name') }}</h1>
          <p class="text-gray-600 mb-8">{{ $t('app.tagline') }}</p>
          <button @click="authStore.login" class="btn-primary w-full py-3 text-lg">
            {{ $t('app.auth.login') }}
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
import { onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from './stores/auth'

const { t, locale } = useI18n()
const authStore = useAuthStore()

const updateTitle = () => {
  document.title = t('app.name')
}

onMounted(async () => {
  // Set initial title
  updateTitle()
  await authStore.initializeAuth()
})

// Watch for locale changes and update title
watch(locale, () => {
  updateTitle()
})
</script>
