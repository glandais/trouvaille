<template>
  <div class="min-h-screen flex items-center justify-center">
    <div class="text-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
      <p class="text-gray-600">Finalisation de l'authentification...</p>
      <p v-if="debugInfo" class="text-xs text-gray-400 mt-4">{{ debugInfo }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const debugInfo = ref('')

// Immediate execution to debug
console.log('OAuthCallbackView component mounted')
console.log('Current route:', route.fullPath)
console.log('Query params:', route.query)

onMounted(async () => {
  console.log('onMounted called in OAuthCallbackView')
  debugInfo.value = 'Processing OAuth callback...'
  
  const code = route.query.code as string
  const state = route.query.state as string

  console.log('Code:', code)
  console.log('State:', state)

  if (code && state) {
    debugInfo.value = 'Exchanging code for token...'
    const success = await authStore.handleOAuthCallback(code, state)
    if (success) {
      debugInfo.value = 'Success! Redirecting...'
      router.push('/')
    } else {
      debugInfo.value = 'Authentication failed'
      router.push('/?error=auth_failed')
    }
  } else {
    debugInfo.value = 'Invalid callback parameters'
    router.push('/?error=invalid_callback')
  }
})

// Alternative: immediate execution without onMounted
const handleOAuthCallback = async () => {
  console.log('Immediate OAuth handling')
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
}

// Try immediate execution if onMounted doesn't work
handleOAuthCallback()
</script>