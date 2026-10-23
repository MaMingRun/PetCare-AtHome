const { post } = require('../../../../utils/request')

Page({
  data: {
    amount: '',       // 元
    bankCardId: '',   // dev 手填银行卡ID
    submitting: false
  },

  onAmountInput(e) {
    this.setData({ amount: e.detail.value })
  },

  onCardInput(e) {
    this.setData({ bankCardId: e.detail.value })
  },

  submit() {
    if (this.data.submitting) return
    const yuan = Number(this.data.amount)
    if (!yuan || yuan <= 0) {
      wx.showToast({ title: '请输入提现金额', icon: 'none' })
      return
    }
    if (!this.data.bankCardId) {
      wx.showToast({ title: '请输入银行卡ID', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    // 后端金额单位统一为分
    post('/provider/withdraw', {
      amount: Math.round(yuan * 100),
      bankCardId: Number(this.data.bankCardId)
    }).then(() => {
      wx.showToast({ title: '提现申请已提交', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    }).finally(() => this.setData({ submitting: false }))
  }
})
