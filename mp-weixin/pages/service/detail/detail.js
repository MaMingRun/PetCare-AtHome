const { get } = require('../../../utils/request')
const { fen2yuan } = require('../../../utils/util')

Page({
  data: {
    id: null,
    svc: null
  },

  onLoad(options) {
    this.setData({ id: options.id })
    this.load()
  },

  load() {
    get('/user/services/' + this.data.id).then(s => {
      this.setData({
        svc: Object.assign({}, s, {
          priceYuan: fen2yuan(s.priceFrom),
          firstChar: (s.name || '').slice(0, 1),
          tagList: this.parseJsonArray(s.tags),
          contentList: this.parseJsonArray(s.contentItems),
          ruleList: (s.priceRules || []).map(r => Object.assign({}, r, {
            amountYuan: fen2yuan(r.amount)
          }))
        })
      })
      wx.setNavigationBarTitle({ title: s.name })
    })
  },

  parseJsonArray(str) {
    try { return JSON.parse(str || '[]') } catch (e) { return [] }
  },

  goCreate() {
    wx.navigateTo({ url: '/pages/order/create/create?serviceId=' + this.data.id })
  }
})
