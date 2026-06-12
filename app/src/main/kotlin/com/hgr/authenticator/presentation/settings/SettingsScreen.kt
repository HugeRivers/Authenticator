package com.hgr.authenticator.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hgr.authenticator.R
import com.hgr.authenticator.domain.model.ThemeMode
import com.hgr.authenticator.presentation.common.ScreenScaffold
import com.hgr.authenticator.presentation.components.AppBar
import com.hgr.authenticator.presentation.components.RadioOption
import com.hgr.authenticator.presentation.components.ToggleSwitch
import com.hgr.authenticator.presentation.settings.SettingsContract.SettingsEvent
import com.hgr.authenticator.presentation.theme.LightAccent
import com.hgr.authenticator.presentation.theme.LightAccentOn

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    ScreenScaffold(
        topBar = {
            AppBar(
                title = stringResource(R.string.title_settings),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsGroup(title = stringResource(R.string.appearance)) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        RadioOption(
                            selected = uiState.settings.themeMode == ThemeMode.System,
                            onSelect = {
                                viewModel.onEvent(SettingsEvent.OnThemeSelected(ThemeMode.System))
                            },
                            label = stringResource(R.string.follow_system)
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        RadioOption(
                            selected = uiState.settings.themeMode == ThemeMode.Light,
                            onSelect = {
                                viewModel.onEvent(SettingsEvent.OnThemeSelected(ThemeMode.Light))
                            },
                            label = stringResource(R.string.light_mode)
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        RadioOption(
                            selected = uiState.settings.themeMode == ThemeMode.Dark,
                            onSelect = {
                                viewModel.onEvent(SettingsEvent.OnThemeSelected(ThemeMode.Dark))
                            },
                            label = stringResource(R.string.dark_mode)
                        )
                    }
                }
            }

            SettingsGroup(title = stringResource(R.string.security)) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(LightAccent, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = LightAccentOn,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.biometric_lock),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.biometric_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        ToggleSwitch(
                            checked = uiState.settings.biometricEnabled,
                            onCheckedChange = {
                                viewModel.onEvent(SettingsEvent.OnBiometricToggle(it))
                            }
                        )
                    }
                }
            }

            SettingsGroup(title = stringResource(R.string.data_migration)) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    SettingsRow(
                        label = stringResource(R.string.export_accounts),
                        value = stringResource(R.string.export_desc),
                        onClick = onNavigateToExport,
                        showArrow = true
                    )
                }
            }

            SettingsGroup(title = stringResource(R.string.about)) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsRow(
                            label = stringResource(R.string.version),
                            value = stringResource(R.string.version_name),
                            onClick = {}
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRow(
                            label = stringResource(R.string.help_feedback),
                            value = null,
                            onClick = {},
                            showArrow = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.app_name) + " v" + stringResource(R.string.version_name) + "\n" + stringResource(R.string.footer_built_with),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String?,
    onClick: () -> Unit,
    showArrow: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (showArrow) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
