/**
 * HarmonyOS平台Hello World示例
 * 使用ArkTS开发，集成Unify KMP框架
 */

@Entry
@Component
struct Index {
  @State counter: number = 0
  @State currentLanguage: string = 'zh'
  @State showToast: boolean = false
  @State toastMessage: string = ''

  aboutToAppear() {
    // 初始化应用
    console.info('Unify KMP HarmonyOS应用启动')
  }

  build() {
    Column() {
      // 标题
      Text(this.getLocalizedString('app.name'))
        .fontSize(32)
        .fontWeight(FontWeight.Bold)
        .fontColor(Color.Blue)
        .margin({ bottom: 20 })

      // 欢迎卡片
      Column() {
        Text(this.getLocalizedString('hello.welcome'))
          .fontSize(24)
          .fontWeight(FontWeight.Medium)
          .margin({ bottom: 12 })

        Text(this.getLocalizedString('hello.description'))
          .fontSize(16)
          .fontColor(Color.Gray)
          .textAlign(TextAlign.Center)
          .margin({ bottom: 16 })
      }
      .width('90%')
      .padding(24)
      .backgroundColor(Color.Blue.opacity(0.1))
      .borderRadius(16)
      .margin({ bottom: 24 })

      // 计数器卡片
      Column() {
        Text(this.getLocalizedString('counter.title'))
          .fontSize(20)
          .fontWeight(FontWeight.Medium)
          .margin({ bottom: 16 })

        Text(this.counter.toString())
          .fontSize(48)
          .fontWeight(FontWeight.Bold)
          .fontColor(Color.Blue)
          .margin({ bottom: 20 })

        Row() {
          Button('-')
            .width(60)
            .height(40)
            .enabled(this.counter > 0)
            .onClick(() => {
              if (this.counter > 0) {
                this.counter--
              }
            })

          Button('+')
            .width(60)
            .height(40)
            .margin({ left: 16, right: 16 })
            .onClick(() => {
              this.counter++
              if (this.counter === 10) {
                this.showToastMessage('恭喜达到10次点击！')
              }
            })

          Button(this.getLocalizedString('common.reset'))
            .width(80)
            .height(40)
            .enabled(this.counter > 0)
            .onClick(() => {
              this.counter = 0
              this.showToastMessage('计数器已重置')
            })
        }
        .justifyContent(FlexAlign.Center)
      }
      .width('90%')
      .padding(24)
      .backgroundColor(Color.White)
      .borderRadius(16)
      .shadow({ radius: 8, color: Color.Gray.opacity(0.2) })
      .margin({ bottom: 24 })

      // 语言切换卡片
      Column() {
        Text(this.getLocalizedString('language.title'))
          .fontSize(18)
          .fontWeight(FontWeight.Medium)
          .margin({ bottom: 16 })

        Row() {
          Button('中文')
            .width(80)
            .height(36)
            .fontSize(14)
            .backgroundColor(this.currentLanguage === 'zh' ? Color.Blue : Color.Gray.opacity(0.3))
            .fontColor(this.currentLanguage === 'zh' ? Color.White : Color.Black)
            .onClick(() => {
              this.currentLanguage = 'zh'
            })

          Button('English')
            .width(80)
            .height(36)
            .fontSize(14)
            .margin({ left: 12, right: 12 })
            .backgroundColor(this.currentLanguage === 'en' ? Color.Blue : Color.Gray.opacity(0.3))
            .fontColor(this.currentLanguage === 'en' ? Color.White : Color.Black)
            .onClick(() => {
              this.currentLanguage = 'en'
            })

          Button('日本語')
            .width(80)
            .height(36)
            .fontSize(14)
            .backgroundColor(this.currentLanguage === 'ja' ? Color.Blue : Color.Gray.opacity(0.3))
            .fontColor(this.currentLanguage === 'ja' ? Color.White : Color.Black)
            .onClick(() => {
              this.currentLanguage = 'ja'
            })
        }
        .justifyContent(FlexAlign.Center)
      }
      .width('90%')
      .padding(24)
      .backgroundColor(Color.White)
      .borderRadius(16)
      .shadow({ radius: 8, color: Color.Gray.opacity(0.2) })
      .margin({ bottom: 24 })

      // 框架信息
      Text(this.getLocalizedString('framework.info'))
        .fontSize(12)
        .fontColor(Color.Gray)
        .margin({ top: 16 })
    }
    .width('100%')
    .height('100%')
    .padding(24)
    .justifyContent(FlexAlign.Start)
    .alignItems(HorizontalAlign.Center)
  }

  private getLocalizedString(key: string): string {
    const translations = {
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

    return translations[this.currentLanguage]?.[key] || key
  }

  private showToastMessage(message: string) {
    this.toastMessage = message
    this.showToast = true
    setTimeout(() => {
      this.showToast = false
    }, 2000)
  }
}
