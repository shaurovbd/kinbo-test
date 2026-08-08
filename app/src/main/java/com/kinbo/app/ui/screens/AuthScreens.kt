package com.kinbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinbo.app.data.KinboViewModel

private enum class AuthMode { Login, Signup }

@Composable
private fun AuthHeader(mode: AuthMode) {
    Column(modifier = Modifier.padding(vertical = 24.dp)) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) { Text("🛒", fontSize = 32.sp) }
        Spacer(Modifier.height(16.dp))
        Text(
            if (mode == AuthMode.Login) "Welcome back" else "Create account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            if (mode == AuthMode.Login) "Sign in to keep your lists in sync" else "Start shopping smarter together",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    icon: ImageVector,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotBlank()) ({ Text(placeholder) }) else null,
        singleLine = true,
        leadingIcon = { Icon(icon, contentDescription = null) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = null)
                }
            }
        } else null,
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
    )
}

@Composable
private fun SocialButton(emoji: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
fun LoginScreen(vm: KinboViewModel, onLogin: () -> Unit, onSignup: () -> Unit) {
    var email by remember { mutableStateOf("aisha@kinbo.app") }
    var password by remember { mutableStateOf("kinbo123") }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        AuthHeader(AuthMode.Login)
        AuthField("Email", email, { email = it }, Icons.Rounded.Email, placeholder = "you@example.com")
        Spacer(Modifier.height(12.dp))
        AuthField("Password", password, { password = it }, Icons.Rounded.Lock, isPassword = true)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {}) { Text("Forgot password?") }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.login(email); onLogin() }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
            Text("Sign In", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("  or continue with  ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SocialButton("🔵", "Google", Modifier.weight(1f), onClick = { vm.login(email); onLogin() })
            SocialButton("🍎", "Apple", Modifier.weight(1f), onClick = { vm.login(email); onLogin() })
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { vm.login("guest@kinbo.app"); onLogin() }, modifier = Modifier.fillMaxWidth()) {
            Text("Continue as Guest")
        }
        Spacer(Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Don't have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onSignup, contentPadding = PaddingValues(0.dp)) { Text("Sign up") }
        }
    }
}

@Composable
fun SignupScreen(vm: KinboViewModel, onSignup: () -> Unit, onLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        AuthHeader(AuthMode.Signup)
        AuthField("Full Name", name, { name = it }, Icons.Rounded.Email, placeholder = "Aisha Khan")
        Spacer(Modifier.height(12.dp))
        AuthField("Email", email, { email = it }, Icons.Rounded.Email, placeholder = "you@example.com")
        Spacer(Modifier.height(12.dp))
        AuthField("Password", password, { password = it }, Icons.Rounded.Lock, isPassword = true)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { vm.signup(if (name.isBlank()) "New User" else name, email.ifBlank { "user@kinbo.app" }); onSignup() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = true,
        ) { Text("Create Account", fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SocialButton("🔵", "Google", Modifier.weight(1f), onClick = { vm.signup("New User", email); onSignup() })
            SocialButton("🍎", "Apple", Modifier.weight(1f), onClick = { vm.signup("New User", email); onSignup() })
        }
        Spacer(Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Already have an account? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onLogin, contentPadding = PaddingValues(0.dp)) { Text("Sign in") }
        }
    }
}
