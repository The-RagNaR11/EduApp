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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                MainScreen()
            }
        }
        LocalDataRepository.init(this)
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val startDestination = when {

        NavigationUtil.isLoggedIn() && NavigationUtil.isUserDetailAvailable() -> {
            if (NavigationUtil.isLevelAssessmentSetUp()) {
                "studentLevelAssessment"
            } else {
                if (NavigationUtil.isStudySessionSetUp()) {
                    "studySessionSetUp"
                } else {
                    if (NavigationUtil.isIntentSetUp()) {
                        "learningIntent"
                    } else {
                        "chatBot"
                    }
                }
            }
        }

        NavigationUtil.isLoggedIn() && !NavigationUtil.isUserDetailAvailable() -> {
            "userDetailEntry"
        }


        else -> "languageSelection"
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
