import androidx.compose.ui.window.ComposeUIViewController
import com.unify.helloworld.HelloWorldApp
import com.unify.helloworld.PlatformInfo

fun MainViewController() = ComposeUIViewController {
    HelloWorldApp(
        platformName = PlatformInfo.getPlatformName()
    )
}
