package com.kinbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.model.ShoppingList
import com.kinbo.app.ui.components.AvatarPile
import com.kinbo.app.ui.components.BudgetBar
import com.kinbo.app.ui.components.ProgressRing
import com.kinbo.app.ui.components.SectionHeader

@Composable
fun HomeScreen(
    vm: KinboViewModel,
    onOpenList: (String) -> Unit,
    onCreateList: () -> Unit,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    onScan: (String) -> Unit,
    onShare: (com.kinbo.app.model.ShoppingList) -> Unit,
) {
    val user by vm.user.collectAsState()
    val lists by vm.lists.collectAsState()
    val budget by vm.budget.collectAsState()
    val notifications by vm.notifications.collectAsState()

    val active = lists.filter { it.active }
    val recent = lists.sortedByDescending { it.updatedAt }
    val unreadCount = notifications.count { !it.read }
    val totalProgress = if (lists.isEmpty()) 0f else lists.sumOf { it.purchasedCount.toDouble() / it.totalItems.coerceAtLeast(1) }.toFloat() / lists.size

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { HomeHeader(name = user.name, initials = user.initials, unread = unreadCount, onProfile = onProfile, onNotifications = onNotifications) }
        item { HeroProgressCard(totalProgress, budget.spent, budget.monthlyLimit, active.size) }
        item { Spacer(Modifier.height(16.dp)) }

        item { SectionHeader("Active Lists", action = "See all", onAction = { onOpenList(recent.firstOrNull()?.id ?: "") }) }
        item {
            if (active.isEmpty()) {
                Text("No active lists — you're all caught up! 🎉",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(active, key = { it.id }) { list ->
                        ActiveListCard(list = list, onClick = { onOpenList(list.id) })
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
        item {
            SectionHeader("Budget Overview", action = "Details", onAction = { })
            BudgetOverviewCard(budget.spent, budget.monthlyLimit, budget.exceeded)
        }

        item { Spacer(Modifier.height(16.dp)) }
        item { SectionHeader("Recent Lists", action = "New", onAction = onCreateList) }
        items(recent.take(5), key = { it.id }) { list ->
            RecentListRow(list = list, onClick = { onOpenList(list.id) }, onFav = { vm.toggleFavorite(list.id) })
        }

        item { Spacer(Modifier.height(20.dp)) }
        item { QuickActionsRow(onCreateList = onCreateList, onScan = { active.firstOrNull()?.id?.let(onScan) }, onShare = { active.firstOrNull()?.let(onShare) }) }
    }
}

@Composable
private fun HomeHeader(name: String, initials: String, unread: Int, onProfile: () -> Unit, onNotifications: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary).clickable(onClick = onProfile), contentAlignment = Alignment.Center) {
            Text(initials, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Good day 👋", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onNotifications) {
            BadgedBox(badge = { if (unread > 0) Badge { Text("$unread") } }) {
                Icon(Icons.Rounded.Notifications, contentDescription = "Notifications")
            }
        }
    }
}

@Composable
private fun HeroProgressCard(progress: Float, spent: Double, limit: Double, activeCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Shopping Progress", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium)
                Text("${(progress * 100).toInt()}% complete", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("$activeCount active lists", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Text("Spent $${"%.2f".format(spent)} of $${"%.0f".format(limit)}", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
            }
            ProgressRing(progress = progress, size = 72, stroke = 8, trackColor = Color.White.copy(alpha = 0.3f), indicatorColor = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun ActiveListCard(list: ShoppingList, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(list.emoji, fontSize = 28.sp)
                Spacer(Modifier.width(8.dp))
                Text(list.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(progress = list.progress, size = 44, stroke = 5)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("${list.purchasedCount}/${list.totalItems} items", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("$${"%.2f".format(list.estimatedTotal)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            AvatarPile(initials = list.collaborators.map { it.initials })
        }
    }
}

@Composable
private fun BudgetOverviewCard(spent: Double, limit: Double, exceeded: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("This month", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text(if (exceeded) "Over budget" else "On track", style = MaterialTheme.typography.labelSmall, color = if (exceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            Text("$${"%.2f".format(spent)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("of $${"%.0f".format(limit)} budget", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            BudgetBar(progress = (spent / limit).toFloat(), exceeded = exceeded)
        }
    }
}

@Composable
private fun RecentListRow(list: ShoppingList, onClick: () -> Unit, onFav: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) { Text(list.emoji, fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(list.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text("${list.totalItems} items · $${"%.2f".format(list.estimatedTotal)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ProgressRing(progress = list.progress, size = 40, stroke = 4)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onFav, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (list.favorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (list.favorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(onCreateList: () -> Unit, onScan: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuickAction("New List", Icons.Rounded.AddCircle, Modifier.weight(1f), onCreateList)
        QuickAction("Scan", Icons.Rounded.QrCodeScanner, Modifier.weight(1f), onScan)
        QuickAction("Voice", Icons.Rounded.Mic, Modifier.weight(1f)) {}
        QuickAction("Share", Icons.Rounded.Share, Modifier.weight(1f), onShare)
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).clickable(onClick = onClick).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
