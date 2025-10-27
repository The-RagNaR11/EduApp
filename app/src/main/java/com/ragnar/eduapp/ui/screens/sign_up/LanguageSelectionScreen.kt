package com.ragnar.eduapp.ui.screens.sign_up

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ragnar.eduapp.R
import com.ragnar.eduapp.ui.components.LanguageOptionModel
import com.ragnar.eduapp.ui.components.SignUpPageFooterModel
import com.ragnar.eduapp.ui.theme.BackgroundSecondary
import com.ragnar.eduapp.ui.theme.BrandPrimary
import com.ragnar.eduapp.ui.theme.TextPrimary
import com.ragnar.eduapp.ui.theme.TextSecondary
import com.ragnar.eduapp.ui.theme.White
import com.ragnar.eduapp.utils.DebugLogger
import com.ragnar.eduapp.utils.SharedPreferenceUtils

/**
 * A UI screen that will show multiple Language option and A Google Sign Button
 * The Radio Buttons are Dynamically Populated from LanguageOptionModel -> Another UI Element
 *
 * The Sign In Button will by default choose English as selected language if not any option are selected
 * After Sign in clicked it saves the language in SharedPreference after that
 * it uses GoogleSignInUtil class to perform all Sign In functions
 * and saves the user Details in SharedPreference
 */
@Composable
fun LanguageSelectionScreen(navController: NavController) {

    val context: Context = LocalContext.current

    val headlineTextSize = MaterialTheme.typography.headlineLarge.fontSize

    var selectedLanguage by remember { mutableStateOf("English") }

    // Language options
    val languages = listOf(
        stringResource(R.string.lang_english),
        stringResource(R.string.lang_kannada),
        stringResource(R.string.lang_hindi),
        stringResource(R.string.lang_telugu)
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSecondary)

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.height(150.dp)
                )
            }
            // Title Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = stringResource(R.string.language_icon_desc),
                    tint = TextPrimary,
                    modifier = Modifier.size(with(LocalDensity.current) { headlineTextSize.toDp() })
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.select_your_language),
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.language_subtitle),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextSecondary,
                    fontSize = 15.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Label
            Text(
                text = stringResource(R.string.select_language_label),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextSecondary,
                    fontSize = 16.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            )

            languages.forEach { language ->
                LanguageOptionModel(
                    language = language,
                    selected = selectedLanguage == language,
                    onClick = {
                        selectedLanguage = language
                        DebugLogger.debugLog("LanguageSelectionScreen", "Selected Language: $selectedLanguage")

                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            /**
             * A button with Continue Text
             * On click ->
             *  1. it will log the selected language
             *  2. It will save the selected language to SharedPreference with the help of SharedPreferenceUtils class
             */
            Button(
                onClick = {
                    DebugLogger.debugLog("LanguageSelectionScreen", "Confirm Button Clicked with selected Language: $selectedLanguage")

                    // Saves the selected language to Shared Preference
                    SharedPreferenceUtils.saveUserInfo(context, SharedPreferenceUtils.KEY_LANGUAGE, selectedLanguage)
                    // Navigate to the GoogleSignUpScreen
                    // using navController because if needed user can press back-button and come back to select language Screen
                    navController.navigate("googleSignUp")
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.continue_),
                    color = White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            SignUpPageFooterModel()
        }
    }
}