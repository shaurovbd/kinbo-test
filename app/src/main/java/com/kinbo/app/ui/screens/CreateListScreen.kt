package com.kinbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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

private val emojis = listOf("🛒", "🥬", "🥩", "🧀", "🍞", "🐟", "🥫", "🧴", "🎉", "☕", "🥚", "🥛", "🌶️", "🍎", "🧊")

@Composable
fun CreateListScreen(vm: KinboViewModel, onCreated: (String) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🛒") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New List") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } },
            )
        },
    ) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner).padding(20.dp).background(MaterialTheme.colorScheme.background)) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 36.sp) }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("List name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("e.g. Weekly Groceries") },
            )
            Spacer(Modifier.height(20.dp))
            Text("Pick an icon", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(emojis) { e ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (e == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { emoji = e },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(e, fontSize = 22.sp)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    val n = if (name.isBlank()) "New List" else name.trim()
                    val list = vm.addList(n, emoji)
                    onCreated(list.id)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = true,
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create List", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
