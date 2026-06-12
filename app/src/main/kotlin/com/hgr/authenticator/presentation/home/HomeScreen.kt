package com.hgr.authenticator.presentation.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hgr.authenticator.R
import com.hgr.authenticator.domain.model.Account
import com.hgr.authenticator.presentation.common.ScreenScaffold
import com.hgr.authenticator.presentation.components.AccountCard
import com.hgr.authenticator.presentation.components.AnimatedFAB
import com.hgr.authenticator.presentation.components.AppBar
import com.hgr.authenticator.presentation.components.BottomSheetMenu
import com.hgr.authenticator.presentation.components.EmptyState
import com.hgr.authenticator.presentation.components.SearchBar
import com.hgr.authenticator.presentation.home.HomeContract.HomeEffect
import com.hgr.authenticator.presentation.home.HomeContract.HomeEvent

/**
 * 首页。
 *
 * 展示 2FA 账户列表，支持搜索、复制验证码、删除账户、查看详情。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddAccount: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val codeCopiedMsg = stringResource(R.string.code_copied)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is HomeEffect.NavigateToSettings -> onNavigateToSettings()
                is HomeEffect.NavigateToDateSettings -> {
                    context.startActivity(
                        Intent(Settings.ACTION_DATE_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
            }
        }
    }

    uiState.deleteConfirmAccountId?.let { accountId ->
        val account = uiState.accounts.find { it.id == accountId }
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(HomeEvent.OnDeleteDismissed) },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_message, account?.issuer ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(HomeEvent.OnDeleteConfirmed(accountId)) }
                ) {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(HomeEvent.OnDeleteDismissed) }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    uiState.detailAccountId?.let { accountId ->
        val account = uiState.accounts.find { it.id == accountId }
        if (account != null) {
            AccountDetailDialog(
                account = account,
                onDismiss = { viewModel.onEvent(HomeEvent.OnDismissDetail) }
            )
        }
    }

    if (uiState.showAboutDialog) {
        AboutDialog(onDismiss = { viewModel.onEvent(HomeEvent.OnDismissAbout) })
    }

    ScreenScaffold(
        topBar = {
            Column {
                AppBar(
                    title = stringResource(R.string.title_home),
                    onSearchClick = { viewModel.onEvent(HomeEvent.OnToggleSearch) },
                    onMoreClick = { viewModel.onEvent(HomeEvent.OnOpenMenu) }
                )
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onEvent(HomeEvent.OnSearchQueryChange(it)) },
                    visible = uiState.isSearchActive
                )
            }
        },
        floatingActionButton = {
            AnimatedFAB(
                onClick = onNavigateToAddAccount,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (uiState.hasTimeDrift) {
                    TimeDriftWarning(
                        onOpenSettings = {
                            viewModel.onEvent(HomeEvent.OnTimeDriftWarningClick)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                if (uiState.accounts.isEmpty() && !uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(visible = true)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.accounts,
                            key = { it.id }
                        ) { account ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.onEvent(HomeEvent.OnDeleteAccount(account.id))
                                    }
                                    false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 4.dp)
                                            .alpha(0.8f),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete),
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(end = 24.dp)
                                        )
                                    }
                                }
                            ) {
                                AccountCard(
                                    account = account,
                                    verificationCodeResult = uiState.verificationCodeResults[account.id],
                                    onCopy = {
                                        val code = uiState.verificationCodeResults[account.id]?.code
                                        code?.let {
                                            clipboardManager.setText(AnnotatedString(it.replace(" ", "")))
                                        }
                                        viewModel.onEvent(HomeEvent.OnCopyCode(account.id))
                                    },
                                    onLongClick = {
                                        viewModel.onEvent(HomeEvent.OnShowAccountDetail(account.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            BottomSheetMenu(
                visible = uiState.isMenuOpen,
                onDismiss = { viewModel.onEvent(HomeEvent.OnCloseMenu) },
                onSettings = {
                    viewModel.onEvent(HomeEvent.OnCloseMenu)
                    onNavigateToSettings()
                },
                onAbout = {
                    viewModel.onEvent(HomeEvent.OnCloseMenu)
                    viewModel.onEvent(HomeEvent.OnShowAbout)
                }
            )
        }

        uiState.copiedAccountId?.let { accountId ->
            LaunchedEffect(accountId) {
                snackbarHostState.showSnackbar(codeCopiedMsg, duration = SnackbarDuration.Short)
            }
        }
    }
}

@Composable
private fun AccountDetailDialog(
    account: Account,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = account.issuer,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                DetailRow(
                    label = stringResource(R.string.account_name),
                    value = account.issuer,
                    onCopy = { clipboardManager.setText(AnnotatedString(account.issuer)) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                DetailRow(
                    label = stringResource(R.string.username),
                    value = account.name,
                    onCopy = { clipboardManager.setText(AnnotatedString(account.name)) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                DetailRow(
                    label = stringResource(R.string.secret_key),
                    value = account.secret,
                    onCopy = { clipboardManager.setText(AnnotatedString(account.secret)) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(end = 32.dp)
                    .align(Alignment.CenterStart)
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy_s, label),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.about),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "A",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(
                        R.string.version_format,
                        stringResource(R.string.version_name)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.about_app_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.about_built_with),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun TimeDriftWarning(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.time_drift_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(R.string.time_drift_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    stringResource(R.string.time_drift_action),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
