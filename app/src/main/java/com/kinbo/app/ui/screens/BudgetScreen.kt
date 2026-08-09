package com.kinbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.util.CurrencyFormatter
import com.kinbo.app.ui.components.BudgetBar
import com.kinbo.app.ui.components.CategoryDot
import com.kinbo.app.ui.theme.CategoryColors

@Composable
fun BudgetScreen(vm: KinboViewModel, onBack: () -> Unit) {
    val budget by vm.budget.collectAsState()
    val expenses by vm.expenses.collectAsState()
    val categoryShare = vm.categoryShare()
    var editLimit by remember { mutableStateOf(false) }
    var limitText by remember { mutableStateOf(budget.monthlyLimit.toString()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Budget Planner") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } }, actions = { IconButton(onClick = { editLimit = true; limitText = budget.monthlyLimit.toString() }) { Icon(Icons.Rounded.Edit, contentDescription = "Edit") } }) },
    ) { inner ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(inner).background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Monthly Budget", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelMedium)
                        Text(CurrencyFormatter.format(budget.monthlyLimit), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        BudgetBar(progress = budget.progress, exceeded = budget.exceeded)
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatPill("Spent", CurrencyFormatter.format(budget.spent), MaterialTheme.colorScheme.onPrimaryContainer)
                            StatPill("Remaining", CurrencyFormatter.format(budget.remaining), MaterialTheme.colorScheme.onPrimaryContainer)
                            StatPill("Avg/visit", CurrencyFormatter.format(if (expenses.isEmpty()) 0.0 else budget.spent / expenses.size), MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
            item { Text("Category Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(categoryShare) { share ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryDot(share.category, Modifier.size(12.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(share.category.displayName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(CurrencyFormatter.format(share.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                val max = categoryShare.maxOfOrNull { it.amount } ?: 1.0
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(modifier = Modifier.fillMaxWidth((share.amount / max).toFloat()).height(8.dp).clip(RoundedCornerShape(4.dp)).background(CategoryColors.forCategory(share.category.displayName)))
                }
            }
            item { Text("Recent Expenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(expenses.take(8)) { e ->
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(CategoryColors.forCategory(e.category.displayName).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = CategoryColors.forCategory(e.category.displayName), modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(e.listName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(e.category.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(CurrencyFormatter.format(e.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (editLimit) {
        AlertDialog(
            onDismissRequest = { editLimit = false },
            title = { Text("Set monthly budget") },
            text = { OutlinedTextField(value = limitText, onValueChange = { limitText = it.filter { c -> c.isDigit() || c == '.' } }, singleLine = true, prefix = { Text("৳") }) },
            confirmButton = { TextButton(onClick = { vm.setBudget(limitText.toDoubleOrNull() ?: budget.monthlyLimit); editLimit = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { editLimit = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun StatPill(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        Text(value, color = color, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}
