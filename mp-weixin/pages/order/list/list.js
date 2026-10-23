const { get } = require('../../../utils/request')
const { decorateOrder } = require('../../../utils/util')

const TABS = [
  { label: '全部', status: '' },
  { label: '待支付', status: 'PENDING_PAY' },
  { label: '待接单', status: 'PENDING_ACCEPT' },
  { label: '服务中', status: 'SERVING' },
  { label: '待确认', status: 'PENDING_CONFIRM' },
  { label: '待评价', status: 'SETTLING' },
  { label: '已取消', status: 'CANCELLED' }
]

Page({
  data: {
    tabs: TABS,
    tabIndex: 0,
    orders: [],
    page: 1,
    size: 10,
    total: 0,
    loading: false
  },

  onShow() {
    if (!wx.getStorageSync('user_token')) {
      this.setData({ orders: [], total: 0 })
      return
    }
    this.reload()
  },

  onPullDownRefresh() {
    this.reload().then(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    if (this.data.orders.length < this.data.total) {
      this.load(this.data.page + 1)
    }
  },

  switchTab(e) {
    this.setData({ tabIndex: Number(e.currentTarget.dataset.i) })
    this.reload()
  },

  reload() {
    this.setData({ orders: [], page: 1, total: 0 })
    return this.load(1)
  },

  load(page) {
    if (this.data.loading) return Promise.resolve()
    this.setData({ loading: true })
    const status = this.data.tabs[this.data.tabIndex].status
    const query = { page, size: this.data.size }
    if (status) query.status = status
    return get('/user/orders', query).then(res => {
      const list = (res.list || []).map(decorateOrder)
      this.setData({
        orders: page === 1 ? list : this.data.orders.concat(list),
        page: res.page,
        total: res.total,
        loading: false
      })
    }).catch(() => this.setData({ loading: false }))
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/order/detail/detail?id=' + e.currentTarget.dataset.id })
  }
})
