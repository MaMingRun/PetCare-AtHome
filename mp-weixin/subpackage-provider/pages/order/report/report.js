const { post } = require('../../../../utils/request')

const EATING = ['正常', '偏少', '拒食']
const DRINKING = ['正常', '偏少']
const EXCRETION = ['正常', '软便', '未排便', '异常']
const SPIRIT = ['活泼', '正常', '萎靡']

Page({
  data: {
    id: null,
    eatingOptions: EATING,
    drinkingOptions: DRINKING,
    excretionOptions: EXCRETION,
    spiritOptions: SPIRIT,
    form: { eating: '正常', drinking: '正常', excretion: '正常', spirit: '正常' },
    summary: '',
    submitting: false
  },

  onLoad(options) {
    this.setData({ id: options.id })
  },

  pick(e) {
    const { key, v } = e.currentTarget.dataset
    this.setData({ ['form.' + key]: v })
  },

  onSummaryInput(e) {
    this.setData({ summary: e.detail.value })
  },

  submit() {
    if (this.data.submitting) return
    if (!this.data.summary.trim()) {
      wx.showToast({ title: '请填写服务总结', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    post('/provider/orders/' + this.data.id + '/report', {
      eating: this.data.form.eating,
      drinking: this.data.form.drinking,
      excretion: this.data.form.excretion,
      spirit: this.data.form.spirit,
      summary: this.data.summary
    }).then(() => {
      wx.showToast({ title: '报告已提交', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    }).finally(() => this.setData({ submitting: false }))
  }
})
