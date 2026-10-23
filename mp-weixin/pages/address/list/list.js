const { get, del } = require('../../../utils/request')
const { entryModeText } = require('../../../utils/util')

Page({
  data: { addresses: [] },

  onShow() {
    get('/user/addresses').then(list => {
      this.setData({
        addresses: (list || []).map(a => Object.assign({}, a, {
          fullText: (a.city || '') + (a.district || '') + (a.community || '') + (a.detail || ''),
          entryModeText: entryModeText(a.entryMode)
        }))
      })
    })
  },

  add() {
    wx.navigateTo({ url: '/pages/address/edit/edit' })
  },

  edit(e) {
    wx.navigateTo({ url: '/pages/address/edit/edit?id=' + e.currentTarget.dataset.id })
  },

  del(e) {
    const id = e.currentTarget.dataset.id
    const addr = e.currentTarget.dataset.addr
    wx.showModal({
      title: '删除地址',
      content: `确定删除「${addr}」吗？`,
      success: (r) => {
        if (!r.confirm) return
        del('/user/addresses/' + id).then(() => {
          wx.showToast({ title: '已删除', icon: 'success' })
          this.onShow()
        })
      }
    })
  }
})
