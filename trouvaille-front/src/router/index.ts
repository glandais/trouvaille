import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import OAuthCallbackView from '../views/OAuthCallbackView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
    {
      path: '/annonces',
      name: 'annonces',
      component: () => import('../views/AnnoncesView.vue')
    },
    {
      path: '/annonces/create',
      name: 'create-annonce',
      component: () => import('../views/CreateAnnonceView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/annonces/:id',
      name: 'annonce-detail',
      component: () => import('../views/AnnonceDetailView.vue'),
      props: true
    },
    {
      path: '/my-annonces',
      name: 'my-annonces',
      component: () => import('../views/MyAnnoncesView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/oauth/callback',
      name: 'oauth-callback',
      component: OAuthCallbackView
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue')
    }
  ]
})

// Navigation guard for protected routes
router.beforeEach((to, from, next) => {
  console.log('Router beforeEach:', {
    to: to.fullPath,
    from: from.fullPath,
    name: to.name
  })
  
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    console.log('Redirecting to login for protected route')
    authStore.login()
    return
  }
  
  next()
})

// Add debug for route changes
router.afterEach((to, from) => {
  console.log('Router afterEach:', {
    to: to.fullPath,
    from: from.fullPath,
    name: to.name
  })
})

export default router