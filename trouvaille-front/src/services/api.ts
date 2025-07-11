import axios from 'axios'
import type { AxiosInstance } from 'axios'
import { Configuration, AnnoncesApi, PhotosApi, AuthentificationApi, ConfigApi } from '../api'
import { useAuthStore } from '../stores/auth'

const baseURL = window.location.origin

// Create axios instance with base configuration
const axiosInstance: AxiosInstance = axios.create({
  baseURL,
  timeout: 10000,
})

// Request interceptor to add Bearer token
axiosInstance.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// Response interceptor to handle auth errors
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
    }
    return Promise.reject(error)
  },
)

// Create API configuration
const apiConfiguration = new Configuration({
  basePath: baseURL,
})

// Export API instances
export const annoncesApi = new AnnoncesApi(apiConfiguration, undefined, axiosInstance)
export const photosApi = new PhotosApi(apiConfiguration, undefined, axiosInstance)
export const authentificationApi = new AuthentificationApi(
  apiConfiguration,
  undefined,
  axiosInstance,
)
export const configApi = new ConfigApi(apiConfiguration, undefined, undefined)

export { axiosInstance }
