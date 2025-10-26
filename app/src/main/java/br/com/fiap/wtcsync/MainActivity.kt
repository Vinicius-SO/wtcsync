package br.com.fiap.wtcsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.fiap.wtcsync.theme.WTCSYNCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WTCSYNCTheme {
                Text(text = "WTC Sync App")
            }
        }
    }
}

// Você pode remover a função Greeting e o GreetingPreview,
// ou deixá-los para testar o Composable
@Preview(showBackground = true)
@Composable
fun AppPreview() {
    WTCSYNCTheme {
        Text(text = "WTC Sync App")
    }
}