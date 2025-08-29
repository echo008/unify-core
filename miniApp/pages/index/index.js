// 微信小程序Hello World示例逻辑
Page({
  data: {
    counter: 0,
    currentLanguage: 'zh',
    translations: {
      'zh': {
        'app.name': 'Unify KMP',
        'hello.welcome': '欢迎使用 Unify KMP！',
        'hello.description': '这是一个跨平台开发框架示例',
        'counter.title': '计数器演示',
        'language.title': '语言切换',
        'framework.info': '基于 Kotlin Multiplatform 构建',
        'common.reset': '重置'
      },
      'en': {
        'app.name': 'Unify KMP',
        'hello.welcome': 'Welcome to Unify KMP!',
        'hello.description': 'This is a cross-platform development framework example',
        'counter.title': 'Counter Demo',
        'language.title': 'Language Switch',
        'framework.info': 'Built with Kotlin Multiplatform',
        'common.reset': 'Reset'
      },
      'ja': {
        'app.name': 'Unify KMP',
        'hello.welcome': 'Unify KMP へようこそ！',
        'hello.description': 'これはクロスプラットフォーム開発フレームワークの例です',
        'counter.title': 'カウンターデモ',
        'language.title': '言語切り替え',
        'framework.info': 'Kotlin Multiplatform で構築',
        'common.reset': 'リセット'
      }
    }
  },

  onLoad() {
    console.log('Unify KMP 小程序启动')
    // 获取系统语言设置
    const systemInfo = wx.getSystemInfoSync()
    const language = systemInfo.language
    if (language.startsWith('en')) {
      this.setData({ currentLanguage: 'en' })
    } else if (language.startsWith('ja')) {
      this.setData({ currentLanguage: 'ja' })
    }
  },

  // 增加计数器
  incrementCounter() {
    const newCounter = this.data.counter + 1
    this.setData({ counter: newCounter })
    
    if (newCounter === 10) {
      wx.showToast({
        title: '恭喜达到10次点击！',
        icon: 'success',
        duration: 2000
      })
    }
  },

  // 减少计数器
  decrementCounter() {
    if (this.data.counter > 0) {
      this.setData({ counter: this.data.counter - 1 })
    }
  },

  // 重置计数器
  resetCounter() {
    this.setData({ counter: 0 })
    wx.showToast({
      title: '计数器已重置',
      icon: 'success',
      duration: 1500
    })
  },

  // 切换语言
  switchLanguage(e) {
    const lang = e.currentTarget.dataset.lang
    this.setData({ currentLanguage: lang })
    
    wx.showToast({
      title: '语言已切换',
      icon: 'success',
      duration: 1000
    })
  },

  // 分享功能
  onShareAppMessage() {
    return {
      title: 'Unify KMP 跨平台框架示例',
      path: '/pages/index/index'
    }
  },

  // 分享到朋友圈
  onShareTimeline() {
    return {
      title: 'Unify KMP 跨平台开发框架'
    }
  }
})
