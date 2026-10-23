const { get, post } = require('../../../../utils/request')
const { fen2yuan, entryModeText, fmtTime } = require('../../../../utils/util')

Page({
  data: {
    dispatches: [],
    loading: false
  },

  onShow() {
    if (!wx.getStorageSync('provider_token')) {
      wx.redirectTo({ url: '/subpackage-provider/pages/login/login' })
      return
    }
    this.load()
  },

  onPullDownRefresh() {
    this.load().then(() => wx.stopPullDownRefresh())
  },

  load() {
    this.setData({ loading: true })
    return get('/provider/dispatches').then(list => {
      this.setData({
        dispatches: (list || []).map(d => Object.assign({}, d, {
          payYuan: fen2yuan(d.payAmount),
          entryModeText: entryModeText(d.entryMode),
          deadlineText: fmtTime(d.responseDeadline)
        })),
        loading: false
      })
    }).catch(() => this.setData({ loading: false }))
  },

  accept(e) {
    const orderId = e.currentTarget.dataset.id
    wx.showModal({
      title: '接单确认',
      content: '接单后请按时上门服务',
      success: (r) => {
        if (!r.confirm) return
        post('/provider/orders/' + orderId + '/accept').then(() => {
          wx.showToast({ title: '已接单', icon: 'success' })
          this.load()
        })
      }
    })
  },

  reject(e) {
    const orderId = e.currentTarget.dataset.id
    wx.showModal({
      title: '拒单原因',
      editable: true,
      placeholderText: '请填写拒单备注',
      success: (r) => {
        if (!r.confirm) return
        post('/provider/orders/' + orderId + '/reject', { remark: r.content || '服务师拒单' }).then(() => {
          wx.showToast({ title: '已拒单', icon: 'success' })
          this.load()
        })
      }
    })
  },

  goMine() {
    wx.navigateTo({ url: '/subpackage-provider/pages/order/list/list' })
  },

  goWallet() {
    wx.navigateTo({ url: '/subpackage-provider/pages/wallet/index/index' })
  }
})
