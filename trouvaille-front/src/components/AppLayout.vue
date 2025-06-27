<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Navigation -->
    <nav class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <!-- Logo -->
            <router-link to="/annonces" class="flex-shrink-0 flex items-center">
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
                @click="toggleUserMenu"
                class="flex items-center space-x-2 text-gray-700 hover:text-gray-900 transition-colors"
              >
                <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                  <span class="text-blue-600 text-sm font-medium">
                    {{
                      (authStore.user?.nickname || authStore.user?.username || 'U')
                        ?.charAt(0)
                        ?.toUpperCase()
                    }}
                  </span>
                </div>
                <ChevronDownIcon
                  :class="['h-4 w-4 transition-transform', showUserMenu ? 'rotate-180' : '']"
                />
              </button>

              <!-- User Dropdown -->
              <Transition
                enter-active-class="transition ease-out duration-100"
                enter-from-class="transform opacity-0 scale-95"
                enter-to-class="transform opacity-100 scale-100"
                leave-active-class="transition ease-in duration-75"
                leave-from-class="transform opacity-100 scale-100"
                leave-to-class="transform opacity-0 scale-95"
              >
                <div
                  v-if="showUserMenu"
                  class="absolute right-0 mt-2 w-56 bg-white rounded-md shadow-lg border border-gray-200 py-1 z-50"
                >
                  <!-- User Info -->
                  <div class="px-4 py-3 border-b border-gray-100">
                    <p class="text-sm font-medium text-gray-900">
                      {{ authStore.user?.nickname || authStore.user?.username || 'Utilisateur' }}
                    </p>
                    <p class="text-xs text-gray-500">Connecté</p>
                  </div>

                  <!-- Menu Items -->
                  <router-link
                    to="/my-annonces"
                    class="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
                    @click="closeUserMenu"
                  >
                    <svg
                      class="mr-3 h-4 w-4 text-gray-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                      />
                    </svg>
                    Mes annonces
                  </router-link>

                  <button
                    @click="handleLogout"
                    class="flex items-center w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
                  >
                    <svg
                      class="mr-3 h-4 w-4 text-red-400"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                      />
                    </svg>
                    Se déconnecter
                  </button>
                </div>
              </Transition>
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
import { ref, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { PlusIcon, ChevronDownIcon, Bars3Icon, XMarkIcon } from '@heroicons/vue/24/outline'

const authStore = useAuthStore()
const showUserMenu = ref(false)
const showMobileMenu = ref(false)

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const closeUserMenu = () => {
  showUserMenu.value = false
}

const handleLogout = () => {
  authStore.logout()
  closeUserMenu()
}

// Close menu when clicking outside
const handleClickOutside = (event: Event) => {
  const target = event.target as Element
  const userMenuContainer = document.querySelector('.relative')
  
  if (showUserMenu.value && userMenuContainer && !userMenuContainer.contains(target)) {
    closeUserMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
