const { get } = require('../../../../utils/request')
const { decorateOrder } = require('../../../../utils/util')

const TABS = [
  { label: '全部', status: '' },
  { label: '已接单', status: 'ACCEPTED' },
  { label: '已出发', status: 'DEPARTED' },
  { label: '服务中', status: 'SERVING' },
  { label: '待确认', status: 'PENDING_CONFIRM' },
  { label: '已完成', status: 'FINISHED' }
]

Page({
  data: {
    tabs: TABS,
    tabIndex: 0,
    orders: []
  },

  onShow() {
    this.load()
  },

  onPullDownRefresh() {
    this.load().then(() => wx.stopPullDownRefresh())
  },

  switchTab(e) {
    this.setData({ tabIndex: Number(e.currentTarget.dataset.i) })
    this.load()
  },

  load() {
    const status = this.data.tabs[this.data.tabIndex].status
    const query = status ? { status } : {}
    return get('/provider/orders', query).then(list => {
      this.setData({ orders: (list || []).map(decorateOrder) })
    })
  },

  goDetail(e) {
    wx.navigateTo({ url: '/subpackage-provider/pages/order/detail/detail?id=' + e.currentTarget.dataset.id })
  }
})
