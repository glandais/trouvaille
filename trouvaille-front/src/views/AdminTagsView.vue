<template>
  <AppLayout>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Gestion des tags</h1>
        <button @click="openCreateModal" class="btn-primary">
          <PlusIcon class="h-4 w-4 mr-2" />
          Nouveau tag
        </button>
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
                <div class="w-6 h-6 bg-gray-200 rounded-full"></div>
                <div class="h-4 bg-gray-200 rounded w-32"></div>
              </div>
              <div class="flex space-x-2">
                <div class="w-16 h-8 bg-gray-200 rounded"></div>
                <div class="w-16 h-8 bg-gray-200 rounded"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tags List -->
      <div v-else class="bg-white rounded-lg shadow-xs border border-gray-200 overflow-hidden">
        <!-- Results count -->
        <div class="px-6 py-4 border-b border-gray-200 bg-gray-50">
          <p class="text-sm text-gray-600">{{ tags.length }} tag(s) disponible(s)</p>
        </div>

        <div v-if="tags.length > 0">
          <!-- Tags List -->
          <div class="divide-y divide-gray-200">
            <div
              v-for="tag in tags"
              :key="tag.id"
              class="px-6 py-4 hover:bg-gray-50 transition-colors"
            >
              <div class="flex items-center justify-between">
                <!-- Tag Info -->
                <div class="flex items-center space-x-4">
                  <div
                    class="w-6 h-6 rounded-full border"
                    :style="{ backgroundColor: tag.couleur }"
                  ></div>
                  <div>
                    <div class="flex items-center space-x-2">
                      <p class="text-sm font-medium text-gray-900">{{ tag.nom }}</p>
                      <span
                        :class="[
                          'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium',
                          tag.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800',
                        ]"
                      >
                        {{ tag.active ? 'Actif' : 'Inactif' }}
                      </span>
                    </div>
                    <p class="text-xs text-gray-500">{{ tag.couleur }}</p>
                  </div>
                </div>

                <!-- Actions -->
                <div class="flex space-x-2">
                  <button
                    @click="openEditModal(tag)"
                    class="inline-flex items-center px-3 py-1.5 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 transition-colors"
                  >
                    <PencilIcon class="h-4 w-4 mr-1" />
                    Modifier
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="px-6 py-12 text-center">
          <div class="text-gray-400 mb-4">
            <TagIcon class="h-12 w-12 mx-auto" />
          </div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">Aucun tag disponible</h3>
          <p class="text-gray-600 mb-6">
            Créez votre premier tag pour commencer à organiser les annonces.
          </p>
          <button @click="openCreateModal" class="btn-primary">Créer un tag</button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="fixed inset-0 z-50 overflow-y-auto">
      <!-- Backdrop -->
      <div
        class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"
        @click="closeModal"
      ></div>

      <!-- Modal container -->
      <div
        class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0"
      >
        <!-- Hidden element to trick browser into centering modal on small screens -->
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true"
          >&#8203;</span
        >

        <!-- Modal content -->
        <div
          class="relative inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full"
        >
          <form @submit.prevent="saveTag">
            <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
              <div class="sm:flex sm:items-start">
                <div class="mt-3 text-center sm:mt-0 sm:text-left w-full">
                  <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">
                    {{ editingTag ? 'Modifier le tag' : 'Nouveau tag' }}
                  </h3>

                  <div class="space-y-4">
                    <!-- Tag Name -->
                    <div>
                      <label for="tagName" class="block text-sm font-medium text-gray-700 mb-1">
                        Nom du tag
                      </label>
                      <input
                        id="tagName"
                        v-model="formData.nom"
                        type="text"
                        required
                        maxlength="50"
                        class="form-input"
                        placeholder="Nom du tag"
                      />
                    </div>

                    <!-- Color -->
                    <div>
                      <label for="tagColor" class="block text-sm font-medium text-gray-700 mb-1">
                        Couleur
                      </label>
                      <div class="flex items-center space-x-3">
                        <input
                          id="tagColor"
                          v-model="formData.couleur"
                          type="color"
                          required
                          class="w-12 h-10 border border-gray-300 rounded-md cursor-pointer"
                        />
                        <input
                          v-model="formData.couleur"
                          type="text"
                          required
                          pattern="^#[0-9A-Fa-f]{6}$"
                          class="form-input flex-1"
                          placeholder="#3B82F6"
                        />
                      </div>
                    </div>

                    <!-- Active Status -->
                    <div>
                      <div class="flex items-center">
                        <input
                          id="tagActive"
                          v-model="formData.active"
                          type="checkbox"
                          class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                        />
                        <label for="tagActive" class="ml-2 block text-sm text-gray-900">
                          Tag actif
                        </label>
                      </div>
                      <p class="mt-1 text-xs text-gray-500">
                        Les tags inactifs ne sont pas visibles lors de la création d'annonces
                      </p>
                    </div>

                    <!-- Preview -->
                    <div>
                      <label class="block text-sm font-medium text-gray-700 mb-1"> Aperçu </label>
                      <div class="flex items-center space-x-2 p-3 bg-gray-50 rounded-md">
                        <div
                          class="w-6 h-6 rounded-full border"
                          :style="{ backgroundColor: formData.couleur }"
                        ></div>
                        <span class="text-sm text-gray-900">{{
                          formData.nom || 'Nom du tag'
                        }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
              <button
                type="submit"
                :disabled="submitting"
                class="w-full inline-flex justify-center rounded-md borders border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:ml-3 sm:w-auto sm:text-sm disabled:opacity-50"
              >
                <template v-if="submitting">
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
                  {{ editingTag ? 'Modification...' : 'Création...' }}
                </template>
                <template v-else>
                  {{ editingTag ? 'Modifier' : 'Créer' }}
                </template>
              </button>
              <button
                type="button"
                @click="closeModal"
                class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
              >
                Annuler
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '../services/api'
import { Tag, TagCreateUpdate } from '../api'
import AppLayout from '../components/AppLayout.vue'
import { PlusIcon, PencilIcon, TagIcon } from '@heroicons/vue/24/outline'

const tags = ref<Tag[]>([])
const loading = ref(false)
const showModal = ref(false)
const editingTag = ref<Tag | null>(null)
const submitting = ref(false)

const formData = ref<TagCreateUpdate>({
  nom: '',
  couleur: '#3B82F6',
  active: true,
})

const fetchTags = async () => {
  loading.value = true
  try {
    const response = await adminApi.listTags()
    tags.value = response.data || []
  } catch (error) {
    console.error('Failed to fetch tags:', error)
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  editingTag.value = null
  formData.value = {
    nom: '',
    couleur: '#3B82F6',
    active: true,
  }
  showModal.value = true
}

const openEditModal = (tag: Tag) => {
  editingTag.value = tag
  formData.value = {
    nom: tag.nom,
    couleur: tag.couleur,
    active: tag.active,
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingTag.value = null
  submitting.value = false
}

const saveTag = async () => {
  submitting.value = true

  try {
    const data: TagCreateUpdate = {
      nom: formData.value.nom,
      couleur: formData.value.couleur,
      active: formData.value.active,
    }
    if (editingTag.value) {
      // Update existing tag
      await adminApi.updateTag(editingTag.value.id, data)
    } else {
      // Create new tag
      await adminApi.createTag(data)
    }

    closeModal()
    await fetchTags()
  } catch (error) {
    console.error('Failed to save tag:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchTags()
})
</script>
