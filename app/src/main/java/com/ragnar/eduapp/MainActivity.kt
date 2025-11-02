package com.ragnar.eduapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ragnar.eduapp.data.repository.ExternalDBPathManager
import com.ragnar.eduapp.data.repository.LocalDataRepository
import com.ragnar.eduapp.ui.debug.DatabaseDebugScreen
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

    private val REQUEST_CODE_PICK_DIR = 2001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("APP_PREF", MODE_PRIVATE)
        if (!prefs.contains("db_uri")) {
            pickDatabaseFolder()
        }

        LocalDataRepository.init(this)
        setContent {
            AppTheme {
                MainScreen()
            }
        }

    }


    private fun pickDatabaseFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        )
        startActivityForResult(intent, REQUEST_CODE_PICK_DIR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_DIR && resultCode == RESULT_OK) {
            val treeUri = data?.data ?: return

            contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            ExternalDBPathManager.saveUri(this, treeUri)

            recreate() // reload with external DB
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
