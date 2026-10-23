const { get, post, put } = require('../../../utils/request')

const SPECIES = ['猫', '狗', '其他']
const SPECIES_KEY = ['CAT', 'DOG', 'OTHER']
const GENDER = ['公', '母', '未知']
const GENDER_KEY = ['MALE', 'FEMALE', 'UNKNOWN']

Page({
  data: {
    id: null,
    isEdit: false,
    speciesList: SPECIES,
    speciesIndex: 0,
    genderList: GENDER,
    genderIndex: 0,
    form: {
      name: '',
      breed: '',
      birthday: '',
      weightKg: '',
      sterilized: 0,
      allergies: '',
      chronicDisease: '',
      feedingInstruction: ''
    }
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id, isEdit: true })
      wx.setNavigationBarTitle({ title: '编辑宠物' })
      this.load()
    } else {
      wx.setNavigationBarTitle({ title: '添加宠物' })
    }
  },

  load() {
    // 列表页的数据在 onShow 已加载，这里复用：通过 get('/user/pets') 查一次找到自己
    get('/user/pets').then(list => {
      const p = (list || []).find(x => String(x.id) === String(this.data.id))
      if (!p) {
        wx.showToast({ title: '宠物不存在', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 800)
        return
      }
      const speciesIdx = Math.max(0, SPECIES_KEY.indexOf(p.species || 'CAT'))
      const genderIdx = Math.max(0, GENDER_KEY.indexOf(p.gender || 'UNKNOWN'))
      this.setData({
        speciesIndex: speciesIdx,
        genderIndex: genderIdx,
        'form.name': p.name || '',
        'form.breed': p.breed || '',
        'form.birthday': p.birthday || '',
        'form.weightKg': p.weightKg != null ? String(p.weightKg) : '',
        'form.sterilized': p.sterilized || 0,
        'form.allergies': p.allergies || '',
        'form.chronicDisease': p.chronicDisease || '',
        'form.feedingInstruction': p.feedingInstruction || ''
      })
    })
  },

  onInput(e) {
    const { key } = e.currentTarget.dataset
    this.setData({ ['form.' + key]: e.detail.value })
  },

  onSpecies(e) {
    this.setData({ speciesIndex: Number(e.detail.value) })
  },

  onGender(e) {
    this.setData({ genderIndex: Number(e.detail.value) })
  },

  onBirthday(e) {
    this.setData({ 'form.birthday': e.detail.value })
  },

  onSterilized(e) {
    this.setData({ 'form.sterilized': e.detail.value ? 1 : 0 })
  },

  submit() {
    const f = this.data.form
    if (!f.name.trim()) {
      wx.showToast({ title: '请填写名字', icon: 'none' })
      return
    }
    const body = {
      name: f.name.trim(),
      species: SPECIES_KEY[this.data.speciesIndex],
      gender: GENDER_KEY[this.data.genderIndex],
      breed: f.breed.trim(),
      birthday: f.birthday || null,
      weightKg: f.weightKg ? Number(f.weightKg) : null,
      sterilized: f.sterilized,
      allergies: f.allergies.trim(),
      chronicDisease: f.chronicDisease.trim(),
      feedingInstruction: f.feedingInstruction.trim()
    }
    const req = this.data.isEdit
      ? put('/user/pets/' + this.data.id, body)
      : post('/user/pets', body)
    req.then(() => {
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    })
  }
})
