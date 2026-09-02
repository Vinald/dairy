package vinald.me.dairy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import vinald.me.dairy.ui.DairyApp
import vinald.me.dairy.ui.theme.DairyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DairyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DairyApp()
                }
            }
        }
    }
}
