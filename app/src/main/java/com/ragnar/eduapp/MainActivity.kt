package com.ragnar.eduapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ragnar.eduapp.data.repository.LocalDataRepository
import com.ragnar.eduapp.ui.navigation.NavigationUtil
import com.ragnar.eduapp.ui.screens.home.ChatBotScreen
import com.ragnar.eduapp.ui.screens.home.LearningLatentScreen
import com.ragnar.eduapp.ui.screens.home.StudySessionSetupScreen
import com.ragnar.eduapp.ui.screens.sign_up.GoogleSignInScreen
import com.ragnar.eduapp.ui.screens.sign_up.LanguageSelectionScreen
import com.ragnar.eduapp.ui.screens.sign_up.StudentLevelAssessmentScreen
import com.ragnar.eduapp.ui.screens.sign_up.UserDetailEntryScreen
import com.ragnar.eduapp.ui.theme.AppTheme
import com.ragnar.eduapp.utils.DebugLogger


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalDataRepository.init(this)
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
    var startDestination: String
    try {
        startDestination = when {
            !NavigationUtil.isLoggedIn() -> "languageSelection"

            NavigationUtil.isLoggedIn() && !NavigationUtil.isUserDetailAvailable() -> "userDetailEntry"

            NavigationUtil.isLoggedIn() && NavigationUtil.isUserDetailAvailable() && !NavigationUtil.isLevelAssessmentSetUp() ->
                "studentLevelAssessment"

            NavigationUtil.isLoggedIn() && NavigationUtil.isLevelAssessmentSetUp() && !NavigationUtil.isStudySessionSetUp() ->
                "studySessionSetUp"

            NavigationUtil.isLoggedIn() && NavigationUtil.isStudySessionSetUp() && !NavigationUtil.isIntentSetUp() ->
                "learningIntent"

            else -> "chatBot"
        }

    }catch (e: Exception) {
        DebugLogger.errorLog("MainActivity", "Using fallback \n $e")
        startDestination = "languageSelection"
    }


    NavHost(navController = navController, startDestination = startDestination) {
        composable("languageSelection") { LanguageSelectionScreen(navController) }
        composable("googleSignUp") { GoogleSignInScreen(navController) }
        composable("userDetailEntry") { UserDetailEntryScreen(navController) }
        composable("studentLevelAssessment") { StudentLevelAssessmentScreen(navController) }
        composable("studySessionSetUp") { StudySessionSetupScreen(navController) }
        composable("learningIntent") { LearningLatentScreen(navController) }
        composable("chatBot") { ChatBotScreen() }
    }
}
