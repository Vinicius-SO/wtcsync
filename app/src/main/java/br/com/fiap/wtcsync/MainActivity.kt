package br.com.fiap.wtcsync

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import br.com.fiap.wtcsync.theme.WTCSYNCTheme
import br.com.fiap.wtcsync.ui.Navigation
import br.com.fiap.wtcsync.util.NotificationHelper

class MainActivity : ComponentActivity() {
    private var notificationTarget by androidx.compose.runtime.mutableStateOf<br.com.fiap.wtcsync.util.ChatNotificationTarget?>(null)


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationTarget = NotificationHelper.target(intent)
        NotificationHelper.createChannel(this)
        requestNotificationPermissionIfNeeded()
        setContent {
            WTCSYNCTheme {
                MainScreen(notificationTarget) {
                    notificationTarget = null
                    intent.removeExtra("chatRecipient")
                    intent.removeExtra("chatCustomerId")
                    intent.removeExtra("chatRequestId")
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationTarget = NotificationHelper.target(intent)
    }

    override fun onResume() {
        super.onResume()
        (application as WtcSyncApp).pushRegistration.refresh()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun MainScreen(target: br.com.fiap.wtcsync.util.ChatNotificationTarget? = null, onConsumed: () -> Unit = {}) {
    val navController = rememberNavController()
    Navigation(navController = navController, notificationTarget = target, onNotificationConsumed = onConsumed)
}

