<template>
  <AppLayout>
    <div class="min-h-[50vh] flex items-center justify-center">
      <div class="text-center">
        <h1 class="text-6xl font-bold text-gray-400 mb-4">404</h1>
        <h2 class="text-2xl font-semibold text-gray-900 mb-2">Page non trouvée</h2>
        <p class="text-gray-600 mb-6">La page que vous cherchez n'existe pas ou a été déplacée.</p>
        <router-link to="/" class="btn-primary"> Retour à l'accueil </router-link>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from '../components/AppLayout.vue'
</script>
