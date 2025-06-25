<template>
  <div class="min-h-screen flex items-center justify-center">
    <div class="text-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
      <p class="text-gray-600">Finalisation de l'authentification...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  const code = route.query.code as string
  const state = route.query.state as string

  if (code && state) {
    const success = await authStore.handleOAuthCallback(code, state)
    if (success) {
      router.push('/')
    } else {
      router.push('/?error=auth_failed')
    }
  } else {
    router.push('/?error=invalid_callback')
  }
})
</script>