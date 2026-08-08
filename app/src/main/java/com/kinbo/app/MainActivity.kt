package com.kinbo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.ui.navigation.KinboNavHost
import com.kinbo.app.ui.theme.KinboTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        setContent {
            val vm: KinboViewModel = viewModel()
            val themeMode by vm.themeMode.collectAsState()
            KinboTheme(themeMode = themeMode) {
                KinboNavHost(vm = vm)
            }
        }
    }
}
