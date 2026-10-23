// 统一请求封装：按路径自动选择三端令牌
//   /user/**     → 请求头 authentication，令牌存 user_token
//   /provider/** → 请求头 provider-token，令牌存 provider_token
function pickAuth(url) {
  if (url.indexOf('/provider/') === 0) {
    return { header: 'provider-token', key: 'provider_token' }
  }
  return { header: 'authentication', key: 'user_token' }
}

function request(options) {
  const app = getApp()
  const auth = pickAuth(options.url)
  const token = wx.getStorageSync(auth.key)
  const header = Object.assign({ 'Content-Type': 'application/json' }, options.header || {})
  if (token) {
    header[auth.header] = token
  }
  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success(res) {
        if (res.statusCode === 401) {
          wx.removeStorageSync(auth.key)
          wx.showToast({ title: '请先登录', icon: 'none' })
          reject(res)
          return
        }
        const body = res.data || {}
        if (body.code === 1) {
          resolve(body.data)
        } else {
          wx.showToast({ title: body.msg || '请求失败', icon: 'none' })
          reject(body)
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常，请检查后端是否启动', icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = {
  get: (url, data) => request({ url, method: 'GET', data }),
  post: (url, data) => request({ url, method: 'POST', data }),
  put: (url, data) => request({ url, method: 'PUT', data }),
  del: (url, data) => request({ url, method: 'DELETE', data })
}
