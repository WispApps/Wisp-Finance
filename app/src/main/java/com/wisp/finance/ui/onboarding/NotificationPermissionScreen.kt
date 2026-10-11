package com.wisp.finance.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R

@Composable
fun NotificationPermissionScreen(
    onFinished: () -> Unit,
    preview: Boolean = false,
    onClosePreview: () -> Unit = {},
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onFinished() }
    OnboardingLayout(
        step = 4,
        title = stringResource(R.string.onboarding_notifications_title),
        subtitle = stringResource(R.string.setup_notifications_description),
        preview = preview, onClosePreview = onClosePreview,
        actions = {
            SetupButton(stringResource(R.string.onboarding_notifications_allow), onClick = {
                if (!preview && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else onFinished()
            })
            TextButton(onClick = onFinished, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.onboarding_notifications_not_now))
            }
        },
    ) {
        item {
            Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Icon(painterResource(R.drawable.ic_setup_notifications), contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(28.dp).size(64.dp))
                }
            }
        }
        item {
            Text(stringResource(R.string.onboarding_notifications_note), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
