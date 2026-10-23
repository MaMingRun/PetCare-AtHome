const { get, post } = require('../../../utils/request')
const { decorateOrder, fen2yuan, dayStatusText, entryModeText, fmtTime } = require('../../../utils/util')

Page({
  data: {
    id: null,
    order: null,
    credentialLogs: [],
    showLogs: false
  },

  onLoad(options) {
    this.setData({ id: options.id })
  },

  onShow() {
    if (this.data.id) this.load()
  },

  load() {
    return get('/user/orders/' + this.data.id).then(o => {
      const order = decorateOrder(o)
      order.entryModeText = entryModeText(o.entryMode)
      order.baseYuan = fen2yuan(o.baseAmount)
      order.extraYuan = fen2yuan(o.extraAmount)
      order.discountYuan = fen2yuan(o.discountAmount)
      order.cancelTimeText = fmtTime(o.cancelTime)
      order.dayViewList = (o.dayList || []).map(d => Object.assign({}, d, {
        dayStatusText: dayStatusText(d.status)
      }))
      this.setData({ order })
    })
  },

  // 支付（Mock：调完即成功）
  pay() {
    wx.showModal({
      title: '模拟支付',
      content: '开发环境为 Mock 支付，确认后直接支付成功',
      success: (r) => {
        if (!r.confirm) return
        post('/user/orders/' + this.data.id + '/pay').then(() => {
          wx.showToast({ title: '支付成功', icon: 'success' })
          this.load()
        })
      }
    })
  },

  cancel() {
    wx.showModal({
      title: '取消订单',
      content: '确定取消该订单吗？',
      success: (r) => {
        if (!r.confirm) return
        post('/user/orders/' + this.data.id + '/cancel').then(() => {
          wx.showToast({ title: '已取消', icon: 'success' })
          this.load()
        })
      }
    })
  },

  confirm() {
    wx.showModal({
      title: '确认收货',
      content: '确认服务已完成吗？',
      success: (r) => {
        if (!r.confirm) return
        post('/user/orders/' + this.data.id + '/confirm').then(() => {
          wx.showToast({ title: '已确认', icon: 'success' })
          this.load()
        })
      }
    })
  },

  goReview() {
    wx.navigateTo({ url: '/pages/order/review/review?id=' + this.data.id })
  },

  // 透明可查：入户凭证操作日志
  toggleLogs() {
    const show = !this.data.showLogs
    this.setData({ showLogs: show })
    if (show && this.data.credentialLogs.length === 0) {
      get('/user/orders/' + this.data.id + '/credential-logs').then(list => {
        this.setData({
          credentialLogs: (list || []).map(l => Object.assign({}, l, {
            createdAtText: fmtTime(l.createdAt),
            actionText: l.action === 'UNLOCK' ? '使用凭证开锁' : '查看凭证'
          }))
        })
      })
    }
  }
})
