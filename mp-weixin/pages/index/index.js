const { get } = require('../../utils/request')
const { fen2yuan } = require('../../utils/util')

Page({
  data: {
    services: [],
    categories: ['全部'],
    activeCategory: '全部',
    loading: false
  },

  onLoad() {
    this.loadServices()
  },

  onPullDownRefresh() {
    this.loadServices().then(() => wx.stopPullDownRefresh())
  },

  loadServices(category) {
    this.setData({ loading: true })
    const query = category && category !== '全部' ? { category } : {}
    return get('/user/services', query).then(list => {
      const services = (list || []).map(s => Object.assign({}, s, {
        priceYuan: fen2yuan(s.priceFrom),
        firstChar: (s.name || '').slice(0, 1),
        tagList: this.parseJsonArray(s.tags)
      }))
      // 汇总分类（首加载时）
      const categories = this.data.categories
      if (!category && categories.length === 1) {
        const set = new Set(['全部'])
        services.forEach(s => s.category && set.add(s.category))
        this.setData({ categories: Array.from(set) })
      }
      this.setData({ services, loading: false })
    }).catch(() => this.setData({ loading: false }))
  },

  parseJsonArray(str) {
    try { return JSON.parse(str || '[]') } catch (e) { return [] }
  },

  switchCategory(e) {
    const c = e.currentTarget.dataset.c
    this.setData({ activeCategory: c })
    this.loadServices(c)
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/service/detail/detail?id=' + e.currentTarget.dataset.id })
  }
})
