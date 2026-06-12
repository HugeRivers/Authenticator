package com.hgr.authenticator.presentation.addaccount

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hgr.authenticator.R
import com.hgr.authenticator.presentation.addaccount.AddAccountContract.AddAccountEffect
import com.hgr.authenticator.presentation.addaccount.AddAccountContract.AddAccountEvent
import com.hgr.authenticator.presentation.addaccount.AddAccountContract.Tab
import com.hgr.authenticator.presentation.common.ScreenScaffold
import com.hgr.authenticator.presentation.components.AnimatedTabBar
import com.hgr.authenticator.presentation.components.AppBar
import com.hgr.authenticator.presentation.components.QRScannerView

/**
 * 添加账户页。
 *
 * 支持扫描二维码和手动输入密钥两种方式添加 2FA 账户。
 */
@Composable
fun AddAccountScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddAccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val tabs = listOf(stringResource(R.string.scan_qr), stringResource(R.string.manual_entry))
    val selectedTabIndex = when (uiState.selectedTab) {
        Tab.Scan -> 0
        Tab.Manual -> 1
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddAccountEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is AddAccountEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    val cameraPermission = Manifest.permission.CAMERA
    val checkPermission = {
        ContextCompat.checkSelfPermission(context, cameraPermission) ==
                PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(uiState.selectedTab) {
        if (uiState.selectedTab == Tab.Scan) {
            viewModel.onEvent(AddAccountEvent.OnCameraPermissionResult(checkPermission()))
        }
    }

    ScreenScaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.title_add_account),
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedTabBar(
                selectedTab = selectedTabIndex,
                onTabSelected = { index ->
                    viewModel.onEvent(
                        AddAccountEvent.OnTabSelected(
                            if (index == 0) Tab.Scan else Tab.Manual
                        )
                    )
                },
                tabs = tabs,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) togetherWith
                                slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(200))
                    } else {
                        slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) togetherWith
                                slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200))
                    }
                },
                label = "tabContent",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    0 -> QRScannerSection(
                        scanState = uiState.scanState,
                        hasPermission = uiState.hasCameraPermission,
                        onPermissionResult = { granted ->
                            viewModel.onEvent(AddAccountEvent.OnCameraPermissionResult(granted))
                        },
                        onRequestPermission = {
                            viewModel.onEvent(AddAccountEvent.OnCameraPermissionRequested)
                        },
                        onQrCodeDetected = { qrData ->
                            viewModel.onEvent(AddAccountEvent.OnQrCodeDetected(qrData))
                        },
                        onDismissError = {
                            viewModel.onEvent(AddAccountEvent.OnDismissError)
                        }
                    )
                    1 -> ManualEntrySection(
                        accountName = uiState.accountName,
                        secretKey = uiState.secretKey,
                        userName = uiState.userName,
                        isLoading = uiState.isLoading,
                        onAccountNameChange = {
                            viewModel.onEvent(AddAccountEvent.OnAccountNameChange(it))
                        },
                        onSecretKeyChange = {
                            viewModel.onEvent(AddAccountEvent.OnSecretKeyChange(it))
                        },
                        onUserNameChange = {
                            viewModel.onEvent(AddAccountEvent.OnUserNameChange(it))
                        },
                        onAddAccount = {
                            viewModel.onEvent(AddAccountEvent.OnAddAccount)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QRScannerSection(
    scanState: AddAccountContract.ScanState,
    hasPermission: Boolean,
    onPermissionResult: (Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    onQrCodeDetected: (String) -> Unit,
    onDismissError: () -> Unit
) {
    QRScannerView(
        scanState = scanState,
        hasPermission = hasPermission,
        onPermissionResult = onPermissionResult,
        onRequestPermission = onRequestPermission,
        onQrCodeDetected = onQrCodeDetected,
        onDismissError = onDismissError,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ManualEntrySection(
    accountName: String,
    secretKey: String,
    userName: String,
    isLoading: Boolean,
    onAccountNameChange: (String) -> Unit,
    onSecretKeyChange: (String) -> Unit,
    onUserNameChange: (String) -> Unit,
    onAddAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = accountName,
            onValueChange = onAccountNameChange,
            label = { Text(stringResource(R.string.account_name)) },
            placeholder = { Text(stringResource(R.string.account_name_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = secretKey,
            onValueChange = onSecretKeyChange,
            label = { Text(stringResource(R.string.secret_key)) },
            placeholder = { Text(stringResource(R.string.secret_key_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(R.string.secret_key_hint_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            label = { Text(stringResource(R.string.username)) },
            placeholder = { Text(stringResource(R.string.username_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddAccount,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.add_account))
            }
        }
    }
}
