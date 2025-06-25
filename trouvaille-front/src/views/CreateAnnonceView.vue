<template>
  <AppLayout>
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Créer une annonce</h1>
        <p class="mt-2 text-gray-600">Remplissez les informations ci-dessous pour publier votre annonce</p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="space-y-8">
        <!-- Basic Information -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-6">Informations générales</h2>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Type -->
            <div>
              <label for="type" class="block text-sm font-medium text-gray-700 mb-2">
                Type d'annonce *
              </label>
              <select
                id="type"
                v-model="form.type"
                required
                class="form-input"
                :class="{ 'border-red-500': errors.type }"
              >
                <option value="">Sélectionnez un type</option>
                <option :value="AnnonceType.Vente">Vente</option>
                <option :value="AnnonceType.Location">Location</option>
              </select>
              <p v-if="errors.type" class="mt-1 text-sm text-red-600">{{ errors.type }}</p>
            </div>

            <!-- Nature -->
            <div>
              <label for="nature" class="block text-sm font-medium text-gray-700 mb-2">
                Nature de l'annonce *
              </label>
              <select
                id="nature"
                v-model="form.nature"
                required
                class="form-input"
                :class="{ 'border-red-500': errors.nature }"
              >
                <option value="">Sélectionnez une nature</option>
                <option :value="AnnonceNature.Offre">Offre</option>
                <option :value="AnnonceNature.Demande">Demande</option>
              </select>
              <p v-if="errors.nature" class="mt-1 text-sm text-red-600">{{ errors.nature }}</p>
            </div>
          </div>

          <!-- Title -->
          <div class="mt-6">
            <label for="titre" class="block text-sm font-medium text-gray-700 mb-2">
              Titre *
            </label>
            <input
              id="titre"
              v-model="form.titre"
              type="text"
              required
              maxlength="100"
              placeholder="Titre de votre annonce (5-100 caractères)"
              class="form-input"
              :class="{ 'border-red-500': errors.titre }"
            />
            <div class="flex justify-between mt-1">
              <p v-if="errors.titre" class="text-sm text-red-600">{{ errors.titre }}</p>
              <p class="text-sm text-gray-500">{{ form.titre.length }}/100</p>
            </div>
          </div>

          <!-- Description -->
          <div class="mt-6">
            <label for="description" class="block text-sm font-medium text-gray-700 mb-2">
              Description *
            </label>
            <textarea
              id="description"
              v-model="form.description"
              rows="6"
              required
              maxlength="2000"
              placeholder="Décrivez votre objet en détail (Markdown supporté)"
              class="form-textarea"
              :class="{ 'border-red-500': errors.description }"
            ></textarea>
            <div class="flex justify-between mt-1">
              <p v-if="errors.description" class="text-sm text-red-600">{{ errors.description }}</p>
              <p class="text-sm text-gray-500">{{ form.description.length }}/2000</p>
            </div>
            <p class="mt-1 text-sm text-gray-500">
              Vous pouvez utiliser le Markdown pour formater votre texte (gras, italique, listes...)
            </p>
          </div>
        </div>

        <!-- Price -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-6">Prix</h2>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Price -->
            <div>
              <label for="prix" class="block text-sm font-medium text-gray-700 mb-2">
                Prix (€) *
              </label>
              <input
                id="prix"
                v-model="form.prix"
                type="number"
                step="0.01"
                min="0"
                required
                placeholder="0.00"
                class="form-input"
                :class="{ 'border-red-500': errors.prix }"
              />
              <p v-if="errors.prix" class="mt-1 text-sm text-red-600">{{ errors.prix }}</p>
            </div>

            <!-- Period (for rentals) -->
            <div v-if="form.type === AnnonceType.Location">
              <label for="periodeLocation" class="block text-sm font-medium text-gray-700 mb-2">
                Période de location
              </label>
              <select
                id="periodeLocation"
                v-model="form.periodeLocation"
                class="form-input"
              >
                <option value="">Non spécifiée</option>
                <option :value="PeriodeLocation.Jour">Par jour</option>
                <option :value="PeriodeLocation.Semaine">Par semaine</option>
                <option :value="PeriodeLocation.Mois">Par mois</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Photos -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-6">Photos</h2>
          
          <!-- Photo Upload -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Ajouter des photos (max 10)
            </label>
            <div
              @drop="handleDrop"
              @dragover.prevent
              @dragenter.prevent
              class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-gray-400 transition-colors"
            >
              <input
                ref="fileInput"
                type="file"
                multiple
                accept="image/*"
                class="hidden"
                @change="handleFileSelect"
              />
              <PhotoIcon class="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <p class="text-gray-600 mb-2">
                Glissez-déposez vos photos ici ou 
                <button
                  type="button"
                  @click="$refs.fileInput.click()"
                  class="text-blue-600 hover:text-blue-700 underline"
                >
                  parcourez vos fichiers
                </button>
              </p>
              <p class="text-sm text-gray-500">
                Formats acceptés: JPG, PNG, WebP (max 5MB par photo)
              </p>
            </div>
          </div>

          <!-- Photo Preview Grid -->
          <div v-if="uploadedPhotos.length > 0" class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div
              v-for="(photo, index) in uploadedPhotos"
              :key="photo.id"
              class="relative group"
            >
              <img
                :src="photo.url"
                :alt="`Photo ${index + 1}`"
                class="w-full h-32 object-cover rounded-lg border border-gray-200"
              />
              
              <!-- Photo Actions -->
              <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-50 transition-all rounded-lg flex items-center justify-center">
                <div class="opacity-0 group-hover:opacity-100 transition-opacity space-x-2">
                  <button
                    v-if="index > 0"
                    type="button"
                    @click="movePhoto(index, index - 1)"
                    class="p-2 bg-white text-gray-600 rounded-full hover:text-gray-900"
                    title="Déplacer vers la gauche"
                  >
                    <ChevronLeftIcon class="h-4 w-4" />
                  </button>
                  <button
                    v-if="index < uploadedPhotos.length - 1"
                    type="button"
                    @click="movePhoto(index, index + 1)"
                    class="p-2 bg-white text-gray-600 rounded-full hover:text-gray-900"
                    title="Déplacer vers la droite"
                  >
                    <ChevronRightIcon class="h-4 w-4" />
                  </button>
                  <button
                    type="button"
                    @click="removePhoto(index)"
                    class="p-2 bg-white text-red-600 rounded-full hover:text-red-700"
                    title="Supprimer"
                  >
                    <TrashIcon class="h-4 w-4" />
                  </button>
                </div>
              </div>

              <!-- Primary Badge -->
              <div v-if="index === 0" class="absolute top-2 left-2">
                <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                  Photo principale
                </span>
              </div>
            </div>
          </div>

          <!-- Upload Progress -->
          <div v-if="uploadingPhotos.length > 0" class="mt-4">
            <p class="text-sm font-medium text-gray-700 mb-2">Upload en cours...</p>
            <div class="space-y-2">
              <div
                v-for="upload in uploadingPhotos"
                :key="upload.name"
                class="flex items-center space-x-3"
              >
                <div class="flex-1 bg-gray-200 rounded-full h-2">
                  <div
                    class="bg-blue-600 h-2 rounded-full transition-all"
                    :style="{ width: `${upload.progress}%` }"
                  ></div>
                </div>
                <span class="text-sm text-gray-600">{{ upload.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Location -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-6">Localisation</h2>
          
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <!-- Address -->
            <div class="md:col-span-2">
              <label for="address" class="block text-sm font-medium text-gray-700 mb-2">
                Adresse
              </label>
              <input
                id="address"
                v-model="addressSearch"
                type="text"
                placeholder="Rechercher une adresse..."
                class="form-input"
                @input="debouncedAddressSearch"
              />
              <!-- Address suggestions would go here in a full implementation -->
            </div>

            <!-- Coordinates -->
            <div>
              <button
                type="button"
                @click="getCurrentLocation"
                :disabled="gettingLocation"
                class="btn-secondary w-full"
              >
                <MapPinIcon class="h-4 w-4 mr-2" />
                {{ gettingLocation ? 'Localisation...' : 'Ma position' }}
              </button>
            </div>
          </div>

          <!-- Manual coordinates -->
          <div v-if="form.coordinates?.latitude && form.coordinates?.longitude" class="mt-4 p-4 bg-green-50 rounded-lg">
            <p class="text-sm text-green-800">
              <MapPinIcon class="h-4 w-4 inline mr-1" />
              Position définie: {{ form.coordinates.latitude.toFixed(4) }}, {{ form.coordinates.longitude.toFixed(4) }}
            </p>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex justify-end space-x-4 pt-6 border-t border-gray-200">
          <router-link to="/annonces" class="btn-secondary">
            Annuler
          </router-link>
          <button
            type="submit"
            :disabled="submitting"
            class="btn-primary"
          >
            {{ submitting ? 'Publication...' : 'Publier l\'annonce' }}
          </button>
        </div>
      </form>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useDebounceFn } from '@vueuse/core'
import { annoncesApi, photosApi } from '../services/api'
import { AnnonceCreate, AnnonceType, AnnonceNature, PeriodeLocation, Coordinates } from '../api'
import AppLayout from '../components/AppLayout.vue'
import {
  PhotoIcon,
  MapPinIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  TrashIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()

const form = reactive<AnnonceCreate>({
  type: undefined,
  nature: undefined,
  titre: '',
  description: '',
  prix: undefined,
  periodeLocation: undefined,
  coordinates: undefined,
  photosIds: []
})

const errors = ref<Record<string, string>>({})
const submitting = ref(false)
const uploadedPhotos = ref<Array<{ id: string, url: string }>>([])
const uploadingPhotos = ref<Array<{ name: string, progress: number }>>([])
const addressSearch = ref('')
const gettingLocation = ref(false)

const validateForm = () => {
  errors.value = {}

  if (!form.type) errors.value.type = 'Le type est requis'
  if (!form.nature) errors.value.nature = 'La nature est requise'
  if (!form.titre || form.titre.length < 5) {
    errors.value.titre = 'Le titre doit contenir au moins 5 caractères'
  }
  if (!form.description || form.description.length < 10) {
    errors.value.description = 'La description doit contenir au moins 10 caractères'
  }
  if (!form.prix || form.prix <= 0) {
    errors.value.prix = 'Le prix doit être supérieur à 0'
  }

  return Object.keys(errors.value).length === 0
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    handleFiles(Array.from(target.files))
  }
}

const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer?.files) {
    handleFiles(Array.from(event.dataTransfer.files))
  }
}

const handleFiles = async (files: File[]) => {
  if (uploadedPhotos.value.length + files.length > 10) {
    alert('Vous ne pouvez pas ajouter plus de 10 photos')
    return
  }

  for (const file of files) {
    if (!file.type.startsWith('image/')) {
      alert(`${file.name} n'est pas une image valide`)
      continue
    }

    if (file.size > 5 * 1024 * 1024) {
      alert(`${file.name} est trop volumineux (max 5MB)`)
      continue
    }

    await uploadPhoto(file)
  }
}

const uploadPhoto = async (file: File) => {
  const uploadProgress = { name: file.name, progress: 0 }
  uploadingPhotos.value.push(uploadProgress)

  try {
    const formData = new FormData()
    formData.append('file', file)

    // Simulate upload progress
    const progressInterval = setInterval(() => {
      if (uploadProgress.progress < 90) {
        uploadProgress.progress += 10
      }
    }, 100)

    const response = await photosApi.uploadPhoto(formData, {
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total) {
          uploadProgress.progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        }
      }
    })

    clearInterval(progressInterval)
    uploadProgress.progress = 100

    // Add to uploaded photos
    const photoId = response.data.id
    const photoUrl = URL.createObjectURL(file)
    
    uploadedPhotos.value.push({ id: photoId, url: photoUrl })
    form.photosIds?.push(photoId)

  } catch (error) {
    console.error('Failed to upload photo:', error)
    alert(`Erreur lors de l'upload de ${file.name}`)
  } finally {
    // Remove from uploading list
    const index = uploadingPhotos.value.findIndex(up => up.name === file.name)
    if (index > -1) {
      uploadingPhotos.value.splice(index, 1)
    }
  }
}

const removePhoto = async (index: number) => {
  const photo = uploadedPhotos.value[index]
  
  try {
    await photosApi.deletePhoto(photo.id)
    uploadedPhotos.value.splice(index, 1)
    
    if (form.photosIds) {
      const photoIndex = form.photosIds.findIndex(id => id === photo.id)
      if (photoIndex > -1) {
        form.photosIds.splice(photoIndex, 1)
      }
    }

    URL.revokeObjectURL(photo.url)
  } catch (error) {
    console.error('Failed to delete photo:', error)
    alert('Erreur lors de la suppression de la photo')
  }
}

const movePhoto = (fromIndex: number, toIndex: number) => {
  const photos = [...uploadedPhotos.value]
  const photosIds = [...(form.photosIds || [])]
  
  // Swap photos
  const temp = photos[fromIndex]
  photos[fromIndex] = photos[toIndex]
  photos[toIndex] = temp
  
  // Swap IDs
  const tempId = photosIds[fromIndex]
  photosIds[fromIndex] = photosIds[toIndex]
  photosIds[toIndex] = tempId
  
  uploadedPhotos.value = photos
  form.photosIds = photosIds
}

const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    alert('La géolocalisation n\'est pas supportée par votre navigateur')
    return
  }

  gettingLocation.value = true
  
  navigator.geolocation.getCurrentPosition(
    (position) => {
      form.coordinates = {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude
      }
      gettingLocation.value = false
    },
    (error) => {
      console.error('Geolocation error:', error)
      alert('Impossible d\'obtenir votre position')
      gettingLocation.value = false
    }
  )
}

const debouncedAddressSearch = useDebounceFn(() => {
  // In a full implementation, this would call a geocoding API
  console.log('Searching address:', addressSearch.value)
}, 500)

const handleSubmit = async () => {
  if (!validateForm()) return

  submitting.value = true

  try {
    const response = await annoncesApi.createAnnonce(form)
    router.push(`/annonces/${response.data.id}`)
  } catch (error) {
    console.error('Failed to create annonce:', error)
    alert('Erreur lors de la création de l\'annonce')
  } finally {
    submitting.value = false
  }
}
</script>