import SwiftUI
import shared
import Combine

/**
 * iOS主ViewModel
 * 集成Unify KMP框架的状态管理
 */
class IOSMainViewModel: ObservableObject {
    @Published var users: [UserModel] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    @Published var isNetworkConnected: Bool = true
    
    private var cancellables = Set<AnyCancellable>()
    private let databaseRepository: UnifyDatabaseRepository
    private let networkService: UnifyNetworkService
    
    init() {
        // 初始化依赖项
        let driverFactory = IOSDatabaseDriverFactory()
        let database = DatabaseProvider.companion.createDatabase(driverFactory: driverFactory)
        self.databaseRepository = UnifyDatabaseRepository(database: database)
        
        let httpClient = NetworkClientFactory.companion.createHttpClient()
        self.networkService = UnifyNetworkServiceImpl(httpClient: httpClient)
        
        setupNetworkMonitoring()
    }
    
    func loadUsers() {
        isLoading = true
        errorMessage = nil
        
        // 使用KMP共享模块加载用户数据
        Task {
            do {
                let userList = try await databaseRepository.getAllUsers()
                
                await MainActor.run {
                    self.users = userList.map { user in
                        UserModel(
                            id: user.id,
                            username: user.username,
                            email: user.email,
                            displayName: user.displayName,
                            avatarUrl: user.avatarUrl
                        )
                    }
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.errorMessage = error.localizedDescription
                    self.isLoading = false
                }
            }
        }
    }
    
    private func setupNetworkMonitoring() {
        // 简单的网络状态监控
        // 在实际项目中应该使用Network框架
        Timer.publish(every: 5.0, on: .main, in: .common)
            .autoconnect()
            .sink { _ in
                // 模拟网络状态检查
                self.isNetworkConnected = true
            }
            .store(in: &cancellables)
    }
}

/**
 * 用户数据模型
 */
struct UserModel {
    let id: Int64
    let username: String
    let email: String
    let displayName: String
    let avatarUrl: String?
}

/**
 * iOS SQLite驱动工厂
 */
class IOSDatabaseDriverFactory: DatabaseDriverFactory {
    func createDriver() -> SqlDriver {
        return NativeSqliteDriver(
            schema: UnifyDatabase.companion.Schema,
            name: "unify.db"
        )
    }
}
