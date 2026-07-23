import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/book/list',
    component: () => import('@/views/book/BookList.vue'),
    meta: { title: '教材列表' }
  },
  {
    path: '/book/detail/:id',
    component: () => import('@/views/book/BookDetail.vue'),
    meta: { title: '教材详情' }
  },
  {
    path: '/book/publish',
    component: () => import('@/views/book/BookPublish.vue'),
    meta: { title: '发布教材', requiresAuth: true }
  },
  {
    path: '/cart',
    component: () => import('@/views/Cart.vue'),
    meta: { title: '暂存架', requiresAuth: true }
  },
  {
    path: '/order/checkout',
    component: () => import('@/views/order/OrderCheckout.vue'),
    meta: { title: '结算', requiresAuth: true }
  },
  {
    path: '/order/list',
    component: () => import('@/views/order/OrderList.vue'),
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/user/center',
    component: () => import('@/views/user/UserCenter.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/user/address',
    component: () => import('@/views/user/AddressManage.vue'),
    meta: { title: '地址管理', requiresAuth: true }
  },
  {
    path: '/admin/user',
    component: () => import('@/views/admin/UserManage.vue'),
    meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/book',
    component: () => import('@/views/admin/BookReview.vue'),
    meta: { title: '教材审核', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/order',
    component: () => import('@/views/admin/OrderManage.vue'),
    meta: { title: '订单管理', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/stats',
    component: () => import('@/views/admin/Stats.vue'),
    meta: { title: '数据统计', requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 智循书堂` : '智循书堂'
  
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.requiresAdmin && user.role !== 'admin') {
    next('/')
  } else {
    next()
  }
})

export default router
