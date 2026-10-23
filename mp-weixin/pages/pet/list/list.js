const { get, del } = require('../../../utils/request')
const { speciesText } = require('../../../utils/util')

Page({
  data: { pets: [] },

  onShow() {
    get('/user/pets').then(list => {
      this.setData({
        pets: (list || []).map(p => Object.assign({}, p, {
          speciesText: speciesText(p.species),
          tagList: this.parseJsonArray(p.personalityTags)
        }))
      })
    })
  },

  parseJsonArray(str) {
    try { return JSON.parse(str || '[]') } catch (e) { return [] }
  },

  add() {
    wx.navigateTo({ url: '/pages/pet/edit/edit' })
  },

  edit(e) {
    wx.navigateTo({ url: '/pages/pet/edit/edit?id=' + e.currentTarget.dataset.id })
  },

  del(e) {
    const id = e.currentTarget.dataset.id
    const name = e.currentTarget.dataset.name
    wx.showModal({
      title: '删除宠物',
      content: `确定删除「${name}」吗？删除后无法恢复`,
      success: (r) => {
        if (!r.confirm) return
        del('/user/pets/' + id).then(() => {
          wx.showToast({ title: '已删除', icon: 'success' })
          this.onShow()
        })
      }
    })
  }
})
