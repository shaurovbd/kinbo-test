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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.data.ShoppingAssistant
import com.kinbo.app.model.ShoppingItem
import com.kinbo.app.util.CurrencyFormatter

private data class ChatMsg(val text: String, val fromUser: Boolean, val suggestions: List<ShoppingItem> = emptyList())

@Composable
fun AiAssistantScreen(vm: KinboViewModel, onBack: () -> Unit) {
    val lists by vm.lists.collectAsState()
    val allItems = lists.flatMap { it.items }
    var input by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMsg>(ChatMsg("Hi! I'm your Kinbo shopping assistant 🤖 Tell me what you're cooking or planning to buy, and I'll suggest groceries you might be missing.", false)) }

    fun send(text: String) {
        if (text.isBlank()) return
        messages.add(ChatMsg(text, true))
        val suggestions = ShoppingAssistant.suggest(text, allItems)
        val recipes = ShoppingAssistant.recipeIdeas(allItems + suggestions)
        val dupes = ShoppingAssistant.detectDuplicates(allItems)
        val bill = ShoppingAssistant.estimateBill(allItems + suggestions)
        val reply = buildString {
            if (suggestions.isNotEmpty()) {
                append("Based on \"$text\", you might also need:\n")
                suggestions.take(6).forEach { append("• ${it.name} (${it.category.displayName})\n") }
            } else {
                append("Here are some seasonal picks for you.\n")
                ShoppingAssistant.seasonalRecommendations().take(4).forEach { append("• ${it.name}\n") }
            }
            append("\n💡 Recipe ideas: ${recipes.joinToString(", ")}")
            if (dupes.isNotEmpty()) append("\n⚠️ Duplicate items detected: ${dupes.joinToString(", ")}")
            append("\n💰 Estimated total bill: ${CurrencyFormatter.format(bill)}")
        }
        messages.add(ChatMsg(reply, false, suggestions))
        input = ""
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("AI Assistant") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } }) },
    ) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner).background(MaterialTheme.colorScheme.background)) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(messages) { m -> MessageBubble(m, onAddSuggestion = { s -> /* would add to selected list */ }) }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Try 'Chicken' or 'Pasta'") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
                IconButton(onClick = { send(input) }) {
                    Icon(Icons.Rounded.Mic, contentDescription = "Voice", tint = MaterialTheme.colorScheme.primary)
                }
                FilledIconButton(onClick = { send(input) }, shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Rounded.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMsg, onAddSuggestion: (ShoppingItem) -> Unit) {
    val align = if (msg.fromUser) Arrangement.End else Arrangement.Start
    val bg = if (msg.fromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (msg.fromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (msg.fromUser) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(bg)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(msg.text, color = fg, style = MaterialTheme.typography.bodyMedium)
        }
        if (msg.suggestions.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                msg.suggestions.take(6).forEach { s ->
                    AssistChip(
                        onClick = { onAddSuggestion(s) },
                        label = { Text("+ ${s.name}", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Add, null, modifier = Modifier.size(14.dp)) },
                    )
                }
            }
        }
    }
}
