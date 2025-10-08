package com.ragnar.eduapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ragnar.eduapp.ui.screens.sign_up.GoogleSignInScreen
import com.ragnar.eduapp.ui.screens.sign_up.LanguageSelectionScreen
import com.ragnar.eduapp.ui.theme.AppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "languageSelection") {
        composable("languageSelection") { LanguageSelectionScreen(navController) }
        composable("googleSignUp") { GoogleSignInScreen(navController) }
    }
}
