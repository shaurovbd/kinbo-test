package com.kinbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinbo.app.R
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.ui.theme.ThemeMode
import com.kinbo.app.util.LocaleManager

@Composable
fun SettingsScreen(vm: KinboViewModel, onProfile: () -> Unit) {
    val themeMode by vm.themeMode.collectAsState()
    val premium by vm.premium.collectAsState()
    val user by vm.user.collectAsState()
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentLang = LocaleManager.getCurrentLanguage(context)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).clickable(onClick = onProfile).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                    Text(user.initials, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(user.plan, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SettingsGroup(stringResource(R.string.appearance)) {
                Text(stringResource(R.string.theme), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                ThemeMode.entries.forEach { mode ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { vm.setThemeMode(mode) }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(mode.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        RadioButton(selected = themeMode == mode, onClick = { vm.setThemeMode(mode) })
                    }
                }
            }
        }
        item {
            SettingsGroup(stringResource(R.string.subscription)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.kinbo_premium), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(if (premium) stringResource(R.string.active) else stringResource(R.string.premium_unlock), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = premium, onCheckedChange = { vm.setPremium(it) })
                }
            }
        }
        item {
            SettingsGroup(stringResource(R.string.preferences)) {
                ToggleRow(stringResource(R.string.smart_notifications), true)
                ToggleRow(stringResource(R.string.offline_mode), true)
                ToggleRow(stringResource(R.string.auto_sync), true)
                NavigationRow(stringResource(R.string.language), if (currentLang == "bn") stringResource(R.string.lang_bangla) else stringResource(R.string.lang_english)) {
                    showLanguageDialog = true
                }
                NavigationRow(stringResource(R.string.default_currency), "BDT ৳") {}
            }
        }
        item {
            SettingsGroup(stringResource(R.string.data)) {
                NavigationRow(stringResource(R.string.export_pdf), "") {}
                NavigationRow(stringResource(R.string.backup_cloud), "") {}
                NavigationRow(stringResource(R.string.clear_cache), "") {}
            }
        }
        item {
            SettingsGroup(stringResource(R.string.about)) {
                NavigationRow(stringResource(R.string.help_support), "") {}
                NavigationRow(stringResource(R.string.privacy_policy), "") {}
                NavigationRow(stringResource(R.string.rate_kinbo), "") {}
                Text(stringResource(R.string.version_mvp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    LocaleManager.supportedLanguages.forEach { (code, name) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                LocaleManager.setLanguage(context, code)
                                showLanguageDialog = false
                            }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            RadioButton(selected = currentLang == code, onClick = {
                                LocaleManager.setLanguage(context, code)
                                showLanguageDialog = false
                            })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        Card(shape = RoundedCornerShape(16.dp)) { Column(content = content) }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean) {
    var on by remember { mutableStateOf(checked) }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = on, onCheckedChange = { on = it })
    }
    HorizontalDivider()
}

@Composable
private fun NavigationRow(label: String, value: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (value.isNotBlank()) Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider()
}
