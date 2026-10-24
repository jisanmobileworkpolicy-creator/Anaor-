package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.JisanMobileTheme
import com.example.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {
    private var viewModelRef: ShopViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.data.remote.JisanApiService.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            val shopViewModel: ShopViewModel = viewModel()
            viewModelRef = shopViewModel
            val isDarkMode by shopViewModel.isDarkMode.collectAsState()
            JisanMobileTheme(darkTheme = isDarkMode) {
                AppNavigation(viewModel = shopViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModelRef?.syncProducts(showToast = false)
        viewModelRef?.checkForAppUpdate(manual = false)
    }
}
