const { get, post, put } = require('../../../utils/request')

const ENTRY = ['智能锁', '物业开门', '钥匙寄存', '主人在家']
const ENTRY_KEY = ['SMART_LOCK', 'PROPERTY', 'KEY_DEPOSIT', 'AT_HOME']

Page({
  data: {
    id: null,
    isEdit: false,
    entryList: ENTRY,
    entryIndex: 0,
    isDefault: false,
    form: {
      contactName: '',
      contactPhone: '',
      city: '',
      district: '',
      community: '',
      detail: ''
    }
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id, isEdit: true })
      wx.setNavigationBarTitle({ title: '编辑地址' })
      this.load()
    } else {
      wx.setNavigationBarTitle({ title: '添加地址' })
    }
  },

  load() {
    get('/user/addresses').then(list => {
      const a = (list || []).find(x => String(x.id) === String(this.data.id))
      if (!a) {
        wx.showToast({ title: '地址不存在', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 800)
        return
      }
      const idx = Math.max(0, ENTRY_KEY.indexOf(a.entryMode || 'AT_HOME'))
      this.setData({
        entryIndex: idx,
        isDefault: a.isDefault === 1,
        'form.contactName': a.contactName || '',
        'form.contactPhone': a.contactPhone || '',
        'form.city': a.city || '',
        'form.district': a.district || '',
        'form.community': a.community || '',
        'form.detail': a.detail || ''
      })
    })
  },

  onInput(e) {
    const { key } = e.currentTarget.dataset
    this.setData({ ['form.' + key]: e.detail.value })
  },

  onEntry(e) {
    this.setData({ entryIndex: Number(e.detail.value) })
  },

  onDefault(e) {
    this.setData({ isDefault: e.detail.value })
  },

  submit() {
    const f = this.data.form
    if (!f.contactName.trim()) return wx.showToast({ title: '请填写联系人', icon: 'none' })
    if (!f.contactPhone.trim()) return wx.showToast({ title: '请填写手机号', icon: 'none' })
    if (!f.city.trim() || !f.district.trim()) return wx.showToast({ title: '请填写省市', icon: 'none' })

    const body = {
      contactName: f.contactName.trim(),
      contactPhone: f.contactPhone.trim(),
      city: f.city.trim(),
      district: f.district.trim(),
      community: f.community.trim(),
      detail: f.detail.trim(),
      entryMode: ENTRY_KEY[this.data.entryIndex],
      isDefault: this.data.isDefault ? 1 : 0
    }
    const req = this.data.isEdit
      ? put('/user/addresses/' + this.data.id, body)
      : post('/user/addresses', body)
    req.then(() => {
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    })
  }
})
