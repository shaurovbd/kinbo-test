package com.kinbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.model.ShoppingItem
import com.kinbo.app.ui.components.AvatarPile
import com.kinbo.app.ui.components.CategoryDot
import com.kinbo.app.ui.components.ProgressRing
import com.kinbo.app.ui.components.PurchasedCheck
import com.kinbo.app.ui.theme.CategoryColors

@Composable
fun ShoppingListScreen(
    vm: KinboViewModel,
    listId: String,
    onBack: () -> Unit,
    onAddItem: () -> Unit,
    onAI: () -> Unit,
    onScan: () -> Unit,
    onShare: (com.kinbo.app.model.ShoppingList) -> Unit,
    onCollab: () -> Unit,
) {
    val lists by vm.lists.collectAsState()
    val list = lists.firstOrNull { it.id == listId }
    var menuOpen by remember { mutableStateOf(false) }
    var renameOpen by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(list?.name.orEmpty()) }

    if (list == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("List not found"); }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(list.name, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = { vm.toggleFavorite(list.id) }) {
                        Icon(if (list.favorite) Icons.Rounded.Star else Icons.Rounded.StarBorder, contentDescription = "Favorite", tint = if (list.favorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { onShare(list) }) { Icon(Icons.Rounded.Share, contentDescription = "Share") }
                    IconButton(onClick = onCollab) { Icon(Icons.Rounded.Group, contentDescription = "Collaborators") }
                    IconButton(onClick = { menuOpen = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = "More") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("Rename") }, onClick = { menuOpen = false; renameOpen = true; renameText = list.name }, leadingIcon = { Icon(Icons.Rounded.Edit, null) })
                        DropdownMenuItem(text = { Text("Share list") }, onClick = { menuOpen = false; onShare(list) }, leadingIcon = { Icon(Icons.Rounded.Share, null) })
                        DropdownMenuItem(text = { Text("Collaborators") }, onClick = { menuOpen = false; onCollab() }, leadingIcon = { Icon(Icons.Rounded.Group, null) })
                        DropdownMenuItem(text = { Text("Duplicate") }, onClick = { menuOpen = false; vm.duplicateList(list.id) }, leadingIcon = { Icon(Icons.Rounded.ContentCopy, null) })
                        DropdownMenuItem(text = { Text("Sort by category") }, onClick = { menuOpen = false; vm.sortItemsByCategory(list.id) }, leadingIcon = { Icon(Icons.Rounded.Sort, null) })
                        DropdownMenuItem(text = { Text("Archive") }, onClick = { menuOpen = false; vm.archiveList(list.id); onBack() }, leadingIcon = { Icon(Icons.Rounded.Archive, null) })
                        DropdownMenuItem(text = { Text("Delete") }, colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error), onClick = { menuOpen = false; vm.deleteList(list.id); onBack() }, leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) })
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddItem, icon = { Icon(Icons.Rounded.Add, null) }, text = { Text("Add Item") })
        },
    ) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner).background(MaterialTheme.colorScheme.background)) {
            ListSummaryHeader(list)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(onClick = onAI, leadingIcon = { Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(16.dp)) }, label = { Text("AI Suggestions") })
                AssistChip(onClick = onScan, leadingIcon = { Icon(Icons.Rounded.DocumentScanner, null, modifier = Modifier.size(16.dp)) }, label = { Text("Scan") })
                AssistChip(onClick = {}, leadingIcon = { Icon(Icons.Rounded.PictureAsPdf, null, modifier = Modifier.size(16.dp)) }, label = { Text("PDF") })
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(list.items, key = { it.id }) { item ->
                    ItemRow(item = item, onToggle = { vm.togglePurchased(list.id, item.id) }, onDelete = { vm.removeItem(list.id, item.id) })
                }
            }
        }
    }

    if (renameOpen) {
        AlertDialog(
            onDismissRequest = { renameOpen = false },
            title = { Text("Rename list") },
            text = { OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true, label = { Text("Name") }) },
            confirmButton = { TextButton(onClick = { if (renameText.isNotBlank()) vm.renameList(list.id, renameText); renameOpen = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { renameOpen = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ListSummaryHeader(list: com.kinbo.app.model.ShoppingList) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(progress = list.progress, size = 60, stroke = 7)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${list.purchasedCount} of ${list.totalItems} purchased", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Estimated $${"%.2f".format(list.estimatedTotal)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                Spacer(Modifier.height(8.dp))
                AvatarPile(initials = list.collaborators.map { it.initials })
            }
        }
    }
}

@Composable
private fun ItemRow(item: ShoppingItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    val strikethrough = if (item.purchased) TextDecoration.LineThrough else TextDecoration.None
    val alpha = if (item.purchased) 0.5f else 1f
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PurchasedCheck(purchased = item.purchased, onClick = onToggle)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryDot(item.category)
                Spacer(Modifier.width(6.dp))
                Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, textDecoration = strikethrough, color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
            }
            Text(
                "${formatQty(item.quantity)} ${item.unit} · ${item.category.displayName} · $${"%.2f".format(item.price * item.quantity)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = strikethrough,
            )
            if (item.note.isNotBlank()) {
                Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatQty(q: Double): String = if (q == q.toLong().toDouble()) q.toLong().toString() else q.toString()
