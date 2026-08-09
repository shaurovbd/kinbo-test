package com.kinbo.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.model.Collaborator
import com.kinbo.app.model.ListRole

@Composable
fun CollaboratorScreen(
    vm: KinboViewModel,
    listId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lists by vm.lists.collectAsState()
    val list = lists.firstOrNull { it.id == listId }
    val isSynced by remember { mutableStateOf(vm.isSynced) }

    var joinCode by remember { mutableStateOf("") }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { snackbarHostState.showSnackbar(it); snackbarMsg = null }
    }

    if (list == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("List not found") }
        return
    }

    val shareCode = remember(list.shareCode) { if (list.shareCode.isBlank()) vm.generateShareCode(list.id) else list.shareCode }
    val inviteLink = "https://kinbo.app/invite?code=$shareCode"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collaborators") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Sync status banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSynced) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isSynced) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                            null,
                            tint = if (isSynced) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (isSynced) "Real-time sync active" else "Local mode — not synced",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSynced) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                if (isSynced) "Changes sync instantly to all collaborators." else "Add Firebase to enable live collaboration (see README).",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSynced) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Invite section
            item {
                Text("Invite people", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Share code", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        SelectionContainer {
                            Text(shareCode, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Join my Kinbo shopping list! Code: $shareCode\n$inviteLink")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Invite to ${list.name}").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                },
                                modifier = Modifier.weight(1f),
                            ) { Icon(Icons.Rounded.Share, null); Spacer(Modifier.width(6.dp)); Text("Send invite") }
                            OutlinedButton(onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Kinbo invite code", shareCode))
                                snackbarMsg = "Code copied"
                            }) { Icon(Icons.Rounded.ContentCopy, null); Spacer(Modifier.width(6.dp)); Text("Copy") }
                        }
                    }
                }
            }

            // Join by code
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Join a list", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = joinCode,
                            onValueChange = { joinCode = it.uppercase().take(6) },
                            label = { Text("Enter 6-digit code") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, capitalization = KeyboardCapitalization.Characters),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (joinCode.length == 6) {
                                    val joined = vm.joinListByShareCode(joinCode)
                                    snackbarMsg = joined?.let { "Joined: ${it.name}" } ?: "Looking up list…"
                                    joinCode = ""
                                } else {
                                    snackbarMsg = "Enter the full 6-digit code"
                                }
                            },
                            enabled = joinCode.length == 6,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Icon(Icons.Rounded.Login, null); Spacer(Modifier.width(6.dp)); Text("Join list") }
                    }
                }
            }

            // Members list
            item {
                Text("Members (${list.collaborators.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
            }

            items(list.collaborators, key = { it.userId.ifEmpty { it.name } }) { member ->
                MemberRow(
                    member = member,
                    isOwner = member.role == ListRole.Owner,
                    onRoleChange = { newRole -> vm.updateCollaboratorRole(list.id, member.userId, newRole) },
                    onRemove = { vm.removeCollaborator(list.id, member.userId); snackbarMsg = "Removed ${member.name}" },
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MemberRow(
    member: Collaborator,
    isOwner: Boolean,
    onRoleChange: (ListRole) -> Unit,
    onRemove: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error,
    )
    val color = palette[member.colorIndex % palette.size]

    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(color),
                contentAlignment = Alignment.Center,
            ) {
                Text(member.initials, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(member.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(member.email.ifBlank { member.role.name }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box {
                AssistChip(
                    onClick = { if (!isOwner) menuOpen = true },
                    label = { Text(member.role.name) },
                    leadingIcon = {
                        Icon(
                            when (member.role) {
                                ListRole.Owner -> Icons.Rounded.Star
                                ListRole.Editor -> Icons.Rounded.Edit
                                ListRole.Viewer -> Icons.Rounded.Visibility
                            }, null, Modifier.size(14.dp)
                        )
                    },
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Editor — can edit items") }, onClick = { menuOpen = false; onRoleChange(ListRole.Editor) })
                    DropdownMenuItem(text = { Text("Viewer — read only") }, onClick = { menuOpen = false; onRoleChange(ListRole.Viewer) })
                }
            }
            if (!isOwner) {
                IconButton(onClick = onRemove) { Icon(Icons.Rounded.PersonRemove, null, tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
