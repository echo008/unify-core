import SwiftUI
import shared

struct ContentView: View {
    @State private var counter = 0
    @State private var currentLanguage: Locale = .chinese
    @State private var showingToast = false
    @State private var toastMessage = ""
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // 标题
                    Text(getLocalizedString("app.name"))
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.blue)
                    
                    // 欢迎卡片
                    VStack(spacing: 16) {
                        Text(getLocalizedString("hello.welcome"))
                            .font(.title2)
                            .fontWeight(.medium)
                        
                        Text(getLocalizedString("hello.description"))
                            .font(.body)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .padding(24)
                    .background(Color.blue.opacity(0.1))
                    .cornerRadius(16)
                    
                    // 计数器卡片
                    VStack(spacing: 16) {
                        Text(getLocalizedString("counter.title"))
                            .font(.headline)
                        
                        Text("\(counter)")
                            .font(.system(size: 48, weight: .bold))
                            .foregroundColor(.blue)
                        
                        HStack(spacing: 16) {
                            Button("-") {
                                if counter > 0 {
                                    counter -= 1
                                }
                            }
                            .buttonStyle(.bordered)
                            .disabled(counter == 0)
                            
                            Button("+") {
                                counter += 1
                                if counter == 10 {
                                    showToast("恭喜达到10次点击！")
                                }
                            }
                            .buttonStyle(.borderedProminent)
                            
                            Button(getLocalizedString("common.reset")) {
                                counter = 0
                                showToast("计数器已重置")
                            }
                            .buttonStyle(.bordered)
                            .disabled(counter == 0)
                        }
                    }
                    .padding(24)
                    .background(Color(.systemBackground))
                    .cornerRadius(16)
                    .shadow(radius: 2)
                    
                    // 语言切换卡片
                    VStack(spacing: 16) {
                        Text(getLocalizedString("language.title"))
                            .font(.headline)
                        
                        HStack(spacing: 12) {
                            LanguageButton(
                                text: "中文",
                                isSelected: currentLanguage == .chinese,
                                action: { currentLanguage = .chinese }
                            )
                            
                            LanguageButton(
                                text: "English",
                                isSelected: currentLanguage == .english,
                                action: { currentLanguage = .english }
                            )
                            
                            LanguageButton(
                                text: "日本語",
                                isSelected: currentLanguage == .japanese,
                                action: { currentLanguage = .japanese }
                            )
                        }
                    }
                    .padding(24)
                    .background(Color(.systemBackground))
                    .cornerRadius(16)
                    .shadow(radius: 2)
                    
                    // 框架信息
                    Text(getLocalizedString("framework.info"))
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .padding(.top, 16)
                }
                .padding(24)
            }
            .navigationTitle("Unify KMP")
            .navigationBarTitleDisplayMode(.inline)
        }
        .toast(message: toastMessage, isShowing: $showingToast)
    }
    
    private func getLocalizedString(_ key: String) -> String {
        switch currentLanguage {
        case .chinese:
            return getChineseTranslation(key)
        case .english:
            return getEnglishTranslation(key)
        case .japanese:
            return getJapaneseTranslation(key)
        }
    }
    
    private func showToast(_ message: String) {
        toastMessage = message
        showingToast = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            showingToast = false
        }
    }
}

enum Locale {
    case chinese, english, japanese
}

struct LanguageButton: View {
    let text: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(text) {
            action()
        }
        .buttonStyle(isSelected ? .borderedProminent : .bordered)
        .font(.caption)
    }
}

// Toast 扩展
extension View {
    func toast(message: String, isShowing: Binding<Bool>) -> some View {
        self.overlay(
            VStack {
                if isShowing.wrappedValue {
                    Text(message)
                        .padding()
                        .background(Color.black.opacity(0.8))
                        .foregroundColor(.white)
                        .cornerRadius(8)
                        .transition(.opacity)
                }
                Spacer()
            }
            .animation(.easeInOut, value: isShowing.wrappedValue)
        )
    }
}

// 翻译函数
private func getChineseTranslation(_ key: String) -> String {
    switch key {
    case "app.name": return "Unify KMP"
    case "hello.welcome": return "欢迎使用 Unify KMP！"
    case "hello.description": return "这是一个跨平台开发框架示例"
    case "counter.title": return "计数器演示"
    case "language.title": return "语言切换"
    case "framework.info": return "基于 Kotlin Multiplatform 构建"
    case "common.reset": return "重置"
    default: return key
    }
}

private func getEnglishTranslation(_ key: String) -> String {
    switch key {
    case "app.name": return "Unify KMP"
    case "hello.welcome": return "Welcome to Unify KMP!"
    case "hello.description": return "This is a cross-platform development framework example"
    case "counter.title": return "Counter Demo"
    case "language.title": return "Language Switch"
    case "framework.info": return "Built with Kotlin Multiplatform"
    case "common.reset": return "Reset"
    default: return key
    }
}

private func getJapaneseTranslation(_ key: String) -> String {
    switch key {
    case "app.name": return "Unify KMP"
    case "hello.welcome": return "Unify KMP へようこそ！"
    case "hello.description": return "これはクロスプラットフォーム開発フレームワークの例です"
    case "counter.title": return "カウンターデモ"
    case "language.title": return "言語切り替え"
    case "framework.info": return "Kotlin Multiplatform で構築"
    case "common.reset": return "リセット"
    default: return key
    }
}
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("用户列表")
                .font(.headline)
            
            if isLoading {
                HStack {
                    Spacer()
                    ProgressView()
                        .scaleEffect(1.2)
                    Spacer()
                }
                .frame(height: 100)
            } else if let errorMessage = errorMessage {
                ErrorView(message: errorMessage, onRetry: onRefresh)
            } else if users.isEmpty {
                EmptyUserListView(onRefresh: onRefresh)
            } else {
                LazyVStack(spacing: 8) {
                    ForEach(users, id: \.id) { user in
                        UserItemView(user: user)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct UserItemView: View {
    let user: UserModel
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(user.displayName)
                .font(.headline)
            Text(user.email)
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(Color(.systemGray6))
        )
    }
}

struct EmptyUserListView: View {
    let onRefresh: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            Text("暂无用户数据")
                .font(.body)
                .foregroundColor(.secondary)
            
            Button("刷新", action: onRefresh)
                .buttonStyle(.bordered)
        }
        .frame(maxWidth: .infinity)
        .padding()
    }
}

struct ErrorView: View {
    let message: String
    let onRetry: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            Text("加载失败: \(message)")
                .font(.body)
                .foregroundColor(.red)
                .multilineTextAlignment(.center)
            
            Button("重试", action: onRetry)
                .buttonStyle(.bordered)
        }
        .frame(maxWidth: .infinity)
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
