package com.kinbo.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.ui.components.CategoryDot
import com.kinbo.app.ui.theme.CategoryColors

@Composable
fun AnalyticsScreen(vm: KinboViewModel, onBack: () -> Unit) {
    val weekly by vm.weeklySpending.collectAsState()
    val categoryShare = vm.categoryShare()
    val expenses by vm.expenses.collectAsState()
    val total = expenses.sumOf { it.amount }
    val maxWeekly = weekly.maxOfOrNull { it.amount } ?: 1.0

    Scaffold(
        topBar = { TopAppBar(title = { Text("Expense Analytics") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } }) },
    ) { inner ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(inner).background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard("Total spent", "$${"%.2f".format(total)}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    MetricCard("This week", "$${"%.2f".format(weekly.sumOf { it.amount })}", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                }
            }
            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Weekly Spending", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        BarChart(values = weekly.map { it.amount }, labels = weekly.map { it.dayLabel }, max = maxWeekly, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Category Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DonutChart(shares = categoryShare.map { it.category to it.amount }, modifier = Modifier.size(140.dp))
                            Spacer(Modifier.width(20.dp))
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                categoryShare.take(5).forEach { s ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryDot(s.category, Modifier.size(10.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(s.category.displayName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                        Text("$${"%.0f".format(s.amount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Monthly Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        val months = listOf("Jan" to 280.0, "Feb" to 320.0, "Mar" to 290.0, "Apr" to 410.0, "May" to 350.0, "Jun" to 327.0)
                        BarChart(values = months.map { it.second }, labels = months.map { it.first }, max = months.maxOfOrNull { it.second } ?: 1.0, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = color)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun BarChart(values: List<Double>, labels: List<String>, max: Double, color: Color, modifier: Modifier = Modifier) {
    val animated by androidx.compose.animation.core.animateFloatAsState(targetValue = 1f, label = "bars")
    Column(modifier = modifier.fillMaxWidth().height(160.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val barW = size.width / (values.size * 1.6f)
            values.forEachIndexed { i, v ->
                val h = (v / max).toFloat() * size.height * animated
                val x = i * (size.width / values.size) + barW * 0.3f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, size.height - h),
                    size = Size(barW, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 4, barW / 4),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { Text(it, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun DonutChart(shares: List<Pair<com.kinbo.app.model.ItemCategory, Double>>, modifier: Modifier = Modifier) {
    val total = shares.sumOf { it.second }.toFloat().coerceAtLeast(0.001f)
    val animated by androidx.compose.animation.core.animateFloatAsState(targetValue = 360f, label = "donut")
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(140.dp)) {
            var start = -90f
            shares.forEach { (cat, amount) ->
                val sweep = (amount.toFloat() / total) * animated
                drawArc(
                    color = CategoryColors.forCategory(cat.displayName),
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 26.dp.toPx()),
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${shares.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("cats", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
