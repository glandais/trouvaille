import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import OAuthCallbackView from '../views/OAuthCallbackView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/annonces',
    },
    {
      path: '/annonces',
      name: 'annonces',
      component: () => import('../views/AnnoncesView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/annonces/create',
      name: 'create-annonce',
      component: () => import('../views/CreateAnnonceView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/annonces/:id',
      name: 'annonce-detail',
      component: () => import('../views/AnnonceDetailView.vue'),
      props: true,
      meta: { requiresAuth: true },
    },
    {
      path: '/annonces/:id/edit',
      name: 'edit-annonce',
      component: () => import('../views/CreateAnnonceView.vue'),
      props: true,
      meta: { requiresAuth: true },
    },
    {
      path: '/my-annonces',
      name: 'my-annonces',
      component: () => import('../views/MyAnnoncesView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/oauth/callback',
      name: 'oauth-callback',
      component: OAuthCallbackView,
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
    },
  ],
})

// Navigation guard for protected routes
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    authStore.login()
    return
  }

  next()
})

export default router
