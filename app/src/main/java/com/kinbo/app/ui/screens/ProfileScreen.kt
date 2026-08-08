package com.kinbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinbo.app.data.KinboViewModel

@Composable
fun ProfileScreen(vm: KinboViewModel, onBack: () -> Unit) {
    val user by vm.user.collectAsState()
    val lists by vm.lists.collectAsState()
    val premium by vm.premium.collectAsState()
    val totalItems = lists.sumOf { it.totalItems }
    val totalPurchased = lists.sumOf { it.purchasedCount }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } }) },
    ) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner).background(MaterialTheme.colorScheme.background)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                    Text(user.initials, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.height(12.dp))
                Text(user.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Surface(color = if (premium) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                    Text(" ${user.plan} ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (premium) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Lists", "${lists.size}", Modifier.weight(1f))
                StatTile("Items", "$totalItems", Modifier.weight(1f))
                StatTile("Bought", "$totalPurchased", Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))
            ProfileItem("Edit Profile", Icons.Rounded.Edit) {}
            ProfileItem("My Favorites", Icons.Rounded.Star) {}
            ProfileItem("Shared Lists", Icons.Rounded.Group) {}
            ProfileItem("Payment & Subscription", Icons.Rounded.Payments) {}
            ProfileItem("Privacy & Security", Icons.Rounded.Security) {}
            ProfileItem("Help Center", Icons.Rounded.Help) {}
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Icon(Icons.Rounded.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
