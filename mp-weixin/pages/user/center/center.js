const { post } = require('../../../utils/request')

Page({
  data: {
    logged: false,
    nickname: '',
    devCode: '',   // dev 环境登录标识（Mock 网关：任意固定字符串 → 固定 openid）
    logging: false
  },

  onShow() {
    this.setData({
      logged: !!wx.getStorageSync('user_token'),
      nickname: wx.getStorageSync('user_name') || ''
    })
  },

  onCodeInput(e) {
    this.setData({ devCode: e.detail.value.trim() })
  },

  // 开发登录：Mock 微信网关下 code 即身份标识，固定 code = 固定账号
  // 生产环境替换为 wx.login() 拿真 code 即可
  login() {
    const code = this.data.devCode || 'dev-user-1'
    if (this.data.logging) return
    this.setData({ logging: true })
    post('/user/auth/login', { code }).then(vo => {
      wx.setStorageSync('user_token', vo.token)
      wx.setStorageSync('user_name', vo.name || '')
      this.setData({ logged: true, nickname: vo.name || '' })
      wx.showToast({ title: '登录成功', icon: 'success' })
    }).finally(() => this.setData({ logging: false }))
  },

  logout() {
    wx.removeStorageSync('user_token')
    wx.removeStorageSync('user_name')
    this.setData({ logged: false, nickname: '' })
  },

  goOrders() {
    wx.switchTab({ url: '/pages/order/list/list' })
  },

  goAddress() {
    wx.navigateTo({ url: '/pages/address/list/list' })
  },

  goPets() {
    wx.navigateTo({ url: '/pages/pet/list/list' })
  },

  // 服务师端入口（分包）
  goProvider() {
    wx.navigateTo({ url: '/subpackage-provider/pages/login/login' })
  }
})
