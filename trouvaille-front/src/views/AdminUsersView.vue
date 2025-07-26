<template>
  <AppLayout>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Gestion des utilisateurs</h1>
      </div>

      <!-- Search -->
      <div class="bg-white rounded-lg shadow-xs border border-gray-200 p-6 mb-8">
        <div class="max-w-md">
          <label for="search" class="block text-sm font-medium text-gray-700 mb-1">
            Rechercher un utilisateur
          </label>
          <input
            id="search"
            v-model="searchQuery"
            type="text"
            placeholder="Nom d'utilisateur ou pseudo..."
            class="form-input"
            @input="debouncedSearch"
          />
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="bg-white rounded-lg shadow-xs border border-gray-200">
        <div class="animate-pulse">
          <div class="px-6 py-4 border-b border-gray-200">
            <div class="h-4 bg-gray-200 rounded w-1/4"></div>
          </div>
          <div v-for="i in 5" :key="i" class="px-6 py-4 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-4">
                <div class="w-10 h-10 bg-gray-200 rounded-full"></div>
                <div class="space-y-2">
                  <div class="h-4 bg-gray-200 rounded w-32"></div>
                  <div class="h-3 bg-gray-200 rounded w-24"></div>
                </div>
              </div>
              <div class="w-16 h-8 bg-gray-200 rounded"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Users Table -->
      <div v-else class="bg-white rounded-lg shadow-xs border border-gray-200 overflow-hidden">
        <!-- Results count -->
        <div class="px-6 py-4 border-b border-gray-200 bg-gray-50">
          <p class="text-sm text-gray-600">
            {{ pagination?.total_elements || 0 }} utilisateur(s) trouvé(s)
          </p>
        </div>

        <div v-if="users.length > 0">
          <!-- Table Header -->
          <div class="px-6 py-3 border-b border-gray-200 bg-gray-50">
            <div
              class="grid grid-cols-12 gap-4 text-xs font-medium text-gray-500 uppercase tracking-wide"
            >
              <div class="col-span-6">Utilisateur</div>
              <div class="col-span-3">Statut</div>
              <div class="col-span-3 text-center">Actions</div>
            </div>
          </div>

          <!-- Users List -->
          <div class="divide-y divide-gray-200">
            <div
              v-for="user in users"
              :key="user.id"
              class="px-6 py-4 hover:bg-gray-50 transition-colors"
            >
              <div class="grid grid-cols-12 gap-4 items-center">
                <!-- User Info -->
                <div class="col-span-6 flex items-center space-x-4">
                  <div class="flex-shrink-0">
                    <div
                      class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center"
                    >
                      <span class="text-blue-600 font-medium text-sm">
                        {{ getUserInitials(user) }}
                      </span>
                    </div>
                  </div>
                  <div class="min-w-0 flex-1">
                    <p class="text-sm font-medium text-gray-900 truncate">
                      {{ user.nickname || user.username || 'Utilisateur' }}
                    </p>
                    <p class="text-sm text-gray-500 truncate">
                      {{ user.username ? `@${user.username}` : user.id }}
                    </p>
                  </div>
                </div>

                <!-- Admin Status -->
                <div class="col-span-3">
                  <span
                    :class="[
                      'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                      user.admin ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800',
                    ]"
                  >
                    {{ user.admin ? 'Administrateur' : 'Utilisateur' }}
                  </span>
                </div>

                <!-- Actions -->
                <div class="col-span-3 text-center">
                  <button
                    @click="toggleAdminStatus(user)"
                    :disabled="updatingUsers.has(user.id)"
                    :class="[
                      'inline-flex items-center px-3 py-1.5 border text-sm font-medium rounded-md transition-colors',
                      user.admin
                        ? 'border-red-300 text-red-700 bg-red-50 hover:bg-red-100 disabled:opacity-50'
                        : 'border-green-300 text-green-700 bg-green-50 hover:bg-green-100 disabled:opacity-50',
                    ]"
                  >
                    <template v-if="updatingUsers.has(user.id)">
                      <svg class="animate-spin -ml-1 mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24">
                        <circle
                          class="opacity-25"
                          cx="12"
                          cy="12"
                          r="10"
                          stroke="currentColor"
                          stroke-width="4"
                        ></circle>
                        <path
                          class="opacity-75"
                          fill="currentColor"
                          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                        ></path>
                      </svg>
                      Mise à jour...
                    </template>
                    <template v-else>
                      {{ user.admin ? 'Retirer admin' : 'Rendre admin' }}
                    </template>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="px-6 py-12 text-center">
          <div class="text-gray-400 mb-4">
            <svg class="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1"
                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
              />
            </svg>
          </div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">Aucun utilisateur trouvé</h3>
          <p class="text-gray-600 mb-6">
            {{
              searchQuery
                ? 'Aucun utilisateur ne correspond à votre recherche.'
                : 'Aucun utilisateur enregistré.'
            }}
          </p>
          <button v-if="searchQuery" @click="clearSearch" class="btn-primary">
            Effacer la recherche
          </button>
        </div>

        <!-- Pagination -->
        <div
          v-if="pagination && pagination.total_pages > 1"
          class="px-6 py-4 border-t border-gray-200"
        >
          <nav class="flex justify-center">
            <div class="flex space-x-2">
              <!-- Previous -->
              <button
                :disabled="pagination.page_courante <= 1"
                @click="changePage(pagination.page_courante - 1)"
                class="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Précédent
              </button>

              <!-- Page numbers -->
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

              <!-- Next -->
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
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import { adminApi } from '../services/api'
import { Utilisateur, Pagination, UserAdminUpdate } from '../api'
import AppLayout from '../components/AppLayout.vue'

const users = ref<Utilisateur[]>([])
const pagination = ref<Pagination>()
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const updatingUsers = ref(new Set<string>())

const fetchUsers = async () => {
  loading.value = true
  try {
    const response = await adminApi.listUsers(
      currentPage.value,
      20, // limit
      searchQuery.value || undefined,
    )
    users.value = response.data.data || []
    pagination.value = response.data.pagination
  } catch (error) {
    console.error('Failed to fetch users:', error)
  } finally {
    loading.value = false
  }
}

const debouncedSearch = useDebounceFn(() => {
  currentPage.value = 1
  fetchUsers()
}, 500)

const changePage = (page: number) => {
  currentPage.value = page
  fetchUsers()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const clearSearch = () => {
  searchQuery.value = ''
  currentPage.value = 1
  fetchUsers()
}

const getUserInitials = (user: Utilisateur): string => {
  const name = user.nickname || user.username || user.id
  return name.charAt(0).toUpperCase()
}

const toggleAdminStatus = async (user: Utilisateur) => {
  updatingUsers.value.add(user.id)

  try {
    const updateData: UserAdminUpdate = {
      admin: !user.admin,
    }

    await adminApi.updateUserAdmin(user.id, updateData)

    // Update local user data
    user.admin = !user.admin
  } catch (error) {
    console.error('Failed to update user admin status:', error)
  } finally {
    updatingUsers.value.delete(user.id)
  }
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

onMounted(() => {
  fetchUsers()
})
</script>
