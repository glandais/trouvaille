<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Navigation -->
    <nav class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <!-- Logo -->
            <router-link to="/" class="flex-shrink-0 flex items-center">
              <h1 class="text-xl font-bold text-blue-600">Trouvaille</h1>
            </router-link>

            <!-- Navigation Links (Desktop) -->
            <div class="hidden md:ml-6 md:flex md:space-x-8">
              <router-link
                to="/annonces"
                class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors"
                active-class="border-blue-500 text-blue-600"
              >
                Annonces
              </router-link>
              <router-link
                v-if="authStore.isAuthenticated"
                to="/my-annonces"
                class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors"
                active-class="border-blue-500 text-blue-600"
              >
                Mes annonces
              </router-link>
            </div>
          </div>

          <div class="flex items-center space-x-4">
            <!-- Create Annonce Button -->
            <router-link
              v-if="authStore.isAuthenticated"
              to="/annonces/create"
              class="btn-primary hidden sm:inline-flex items-center"
            >
              <PlusIcon class="h-4 w-4 mr-2" />
              Créer une annonce
            </router-link>

            <!-- Mobile Create Button -->
            <router-link
              v-if="authStore.isAuthenticated"
              to="/annonces/create"
              class="sm:hidden p-2 text-blue-600 hover:text-blue-700"
            >
              <PlusIcon class="h-6 w-6" />
            </router-link>

            <!-- User Menu -->
            <div v-if="authStore.isAuthenticated" class="relative">
              <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center space-x-2 text-gray-700 hover:text-gray-900 transition-colors"
              >
                <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                  <span class="text-blue-600 text-sm font-medium">
                    {{ authStore.user?.pseudo?.charAt(0)?.toUpperCase() }}
                  </span>
                </div>
                <ChevronDownIcon class="h-4 w-4" />
              </button>

              <!-- User Dropdown -->
              <div
                v-if="showUserMenu"
                v-click-outside="() => showUserMenu = false"
                class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50"
              >
                <div class="px-4 py-2 text-sm text-gray-700 border-b">
                  {{ authStore.user?.pseudo }}
                </div>
                <router-link
                  to="/my-annonces"
                  class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  @click="showUserMenu = false"
                >
                  Mes annonces
                </router-link>
                <button
                  @click="handleLogout"
                  class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                >
                  Se déconnecter
                </button>
              </div>
            </div>

            <!-- Mobile Menu Button -->
            <button
              @click="showMobileMenu = !showMobileMenu"
              class="md:hidden p-2 text-gray-400 hover:text-gray-500"
            >
              <Bars3Icon v-if="!showMobileMenu" class="h-6 w-6" />
              <XMarkIcon v-else class="h-6 w-6" />
            </button>
          </div>
        </div>
      </div>

      <!-- Mobile Menu -->
      <div v-if="showMobileMenu" class="md:hidden">
        <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3 border-t border-gray-200">
          <router-link
            to="/annonces"
            class="text-gray-700 hover:text-gray-900 block px-3 py-2 rounded-md text-base font-medium"
            @click="showMobileMenu = false"
          >
            Annonces
          </router-link>
          <router-link
            v-if="authStore.isAuthenticated"
            to="/my-annonces"
            class="text-gray-700 hover:text-gray-900 block px-3 py-2 rounded-md text-base font-medium"
            @click="showMobileMenu = false"
          >
            Mes annonces
          </router-link>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main>
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { PlusIcon, ChevronDownIcon, Bars3Icon, XMarkIcon } from '@heroicons/vue/24/outline'

const authStore = useAuthStore()
const showUserMenu = ref(false)
const showMobileMenu = ref(false)

const handleLogout = () => {
  authStore.logout()
  showUserMenu.value = false
}

// Click outside directive implementation
const vClickOutside = {
  beforeMount(el: any, binding: any) {
    el.clickOutsideEvent = (event: Event) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event)
      }
    }
    document.addEventListener('click', el.clickOutsideEvent)
  },
  unmounted(el: any) {
    document.removeEventListener('click', el.clickOutsideEvent)
  }
}
</script>