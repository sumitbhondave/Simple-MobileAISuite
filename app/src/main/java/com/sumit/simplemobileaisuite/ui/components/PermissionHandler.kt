package com.sumit.simplemobileaisuite.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

/**
 * A reusable composable to handle permission requests declaratively.
 *
 * @param modifier Optional modifier.
 * @param permission The permission to request (e.g., Manifest.permission.CAMERA).
 * @param onResult Callback invoked with the result of the permission request.
 * @param trigger A value that, when changed, triggers the permission request.
 *                Defaults to Unit to trigger only once when entering the composition.
 */
@Composable
fun PermissionHandler(
    modifier: Modifier = Modifier,
    permission: String,
    onResult: (Boolean) -> Unit,
    trigger: Any = Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )

    LaunchedEffect(trigger) {
        launcher.launch(permission)
    }
}
