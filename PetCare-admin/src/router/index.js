import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard',    component: () => import('@/views/Dashboard.vue'),     meta: { title: '仪表盘' } },
      { path: 'orders',       component: () => import('@/views/OrderList.vue'),     meta: { title: '订单管理' } },
      { path: 'orders/:id',   component: () => import('@/views/OrderDetail.vue'),   meta: { title: '订单详情' } },
      { path: 'providers',    component: () => import('@/views/ProviderList.vue'),  meta: { title: '服务师管理' } },
      { path: 'users',        component: () => import('@/views/UserList.vue'),      meta: { title: '用户管理' } },
      { path: 'services',     component: () => import('@/views/ServiceList.vue'),   meta: { title: '服务项目' } },
      { path: 'reviews',      component: () => import('@/views/ReviewList.vue'),    meta: { title: '评价管理' } },
      { path: 'wallet-flows', component: () => import('@/views/WalletFlowList.vue'), meta: { title: '钱包流水' } },
      { path: 'withdrawals',  component: () => import('@/views/WithdrawalList.vue'), meta: { title: '提现管理' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.meta.public) return next()
  if (!token) return next({ path: '/login', query: { redirect: to.fullPath } })
  next()
})

export default router
