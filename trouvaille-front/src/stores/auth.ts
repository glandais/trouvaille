import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface User {
  id: string
  pseudo: string
  avatar?: string
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem('access_token'))
  const user = ref<User | null>(null)
  const isAuthenticating = ref(false)

  const isAuthenticated = computed(() => !!accessToken.value)

  // OAuth2 configuration from CLAUDE.md
  const OAUTH_CONFIG = {
    authorizeUri: 'https://chat.n-peloton.fr/oauth/authorize',
    tokenUri: 'https://chat.n-peloton.fr/oauth/access_token',
    userInfoUri: 'https://chat.n-peloton.fr/api/v4/users/me',
    clientId: 'trouvaille',
    redirectUri: window.location.origin + '/oauth/callback',
    scope: 'read'
  }

  const login = () => {
    const state = generateRandomState()
    localStorage.setItem('oauth_state', state)
    
    const params = new URLSearchParams({
      response_type: 'code',
      client_id: OAUTH_CONFIG.clientId,
      redirect_uri: OAUTH_CONFIG.redirectUri,
      scope: OAUTH_CONFIG.scope,
      state
    })

    window.location.href = `${OAUTH_CONFIG.authorizeUri}?${params.toString()}`
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

      // Exchange code for access token
      const tokenResponse = await fetch(OAUTH_CONFIG.tokenUri, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'Accept': 'application/json'
        },
        body: new URLSearchParams({
          grant_type: 'authorization_code',
          client_id: OAUTH_CONFIG.clientId,
          redirect_uri: OAUTH_CONFIG.redirectUri,
          code
        })
      })

      if (!tokenResponse.ok) {
        throw new Error('Failed to exchange code for token')
      }

      const tokenData = await tokenResponse.json()
      accessToken.value = tokenData.access_token
      localStorage.setItem('access_token', tokenData.access_token)

      // Fetch user info
      await fetchUserInfo()
      
      return true
    } catch (error) {
      console.error('OAuth callback error:', error)
      return false
    } finally {
      isAuthenticating.value = false
    }
  }

  const fetchUserInfo = async () => {
    if (!accessToken.value) return

    try {
      const response = await fetch(OAUTH_CONFIG.userInfoUri, {
        headers: {
          Authorization: `Bearer ${accessToken.value}`
        }
      })

      if (!response.ok) {
        throw new Error('Failed to fetch user info')
      }

      const userData = await response.json()
      user.value = {
        id: userData.id,
        pseudo: userData.username,
        avatar: userData.avatar
      }
    } catch (error) {
      console.error('Failed to fetch user info:', error)
      logout()
    }
  }

  const logout = () => {
    accessToken.value = null
    user.value = null
    localStorage.removeItem('access_token')
  }

  const initializeAuth = async () => {
    if (accessToken.value) {
      isAuthenticating.value = true
      await fetchUserInfo()
      isAuthenticating.value = false
    }
  }

  const generateRandomState = (): string => {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
  }

  return {
    accessToken: computed(() => accessToken.value),
    user: computed(() => user.value),
    isAuthenticated,
    isAuthenticating: computed(() => isAuthenticating.value),
    login,
    logout,
    handleOAuthCallback,
    initializeAuth
  }
})