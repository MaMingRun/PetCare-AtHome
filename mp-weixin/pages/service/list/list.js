const { get } = require('../../../utils/request')
const { fen2yuan } = require('../../../utils/util')

Page({
  data: {
    services: [],
    category: ''
  },

  onLoad(options) {
    const category = options.category ? decodeURIComponent(options.category) : ''
    this.setData({ category })
    if (category) wx.setNavigationBarTitle({ title: category })
    this.load()
  },

  load() {
    const query = this.data.category ? { category: this.data.category } : {}
    get('/user/services', query).then(list => {
      this.setData({
        services: (list || []).map(s => Object.assign({}, s, {
          priceYuan: fen2yuan(s.priceFrom),
          firstChar: (s.name || '').slice(0, 1),
          tagList: this.parseJsonArray(s.tags)
        }))
      })
    })
  },

  parseJsonArray(str) {
    try { return JSON.parse(str || '[]') } catch (e) { return [] }
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/service/detail/detail?id=' + e.currentTarget.dataset.id })
  }
})
