import androidx.compose.ui.window.ComposeUIViewController
import com.unify.helloworld.HelloWorldApp

fun mainViewController() =
    ComposeUIViewController {
        HelloWorldApp()
    }
