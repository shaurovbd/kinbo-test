package com.kinbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.data.ShoppingAssistant
import com.kinbo.app.model.ItemCategory
import com.kinbo.app.model.Priority
import com.kinbo.app.model.ShoppingItem
import com.kinbo.app.ui.components.CategoryChip

@Composable
fun AddItemScreen(vm: KinboViewModel, listId: String, onDone: () -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("pcs") }
    var price by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ItemCategory.Other) }
    var priority by remember { mutableStateOf(Priority.Medium) }

    val lists by vm.lists.collectAsState()
    val list = lists.firstOrNull { it.id == listId }
    val suggestions = remember(name) { ShoppingAssistant.suggest(name, list?.items.orEmpty()) }
    val units = listOf("pcs", "kg", "g", "L", "ml", "pack", "box", "dozen")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Item") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } },
                actions = {
                    TextButton(onClick = {
                        if (name.isNotBlank()) {
                            val item = ShoppingItem(
                                name = name.trim(),
                                quantity = qty.toDoubleOrNull() ?: 1.0,
                                unit = unit,
                                price = price.toDoubleOrNull() ?: ShoppingAssistant.guessPrice(name),
                                note = note.trim(),
                                category = category,
                                priority = priority,
                            )
                            vm.addItem(listId, item)
                            onDone()
                        }
                    }) { Text("Save", fontWeight = FontWeight.SemiBold) }
                },
            )
        },
    ) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner).padding(16.dp).background(MaterialTheme.colorScheme.background)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), placeholder = { Text("e.g. Chicken") })
            if (suggestions.isNotEmpty() && name.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("AI suggests", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    items(suggestions.take(8)) { s ->
                        AssistChip(
                            onClick = { name = s.name; category = s.category; if (price.isBlank()) price = s.price.toString() },
                            label = { Text(s.name) },
                            leadingIcon = { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(14.dp)) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = qty, onValueChange = { qty = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Qty") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp))
                OutlinedTextField(value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Price") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), prefix = { Text("$") })
            }
            Spacer(Modifier.height(12.dp))
            Text("Unit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                items(units) { u ->
                    FilterChip(selected = u == unit, onClick = { unit = u }, label = { Text(u) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                ItemCategory.entries.forEach { c ->
                    FilterChip(selected = c == category, onClick = { category = c }, label = { Text(c.displayName) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Priority", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                Priority.entries.forEach { p ->
                    FilterChip(selected = p == priority, onClick = { priority = p }, label = { Text(p.name) })
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), maxLines = 3)
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(horizontalArrangement: Arrangement.Horizontal, modifier: Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = horizontalArrangement, modifier = modifier) { content() }
}
