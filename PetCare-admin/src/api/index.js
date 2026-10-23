import request from '@/utils/request'

export const login = (username, password) =>
  request.post('/admin/auth/login', { username, password })

export const fetchDashboard = () => request.get('/admin/dashboard')

// ============ 订单管理 ============
export const fetchOrders = params => request.get('/admin/orders/page', { params })
export const fetchOrderDetail = id => request.get(`/admin/orders/${id}`)
export const fetchProviders = () => request.get('/admin/providers')
export const dispatchOrder = (id, providerId, remark = '') =>
  request.post(`/admin/orders/${id}/dispatch`, { providerId, remark })
export const markSettleAll = () => request.post('/admin/settlements/settle-all')

// 订单状态日志 + 入户凭证
export const fetchOrderStatusLogs = orderId => request.get(`/admin/orders/${orderId}/status-logs`)
export const fetchOrderCredentialLogs = orderId => request.get(`/admin/orders/${orderId}/credential-logs`)

// ============ 提现管理 ============
export const fetchWithdrawals = params => request.get('/admin/withdrawals/page', { params })
export const approveWithdrawal = id => request.post(`/admin/withdrawals/${id}/approve`)
export const rejectWithdrawal = (id, reason) =>
  request.post(`/admin/withdrawals/${id}/reject`, { reason })
export const markWithdrawalPaid = (id, transferNo) =>
  request.post(`/admin/withdrawals/${id}/paid`, { transferNo })

// ============ 服务师管理 ============
export const fetchProvidersPage = params => request.get('/admin/providers/page', { params })
export const fetchProviderDetail = id => request.get(`/admin/providers/${id}`)
export const updateProviderStatus = (id, status) =>
  request.post(`/admin/providers/${id}/status`, { status })

// ============ 钱包流水 ============
export const fetchWalletFlows = params => request.get('/admin/wallet/flows/page', { params })

// ============ 评价管理 ============
export const fetchReviews = params => request.get('/admin/reviews/page', { params })
export const fetchReviewStat = providerId =>
  request.get('/admin/reviews/stat', { params: providerId ? { providerId } : {} })

// ============ 用户管理 ============
export const fetchUsers = params => request.get('/admin/users/page', { params })
export const fetchUserDetail = id => request.get(`/admin/users/${id}`)
export const updateUserBlacklist = (id, blacklisted, reason = '') =>
  request.post(`/admin/users/${id}/blacklist`, { blacklisted, reason })

// ============ 服务项目管理 ============
export const fetchServices = params => request.get('/admin/services/page', { params })
export const fetchServiceDetail = id => request.get(`/admin/services/${id}`)
export const createService = data => request.post('/admin/services', data)
export const updateService = (id, data) => request.put(`/admin/services/${id}`, data)
export const updateServiceStatus = (id, status) =>
  request.post(`/admin/services/${id}/status`, { status })
