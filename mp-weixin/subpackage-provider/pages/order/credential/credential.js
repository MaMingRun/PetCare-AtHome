const { get, post } = require('../../../../utils/request')
const { entryModeText } = require('../../../../utils/util')

Page({
  data: {
    id: null,
    credential: null,
    error: ''
  },

  onLoad(options) {
    this.setData({ id: options.id })
    this.load()
  },

  // 查看即留痕（后端写 VIEW 日志，用户端透明可查）
  load() {
    get('/provider/orders/' + this.data.id + '/credential').then(c => {
      this.setData({
        credential: Object.assign({}, c, { entryModeText: entryModeText(c.entryMode) }),
        error: ''
      })
    }).catch(err => {
      this.setData({ error: (err && err.msg) || '当前状态不可查看凭证' })
    })
  },

  copySecret() {
    if (!this.data.credential || !this.data.credential.secret) return
    wx.setClipboardData({ data: this.data.credential.secret })
  },

  // 模拟开锁（写 UNLOCK 日志）
  unlock() {
    wx.showModal({
      title: '使用凭证',
      content: '将记录一次开锁使用日志，用户端可见',
      success: (r) => {
        if (!r.confirm) return
        post('/provider/orders/' + this.data.id + '/credential/unlock').then(() => {
          wx.showToast({ title: '已记录使用', icon: 'success' })
        })
      }
    })
  }
})
