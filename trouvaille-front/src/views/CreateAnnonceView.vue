<template>
  <AppLayout>
    <!-- Loading State -->
    <div v-if="loading" class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="animate-pulse">
        <div class="h-8 bg-gray-200 rounded w-1/4 mb-8"></div>
        <div class="space-y-8">
          <div class="h-32 bg-gray-200 rounded"></div>
          <div class="h-32 bg-gray-200 rounded"></div>
          <div class="h-32 bg-gray-200 rounded"></div>
        </div>
      </div>
    </div>

    <div v-else class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">
          {{ isEditMode ? $t('annonce.edit.title') : $t('annonce.create.title') }}
        </h1>
        <p class="mt-2 text-gray-600">
          {{
            isEditMode
              ? $t('annonce.edit.description')
              : $t('annonce.create.description')
          }}
        </p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="space-y-8">
        <!-- Basic Information -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-6">{{ $t('annonce.form.sections.general') }}</h2>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Type -->
            <div>
              <label for="type" class="block text-sm font-medium text-gray-700 mb-2">
                {{ $t('annonce.form.labels.type_required') }}
              </label>
              <select
                id="type"
                v-model="form.type"
                required
                class="form-input"
                :class="{ 'border-red-500': errors.type }"
              >
                <option value="">{{ $t('annonce.form.placeholders.select_type') }}</option>
                <option :value="AnnonceType.Vente">{{ $t('annonce.types.vente') }}</option>
                <option :value="AnnonceType.Location">{{ $t('annonce.types.location') }}</option>
              </select>
              <p v-if="errors.type" class="mt-1 text-sm text-red-600">{{ errors.type }}</p>
            </div>

            <!-- Nature -->
            <div>
              <label for="nature" class="block text-sm font-medium text-gray-700 mb-2">
                {{ $t('annonce.form.labels.nature_required') }}
              </label>
              <select
                id="nature"
                v-model="form.nature"
                required
                class="form-input"
                :class="{ 'border-red-500': errors.nature }"
              >
                <option value="">{{ $t('annonce.form.placeholders.select_nature') }}</option>
                <option :value="AnnonceNature.Offre">{{ $t('annonce.natures.offre') }}</option>
                <option :value="AnnonceNature.Demande">{{ $t('annonce.natures.demande') }}</option>
              </select>
              <p v-if="errors.nature" class="mt-1 text-sm text-red-600">{{ errors.nature }}</p>
            </div>
          </div>

          <!-- Title -->
          <div class="mt-6">
            <label for="titre" class="block text-sm font-medium text-gray-700 mb-2">
              {{ $t('annonce.form.labels.title_required') }}
            </label>
            <input
              id="titre"
              v-model="form.titre"
              type="text"
              required
              maxlength="100"
              :placeholder="$t('annonce.form.placeholders.title_help')"
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
              {{ $t('annonce.form.labels.description_required') }}
            </label>
            <MarkdownEditor
              v-model="form.description"
              :max-length="2000"
              :placeholder="$t('annonce.form.placeholders.description_help')"
              :class="{ 'border-red-500': errors.description }"
            />
            <p v-if="errors.description" class="mt-1 text-sm text-red-600">{{ errors.description }}</p>
            <p class="mt-1 text-sm text-gray-500">
              {{ $t('annonce.form.placeholders.editor_help') }}
            </p>
          </div>
        </div>

        <!-- Price -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-6">{{ $t('annonce.form.sections.price') }}</h2>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Price -->
            <div>
              <label for="prix" class="block text-sm font-medium text-gray-700 mb-2">
                {{ $t('annonce.form.labels.price_required') }}
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
                {{ $t('annonce.form.labels.period') }}
              </label>
              <select id="periodeLocation" v-model="form.periode_location" class="form-input">
                <option value="">{{ $t('annonce.periode.not_specified') }}</option>
                <option :value="PeriodeLocation.Jour">{{ $t('annonce.periode.jour') }}</option>
                <option :value="PeriodeLocation.Semaine">{{ $t('annonce.periode.semaine') }}</option>
                <option :value="PeriodeLocation.Mois">{{ $t('annonce.periode.mois') }}</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Status (only in edit mode) -->
        <div v-if="isEditMode" class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-6">{{ $t('annonce.form.sections.status') }}</h2>

          <div>
            <label for="statut" class="block text-sm font-medium text-gray-700 mb-2">
              {{ $t('annonce.form.labels.status_required') }}
            </label>
            <select
              id="statut"
              v-model="form.statut"
              required
              class="form-input"
              :class="{ 'border-red-500': errors.statut }"
            >
              <option :value="AnnonceStatut.Active">{{ $t('annonce.status.active') }}</option>
              <option :value="AnnonceStatut.Suspendue">{{ $t('annonce.status.suspendue') }}</option>
              <option :value="AnnonceStatut.Vendue">{{ $t('annonce.status.vendue') }}</option>
            </select>
            <p v-if="errors.statut" class="mt-1 text-sm text-red-600">{{ errors.statut }}</p>
            <p class="mt-1 text-sm text-gray-500">
              {{ $t('annonce.form.help.status') }}
            </p>
          </div>
        </div>

        <!-- Photos -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-6">{{ $t('annonce.form.sections.photos') }}</h2>

          <!-- Photo Upload -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">
              {{ $t('photos.upload.add_photos') }}
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
                {{ $t('photos.upload.drag_drop_or') }}
                <button
                  type="button"
                  @click="($refs.fileInput as HTMLInputElement)?.click()"
                  class="text-blue-600 hover:text-blue-700 underline"
                >
                  {{ $t('photos.upload.browse_files') }}
                </button>
              </p>
              <p class="text-sm text-gray-500">
                {{ $t('photos.upload.accepted_formats') }}
              </p>
            </div>
          </div>

          <!-- Photo Preview Grid -->
          <div v-if="uploadedPhotos.length > 0" class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div v-for="(photo, index) in uploadedPhotos" :key="photo.id" class="relative group">
              <img
                :src="photo.url"
                :alt="`Photo ${index + 1}`"
                class="w-full h-32 object-cover rounded-lg border border-gray-200"
              />

              <!-- Photo Actions -->
              <div
                class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-50 transition-all rounded-lg flex items-center justify-center"
              >
                <div class="opacity-0 group-hover:opacity-100 transition-opacity space-x-2">
                  <button
                    v-if="index > 0"
                    type="button"
                    @click="movePhoto(index, index - 1)"
                    class="p-2 bg-white text-gray-600 rounded-full hover:text-gray-900"
                    :title="$t('photos.actions.move_left')"
                  >
                    <ChevronLeftIcon class="h-4 w-4" />
                  </button>
                  <button
                    v-if="index < uploadedPhotos.length - 1"
                    type="button"
                    @click="movePhoto(index, index + 1)"
                    class="p-2 bg-white text-gray-600 rounded-full hover:text-gray-900"
                    :title="$t('photos.actions.move_right')"
                  >
                    <ChevronRightIcon class="h-4 w-4" />
                  </button>
                  <button
                    type="button"
                    @click="removePhoto(index)"
                    class="p-2 bg-white text-red-600 rounded-full hover:text-red-700"
                    :title="$t('photos.actions.delete')"
                  >
                    <TrashIcon class="h-4 w-4" />
                  </button>
                </div>
              </div>

              <!-- Primary Badge -->
              <div v-if="index === 0" class="absolute top-2 left-2">
                <span
                  class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                >
                  {{ $t('photos.main') }}
                </span>
              </div>
            </div>
          </div>

          <!-- Upload Progress -->
          <div v-if="uploadingPhotos.length > 0" class="mt-4">
            <p class="text-sm font-medium text-gray-700 mb-2">{{ $t('photos.upload.uploading') }}</p>
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
          <h2 class="text-xl font-semibold text-gray-900 mb-6">{{ $t('annonce.form.sections.location') }}</h2>
          {{ $t('location.privacy_note') }}
          <LocationField
            v-model="selectedLocation"
            :auto-detect="!isEditMode"
            @change="handleLocationChange"
          />
        </div>

        <!-- Actions -->
        <div class="flex justify-end space-x-4 pt-6 border-t border-gray-200">
          <router-link
            :to="isEditMode ? `/annonces/${props.id}` : '/annonces'"
            class="btn-secondary"
          >
            {{ $t('common.actions.cancel') }}
          </router-link>
          <button type="submit" :disabled="submitting" class="btn-primary">
            {{
              submitting
                ? isEditMode
                  ? $t('common.actions.modifying')
                  : $t('common.actions.publishing')
                : isEditMode
                  ? $t('annonce.edit.title')
                  : $t('annonce.create.title')
            }}
          </button>
        </div>
      </form>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { annoncesApi, photosApi } from '../services/api'
import {
  AnnonceBase,
  AnnonceWithStatut,
  AnnonceType,
  AnnonceNature,
  AnnonceStatut,
  PeriodeLocation,
  Annonce,
} from '../api'
import AppLayout from '../components/AppLayout.vue'
import MarkdownEditor from '../components/MarkdownEditor.vue'
import LocationField from '../components/LocationField.vue'
import { PhotoIcon, ChevronLeftIcon, ChevronRightIcon, TrashIcon } from '@heroicons/vue/24/outline'
import { photoService } from '../services/photoService'
import { SelectedLocation } from '@/types/location'

interface Props {
  id?: string
}

const props = defineProps<Props>()
const router = useRouter()
const { t } = useI18n()

const form = reactive<AnnonceBase & { statut?: AnnonceStatut }>({
  type: AnnonceType.Vente,
  nature: AnnonceNature.Offre,
  titre: '',
  description: '',
  prix: undefined,
  periode_location: undefined,
  coordinates: { latitude: 0, longitude: 0 },
  ville: '',
  photos: [],
  statut: undefined,
})

const errors = ref<Record<string, string>>({})
const submitting = ref(false)
const uploadedPhotos = ref<Array<{ id: string; url: string }>>([])
const uploadingPhotos = ref<Array<{ name: string; progress: number }>>([])
const selectedLocation = ref<SelectedLocation | null>(null)
const loading = ref(false)

const isEditMode = computed(() => !!props.id)
const existingAnnonce = ref<Annonce>()

const validateForm = () => {
  errors.value = {}

  if (!form.type) errors.value.type = t('validation.type_required')
  if (!form.nature) errors.value.nature = t('validation.nature_required')
  if (!form.titre || form.titre.length < 5) {
    errors.value.titre = t('validation.title_min_length')
  }
  if (!form.description || form.description.length < 10) {
    errors.value.description = t('validation.description_required')
  }
  if (!form.prix || form.prix <= 0) {
    errors.value.prix = t('validation.price_invalid')
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
    alert(t('photos.upload.max_files_exceeded'))
    return
  }

  for (const file of files) {
    if (!file.type.startsWith('image/')) {
      alert(t('photos.upload.invalid_file', { filename: file.name }))
      continue
    }

    if (file.size > 5 * 1024 * 1024) {
      alert(t('photos.upload.file_too_large', { filename: file.name }))
      continue
    }

    await uploadPhoto(file)
  }
}

const uploadPhoto = async (file: File) => {
  const uploadProgress = { name: file.name, progress: 0 }
  uploadingPhotos.value.push(uploadProgress)

  try {
    // Simulate upload progress
    const progressInterval = setInterval(() => {
      if (uploadProgress.progress < 90) {
        uploadProgress.progress += 10
      }
    }, 100)

    // Send raw file data as required by the API
    const response = await photosApi.createPhoto(file)

    clearInterval(progressInterval)
    uploadProgress.progress = 100

    // The response data is directly the photo ID (string)
    const photoId = response.data
    const photoUrl = URL.createObjectURL(file)

    uploadedPhotos.value.push({ id: photoId, url: photoUrl })
    form.photos?.push(photoId)
  } catch (error) {
    console.error('Failed to upload photo:', error)
    alert(t('photos.upload.upload_failed', { filename: file.name }))
  } finally {
    // Remove from uploading list
    const index = uploadingPhotos.value.findIndex((up) => up.name === file.name)
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

    if (form.photos) {
      const photoIndex = form.photos.findIndex((id) => id === photo.id)
      if (photoIndex > -1) {
        form.photos.splice(photoIndex, 1)
      }
    }

    URL.revokeObjectURL(photo.url)
  } catch (error) {
    console.error('Failed to delete photo:', error)
    alert(t('photos.actions.delete_error'))
  }
}

const movePhoto = (fromIndex: number, toIndex: number) => {
  const photos = [...uploadedPhotos.value]
  const photosIds = [...(form.photos || [])]

  // Swap photos
  const temp = photos[fromIndex]
  photos[fromIndex] = photos[toIndex]
  photos[toIndex] = temp

  // Swap IDs
  const tempId = photosIds[fromIndex]
  photosIds[fromIndex] = photosIds[toIndex]
  photosIds[toIndex] = tempId

  uploadedPhotos.value = photos
  form.photos = photosIds
}

const handleLocationChange = (location: SelectedLocation | null) => {
  if (location) {
    form.coordinates = {
      latitude: location.coordinates[1], // latitude is at index 1
      longitude: location.coordinates[0], // longitude is at index 0
    }
    form.ville = location.city
  } else {
    form.coordinates = { latitude: 0, longitude: 0 }
    form.ville = t('location.unknown')
  }
}

const loadExistingAnnonce = async () => {
  if (!props.id) return

  loading.value = true
  try {
    const response = await annoncesApi.getAnnonce(props.id)
    existingAnnonce.value = response.data

    // Populate form with existing data
    form.type = response.data.type
    form.nature = response.data.nature
    form.titre = response.data.titre || ''
    form.description = response.data.description || ''
    form.prix = response.data.prix
    form.periode_location = response.data.periode_location
    form.coordinates = response.data.coordinates
    form.ville = response.data.ville
    form.statut = response.data.statut

    // Set selected location if coordinates exist
    if (response.data.coordinates?.latitude && response.data.coordinates?.longitude) {
      selectedLocation.value = {
        label: t('location.saved', { 
          lat: response.data.coordinates.latitude.toFixed(4), 
          lng: response.data.coordinates.longitude.toFixed(4) 
        }),
        city: response.data.ville,
        coordinates: [response.data.coordinates.longitude, response.data.coordinates.latitude],
      }
    }

    // Load existing photos
    if (response.data.photos && response.data.photos.length > 0) {
      form.photos = [...response.data.photos]

      // Charger les URLs des photos existantes via le service
      const photoPromises = response.data.photos.map(async (photoId) => {
        try {
          const url = await photoService.getPhotoUrl(photoId, 'thumb')
          return { id: photoId, url }
        } catch (error) {
          console.warn(`Failed to load existing photo ${photoId}:`, error)
          return null
        }
      })

      const photoResults = await Promise.allSettled(photoPromises)
      uploadedPhotos.value = photoResults
        .filter((result) => result.status === 'fulfilled' && result.value !== null)
        .map((result) => (result as PromiseFulfilledResult<{ id: string; url: string }>).value)
    }
  } catch (error) {
    console.error('Failed to load annonce:', error)
    alert(t('annonce.edit.error'))
    router.push('/annonces')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!validateForm()) return

  submitting.value = true

  try {
    let response
    if (isEditMode.value && props.id) {
      // Update existing annonce - use AnnonceWithStatut interface
      const updateData: AnnonceWithStatut = {
        type: form.type!,
        nature: form.nature!,
        titre: form.titre,
        description: form.description,
        prix: form.prix,
        periode_location: form.periode_location,
        coordinates: form.coordinates!,
        ville: form.ville,
        statut: form.statut,
        photos: form.photos,
      }
      response = await annoncesApi.putAnnonce(props.id, updateData)
    } else {
      // Create new annonce
      response = await annoncesApi.createAnnonce(form)
    }

    router.push(`/annonces/${response.data.id}`)
  } catch (error) {
    console.error('Failed to save annonce:', error)
    alert(isEditMode.value ? t('annonce.edit.error') : t('annonce.create.error'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (isEditMode.value) {
    loadExistingAnnonce()
  }
})
</script>
