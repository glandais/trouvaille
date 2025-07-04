import { createRouter, createWebHistory } from 'vue-router'

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
    },
    {
      path: '/annonces/create',
      name: 'create-annonce',
      component: () => import('../views/CreateAnnonceView.vue'),
    },
    {
      path: '/annonces/:id',
      name: 'annonce-detail',
      component: () => import('../views/AnnonceDetailView.vue'),
      props: true,
    },
    {
      path: '/annonces/:id/edit',
      name: 'edit-annonce',
      component: () => import('../views/CreateAnnonceView.vue'),
      props: true,
    },
    {
      path: '/my-annonces',
      name: 'my-annonces',
      component: () => import('../views/MyAnnoncesView.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
    },
  ],
})

export default router
