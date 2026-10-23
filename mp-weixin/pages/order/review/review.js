const { post } = require('../../../utils/request')

const DIMS = [
  { key: 'scoreProfessional', label: '专业程度' },
  { key: 'scorePunctual', label: '准时到达' },
  { key: 'scoreCommunication', label: '沟通交流' },
  { key: 'scoreCare', label: '照顾宠物' }
]

Page({
  data: {
    id: null,
    dims: DIMS,
    scores: { scoreProfessional: 5, scorePunctual: 5, scoreCommunication: 5, scoreCare: 5 },
    content: '',
    anonymous: 0,
    submitting: false
  },

  onLoad(options) {
    this.setData({ id: options.id })
  },

  setScore(e) {
    const { key, v } = e.currentTarget.dataset
    this.setData({ ['scores.' + key]: v })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  toggleAnonymous(e) {
    this.setData({ anonymous: e.detail.value ? 1 : 0 })
  },

  submit() {
    if (this.data.submitting) return
    this.setData({ submitting: true })
    post('/user/orders/' + this.data.id + '/review', {
      scoreProfessional: this.data.scores.scoreProfessional,
      scorePunctual: this.data.scores.scorePunctual,
      scoreCommunication: this.data.scores.scoreCommunication,
      scoreCare: this.data.scores.scoreCare,
      content: this.data.content,
      anonymous: this.data.anonymous
    }).then(() => {
      wx.showToast({ title: '评价成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    }).finally(() => this.setData({ submitting: false }))
  }
})
