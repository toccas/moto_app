package com.moto.tracker.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moto.tracker.phone.ui.SessionDetailScreen
import com.moto.tracker.phone.ui.SessionListScreen
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "list") {
                    composable("list") {
                        SessionListScreen(
                            viewModel = viewModel,
                            onSessionClick = { id -> navController.navigate("detail/$id") }
                        )
                    }
                    composable("detail/{id}") { backStack ->
                        SessionDetailScreen(
                            sessionId = backStack.arguments?.getString("id") ?: "",
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
