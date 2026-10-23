const { get, post } = require('../../../../utils/request')
const { decorateOrder, fen2yuan, dayStatusText, entryModeText } = require('../../../../utils/util')

Page({
  data: {
    id: null,
    order: null,
    acting: false
  },

  onLoad(options) {
    this.setData({ id: options.id })
  },

  onShow() {
    if (this.data.id) this.load()
  },

  load() {
    return get('/provider/orders/' + this.data.id).then(o => {
      const order = decorateOrder(o)
      order.entryModeText = entryModeText(o.entryMode)
      order.dayViewList = (o.dayList || []).map(d => Object.assign({}, d, {
        dayStatusText: dayStatusText(d.status)
      }))
      this.setData({ order })
    })
  },

  // 履约四步：出发 → 到达打卡 → 开始 → 完成当天
  act(action) {
    if (this.data.acting) return
    if (action === 'arrive') {
      return this.arriveWithLocation()
    }
    this.setData({ acting: true })
    post('/provider/orders/' + this.data.id + '/' + action).then(() => {
      wx.showToast({ title: '操作成功', icon: 'success' })
      this.load()
    }).finally(() => this.setData({ acting: false }))
  },

  // 到达打卡：必须上传当前定位（后端校验与订单地址偏差 >500m 记告警）
  arriveWithLocation() {
    this.setData({ acting: true })
    wx.getLocation({
      type: 'gcj02',
      success: (loc) => {
        post('/provider/orders/' + this.data.id + '/arrive', {
          longitude: loc.longitude,
          latitude: loc.latitude
        }).then(() => {
          wx.showToast({ title: '已打卡', icon: 'success' })
          this.load()
        }).finally(() => this.setData({ acting: false }))
      },
      fail: () => {
        this.setData({ acting: false })
        wx.showToast({ title: '请授权定位后打卡', icon: 'none' })
      }
    })
  },

  // 服务过程留痕（NOTE 文字；PHOTO/VIDEO 需媒体上传服务，dev 下手填 URL）
  addNote() {
    wx.showModal({
      title: '写服务留痕',
      editable: true,
      placeholderText: '记录服务过程，如：已添粮换水',
      success: (r) => {
        if (!r.confirm || !r.content) return
        post('/provider/orders/' + this.data.id + '/records', {
          type: 'NOTE',
          remark: r.content
        }).then(() => wx.showToast({ title: '已留痕', icon: 'success' }))
      }
    })
  },

  goCredential() {
    wx.navigateTo({ url: '/subpackage-provider/pages/order/credential/credential?id=' + this.data.id })
  },

  goReport() {
    wx.navigateTo({ url: '/subpackage-provider/pages/order/report/report?id=' + this.data.id })
  }
})
