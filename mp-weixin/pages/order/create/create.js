const { get, post } = require('../../../utils/request')
const { fen2yuan, speciesText, entryModeText } = require('../../../utils/util')

const TIME_SLOTS = ['09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00', '16:00-17:00', '19:00-20:00']

Page({
  data: {
    serviceId: null,
    svc: null,
    addresses: [],
    addressIndex: -1,
    pets: [],
    petChecked: {},      // petId -> bool
    startDate: '',
    days: 1,
    timeSlots: TIME_SLOTS,
    slotIndex: -1,
    distanceKm: '0',
    remark: '',
    preview: null,       // 试算结果
    submitting: false
  },

  onLoad(options) {
    if (!wx.getStorageSync('user_token')) {
      wx.showToast({ title: '请先到「我的」登录', icon: 'none' })
      setTimeout(() => wx.switchTab({ url: '/pages/user/center/center' }), 800)
      return
    }
    const today = new Date()
    const yyyy = today.getFullYear()
    const mm = String(today.getMonth() + 1).padStart(2, '0')
    const dd = String(today.getDate()).padStart(2, '0')
    this.setData({ serviceId: options.serviceId, startDate: `${yyyy}-${mm}-${dd}` })
    this.loadAll()
  },

  loadAll() {
    get('/user/services/' + this.data.serviceId).then(s => {
      this.setData({ svc: Object.assign({}, s, { priceYuan: fen2yuan(s.priceFrom) }) })
      wx.setNavigationBarTitle({ title: '预约·' + s.name })
    })
    get('/user/addresses').then(list => {
      const addresses = (list || []).map(a => Object.assign({}, a, {
        fullText: (a.city || '') + (a.district || '') + (a.community || '') + (a.detail || ''),
        entryModeText: entryModeText(a.entryMode)
      }))
      // 默认选中默认地址
      let idx = addresses.findIndex(a => a.isDefault === 1)
      if (idx < 0 && addresses.length > 0) idx = 0
      this.setData({ addresses, addressIndex: idx })
    })
    get('/user/pets').then(list => {
      const pets = (list || []).map(p => Object.assign({}, p, {
        speciesText: speciesText(p.species)
      }))
      this.setData({ pets })
    })
  },

  pickAddress(e) {
    this.setData({ addressIndex: Number(e.currentTarget.dataset.i) })
  },

  togglePet(e) {
    const id = e.currentTarget.dataset.id
    const key = 'petChecked.' + id
    this.setData({ [key]: !this.data.petChecked[id] }, () => this.refreshPreview())
  },

  pickDate(e) {
    this.setData({ startDate: e.detail.value })
  },

  changeDays(e) {
    const delta = Number(e.currentTarget.dataset.d)
    let days = this.data.days + delta
    if (days < 1) days = 1
    if (days > 30) days = 30
    this.setData({ days }, () => this.refreshPreview())
  },

  pickSlot(e) {
    this.setData({ slotIndex: Number(e.currentTarget.dataset.i) })
  },

  onDistanceInput(e) {
    this.setData({ distanceKm: e.detail.value }, () => this.refreshPreview())
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value })
  },

  selectedPetIds() {
    return this.data.pets.filter(p => this.data.petChecked[p.id]).map(p => p.id)
  },

  // 价格试算：宠物/天数/距离变化时触发（与下单共用同一服务端计价）
  refreshPreview() {
    const petIds = this.selectedPetIds()
    if (petIds.length === 0) {
      this.setData({ preview: null })
      return
    }
    post('/user/orders/price-preview', {
      serviceId: Number(this.data.serviceId),
      petIds,
      days: this.data.days,
      distanceKm: Number(this.data.distanceKm) || 0
    }).then(p => {
      this.setData({
        preview: Object.assign({}, p, {
          totalYuan: fen2yuan(p.totalAmount),
          itemList: (p.items || []).map(it => Object.assign({}, it, { amountYuan: fen2yuan(it.amount) }))
        })
      })
    }).catch(() => {})
  },

  submit() {
    if (this.data.submitting) return
    const addr = this.data.addresses[this.data.addressIndex]
    if (!addr) {
      wx.showToast({ title: '请选择服务地址', icon: 'none' })
      return
    }
    const petIds = this.selectedPetIds()
    if (petIds.length === 0) {
      wx.showToast({ title: '请选择宠物', icon: 'none' })
      return
    }
    if (this.data.slotIndex < 0) {
      wx.showToast({ title: '请选择服务时段', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    post('/user/orders', {
      serviceId: Number(this.data.serviceId),
      addressId: addr.id,
      petIds,
      startDate: this.data.startDate,
      days: this.data.days,
      timeSlot: this.data.timeSlots[this.data.slotIndex],
      remark: this.data.remark,
      distanceKm: Number(this.data.distanceKm) || 0
    }).then(vo => {
      wx.showToast({ title: '下单成功，请支付', icon: 'success' })
      setTimeout(() => {
        wx.redirectTo({ url: '/pages/order/detail/detail?id=' + vo.orderId })
      }, 600)
    }).finally(() => this.setData({ submitting: false }))
  }
})
