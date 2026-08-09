package com.kinbo.app

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.ui.navigation.KinboNavHost
import com.kinbo.app.ui.theme.KinboTheme
import com.kinbo.app.util.LocaleManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleManager.applySavedLanguage(this)
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
