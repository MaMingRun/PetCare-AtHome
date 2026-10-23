const { post } = require('../../../utils/request')

Page({
  data: {
    devCode: '',
    logging: false
  },

  onCodeInput(e) {
    this.setData({ devCode: e.detail.value.trim() })
  },

  // 开发登录：Mock 网关下 code 即身份标识，固定 code = 固定服务师账号
  login() {
    const code = this.data.devCode || 'dev-provider-1'
    if (this.data.logging) return
    this.setData({ logging: true })
    post('/provider/auth/login', { code }).then(vo => {
      if (vo.status === 'PENDING') {
        wx.showToast({ title: '账号待审核，请联系管理员', icon: 'none' })
        return
      }
      wx.setStorageSync('provider_token', vo.token)
      wx.setStorageSync('provider_name', vo.name || '')
      wx.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        wx.redirectTo({ url: '/subpackage-provider/pages/dispatch/pool/pool' })
      }, 600)
    }).finally(() => this.setData({ logging: false }))
  }
})
