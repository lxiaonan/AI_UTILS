import { createRouter, createWebHashHistory } from 'vue-router';

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', name: 'home', component: () => import('@/views/SqlOptimizeView.vue') },
    { path: '/history', name: 'history', component: () => import('@/views/SqlHistoryView.vue') },
    { path: '/sql-detail', name: 'sql-detail', component: () => import('@/views/SqlDetailView.vue') },
    { path: '/image', name: 'image', component: () => import('@/views/ImageStudioView.vue') },
    { path: '/marketing', name: 'marketing', component: () => import('@/views/MarketingCopyView.vue') },
    { path: '/slogan', name: 'slogan', component: () => import('@/views/SloganStudioView.vue') },
    { path: '/marketing-history', name: 'marketing-history', component: () => import('@/views/MarketingHistoryView.vue') },
    { path: '/camera', name: 'camera', component: () => import('@/views/CameraControlView.vue') },
    { path: '/video', name: 'video', component: () => import('@/views/VideoStudioView.vue') },
    { path: '/voice', name: 'voice', component: () => import('@/views/VoiceStudioView.vue') },
    { path: '/assets', name: 'assets', component: () => import('@/views/AssetCenterView.vue') },
    { path: '/templates', name: 'templates', component: () => import('@/views/TemplateCenterView.vue') },
    { path: '/tasks', name: 'tasks', component: () => import('@/views/TaskCenterView.vue') },
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
});

export default router;
