import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import AdminLayout from '../views/admin/Layout.vue'
import Upload from '../views/admin/Upload.vue'
import Documents from '../views/admin/Documents.vue'
import Chat from '../views/admin/Chat.vue'
import Config from '../views/admin/Config.vue'
import Channel from '../views/admin/Channel.vue'
import KnowledgeBase from '../views/admin/KnowledgeBase.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    redirect: '/admin/upload'
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/upload',
    children: [
      {
        path: 'upload',
        name: 'Upload',
        component: Upload,
        meta: { requiresAuth: true }
      },
      {
        path: 'documents',
        name: 'Documents',
        component: Documents,
        meta: { requiresAuth: true }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: Chat,
        meta: { requiresAuth: true }
      },
      {
        path: 'config',
        name: 'Config',
        component: Config,
        meta: { requiresAuth: true }
      },
      {
        path: 'channel',
        name: 'Channel',
        component: Channel,
        meta: { requiresAuth: true }
      },
      {
        path: 'knowledge-base',
        name: 'KnowledgeBase',
        component: KnowledgeBase,
        meta: { requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('satoken')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/admin/upload')
  } else {
    next()
  }
})

export default router
