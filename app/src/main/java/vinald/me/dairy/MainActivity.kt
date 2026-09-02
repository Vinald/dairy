package vinald.me.dairy

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import vinald.me.dairy.ui.DairyApp
import vinald.me.dairy.ui.theme.DairyTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // When app lock is on, keep diary content out of screenshots and the recents preview.
        val container = (application as DiaryApplication).container
        val pinManager = container.pinManager
        pinManager.hasPin
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { hasPin ->
                if (hasPin) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
            .launchIn(lifecycleScope)

        enableEdgeToEdge()
        setContent {
            val dynamicColor by container.appPreferences.dynamicColor
                .collectAsStateWithLifecycle(initialValue = true)
            DairyTheme(dynamicColor = dynamicColor) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DairyApp()
                }
            }
        }
    }
}
