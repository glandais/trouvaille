import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { type OAuthTokenRequest, type Utilisateur } from '../api'
import { authentificationApi, configApi } from '../services/api'

export const useAuthStore = defineStore('auth', () => {
  const redirectUri = window.location.origin + '/oauth/callback'
  const authorizeUri = ref<string>('')
  const clientId = ref<string>('')

  const accessToken = ref<string | null>(localStorage.getItem('access_token'))
  const user = ref<Utilisateur | null>(null)
  const isAuthenticating = ref(false)

  const isAuthenticated = computed(() => !!accessToken.value)

  const login = () => {
    const state = generateRandomState()
    localStorage.setItem('oauth_state', state)

    const params = new URLSearchParams({
      response_type: 'code',
      client_id: clientId.value,
      redirect_uri: redirectUri,
      scope: 'read',
      state,
    })

    window.location.href = `${authorizeUri.value}?${params.toString()}`
  }

  const handleOAuthCallback = async (code: string, state: string): Promise<boolean> => {
    const storedState = localStorage.getItem('oauth_state')

    if (state !== storedState) {
      console.error('OAuth state mismatch')
      return false
    }

    localStorage.removeItem('oauth_state')

    try {
      isAuthenticating.value = true

      // Exchange code for access token via backend proxy
      const tokenRequest: OAuthTokenRequest = {
        code,
        state,
        redirectUri: redirectUri,
      }

      const tokenResponse = await authentificationApi.exchangeOAuthToken(tokenRequest)
      accessToken.value = tokenResponse.data.access_token
      localStorage.setItem('access_token', tokenResponse.data.access_token)

      // Fetch user info
      fetchUserInfo()

      return true
    } catch (error) {
      console.error('OAuth callback error:', error)
      return false
    } finally {
      isAuthenticating.value = false
    }
  }

  const fetchUserInfo = () => {
    if (!accessToken.value) return

    try {
      // Decode JWT to extract user info
      const payload = decodeJWT(accessToken.value)

      user.value = {
        id: payload.sub,
        username: payload.username,
        nickname: payload.nickname,
      }
    } catch (error) {
      console.error('Failed to decode JWT:', error)
      logout()
    }
  }

  const logout = () => {
    accessToken.value = null
    user.value = null
    localStorage.removeItem('access_token')
  }

  const initializeAuth = async () => {
    const configResponse = await configApi.getConfig()
    const frontConfig = configResponse.data
    authorizeUri.value = frontConfig.authorizeUri
    clientId.value = frontConfig.clientId

    if (accessToken.value) {
      // Check if token is expired
      if (isTokenExpired(accessToken.value)) {
        logout()
        return
      }
      fetchUserInfo()
    }
  }

  const isTokenExpired = (token: string): boolean => {
    try {
      const payload = decodeJWT(token)
      const currentTime = Math.floor(Date.now() / 1000)
      return payload.exp < currentTime
    } catch (error) {
      console.error('Error checking token expiration:', error)
      return true // Consider invalid tokens as expired
    }
  }

  const generateRandomState = (): string => {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
  }

  interface JWTPayload {
    exp: number
    sub: string
    username?: string
    nickname?: string
  }

  const decodeJWT = (token: string): JWTPayload => {
    try {
      // Split JWT into parts (header.payload.signature)
      const parts = token.split('.')
      if (parts.length !== 3) {
        throw new Error('Invalid JWT format')
      }

      // Decode payload (second part)
      const payload = parts[1]
      const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))

      return JSON.parse(decodedPayload)
    } catch (error) {
      console.error('JWT decode error:', error)
      throw new Error('Failed to decode JWT')
    }
  }

  return {
    accessToken: computed(() => accessToken.value),
    user: computed(() => user.value),
    isAuthenticated,
    isAuthenticating: computed(() => isAuthenticating.value),
    login,
    logout,
    handleOAuthCallback,
    initializeAuth,
  }
})
