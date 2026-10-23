const { get } = require('../../../../utils/request')
const { fen2yuan, fmtTime } = require('../../../../utils/util')

const BIZ_TYPE = {
  INCOME_PENDING: '待结算入账',
  INCOME_SETTLED: '结算入账',
  WITHDRAW_APPLY: '提现申请',
  WITHDRAW_REJECT: '提现驳回',
  WITHDRAW_PAID: '提现打款'
}

Page({
  data: {
    wallet: null,
    flows: []
  },

  onShow() {
    this.load()
  },

  onPullDownRefresh() {
    this.load().then(() => wx.stopPullDownRefresh())
  },

  load() {
    const p1 = get('/provider/wallet').then(w => {
      this.setData({
        wallet: Object.assign({}, w, {
          balanceYuan: fen2yuan(w.balance),
          pendingYuan: fen2yuan(w.pendingSettlement),
          totalYuan: fen2yuan(w.totalIncome)
        })
      })
    })
    const p2 = get('/provider/wallet/flows').then(list => {
      this.setData({
        flows: (list || []).map(f => Object.assign({}, f, {
          amountYuan: (f.amount >= 0 ? '+' : '') + fen2yuan(f.amount),
          bizTypeText: BIZ_TYPE[f.bizType] || f.bizType,
          createdAtText: fmtTime(f.createdAt)
        }))
      })
    })
    return Promise.all([p1, p2])
  },

  goWithdraw() {
    wx.navigateTo({ url: '/subpackage-provider/pages/wallet/withdraw/withdraw' })
  }
})
