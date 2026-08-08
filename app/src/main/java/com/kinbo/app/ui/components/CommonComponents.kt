package com.kinbo.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinbo.app.ui.theme.CategoryColors
import com.kinbo.app.model.ItemCategory
import com.kinbo.app.model.Priority
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Int = 56,
    stroke: Int = 6,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
) {
    val animated by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(800), label = "ring")
    val pctText = "${(progress * 100).toInt()}%"
    Box(modifier = modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val strokePx = stroke.dp.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)
            drawArc(color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = strokePx, cap = StrokeCap.Round))
            drawArc(color = indicatorColor, startAngle = -90f, sweepAngle = 360f * animated, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = strokePx, cap = StrokeCap.Round))
        }
        Text(pctText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun BudgetBar(
    progress: Float,
    exceeded: Boolean,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(800), label = "bar")
    val color = when {
        exceeded -> MaterialTheme.colorScheme.error
        progress > 0.8f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun CategoryDot(category: ItemCategory, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(10.dp).clip(CircleShape).background(CategoryColors.forCategory(category.displayName)))
}

@Composable
fun PriorityChip(priority: Priority, modifier: Modifier = Modifier) {
    val (color, label) = when (priority) {
        Priority.High -> MaterialTheme.colorScheme.error to "High"
        Priority.Medium -> MaterialTheme.colorScheme.tertiary to "Medium"
        Priority.Low -> MaterialTheme.colorScheme.secondary to "Low"
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun CategoryChip(category: ItemCategory, modifier: Modifier = Modifier) {
    val color = CategoryColors.forCategory(category.displayName)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(category.displayName, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
fun PurchasedCheck(
    purchased: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (purchased) MaterialTheme.colorScheme.primary else Color.Transparent
    val border = if (purchased) Color.Transparent else MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(container)
            .border(1.5.dp, border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (purchased) {
            Icon(Icons.Rounded.Check, contentDescription = "Purchased", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AvatarPile(initials: List<String>, maxVisible: Int = 3, modifier: Modifier = Modifier) {
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error,
    )
    Row(modifier = modifier) {
        initials.take(maxVisible).forEachIndexed { i, init ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = (i * -8).dp)
                    .clip(CircleShape)
                    .background(palette[i % palette.size])
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(init, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        if (initials.size > maxVisible) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = (maxVisible * -8).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("+${initials.size - maxVisible}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
