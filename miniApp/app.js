// 微信小程序应用入口
App({
  onLaunch() {
    console.log('Unify KMP 小程序启动')
    
    // 获取用户信息
    wx.getSetting({
      success: res => {
        if (res.authSetting['scope.userInfo']) {
          wx.getUserInfo({
            success: res => {
              console.log('用户信息获取成功', res.userInfo)
            }
          })
        }
      }
    })
  },

  onShow() {
    console.log('小程序显示')
  },

  onHide() {
    console.log('小程序隐藏')
  },

  onError(msg) {
    console.error('小程序错误:', msg)
  },

  globalData: {
    userInfo: null,
    version: '1.0.0'
  }
})
