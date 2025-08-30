import SwiftUI
import shared
import Combine

/**
 * iOS主ViewModel
 * 简化版本，与当前shared模块的HelloWorld应用保持一致
 */
class IOSMainViewModel: ObservableObject {
    @Published var platformInfo: String = ""
    @Published var deviceInfo: String = ""
    @Published var isLoading: Bool = false
    
    private var cancellables = Set<AnyCancellable>()
    
    init() {
        loadPlatformInfo()
    }
    
    func loadPlatformInfo() {
        isLoading = true
        
        // 使用shared模块的PlatformInfo
        platformInfo = PlatformInfo().getPlatformName()
        deviceInfo = PlatformInfo().getDeviceInfo()
        
        isLoading = false
    }
    
    func refreshInfo() {
        loadPlatformInfo()
    }
}
