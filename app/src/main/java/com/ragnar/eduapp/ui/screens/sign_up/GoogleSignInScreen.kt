package com.ragnar.eduapp.ui.screens.sign_up

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ragnar.eduapp.R
import com.ragnar.eduapp.core.GoogleSignIn
import com.ragnar.eduapp.data.repository.DBHelper
import com.ragnar.eduapp.ui.components.SignUpPageFooterModel
import com.ragnar.eduapp.ui.theme.BackgroundPrimary
import com.ragnar.eduapp.ui.theme.BackgroundSecondary
import com.ragnar.eduapp.ui.theme.ColorHint
import com.ragnar.eduapp.ui.theme.TextPrimary
import com.ragnar.eduapp.ui.theme.TextSecondary
import com.ragnar.eduapp.ui.theme.White
import com.ragnar.eduapp.utils.DebugLogger
import com.ragnar.eduapp.utils.SharedPreferenceUtils

@Composable
fun GoogleSignInScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db : DBHelper = DBHelper(context)

    /**
     * Using rememberLauncherForActivityResult to keep the launcher alive and stable
     * Even if there is an UI update
     * It is useful because creating new launcher each time UI updates will break the result
     */
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        GoogleSignIn.doGoogleSignIn(
            context = context,
            scope = scope,
            launcher = null,
            onLoginSuccess = { }
        )
    }


    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {

        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPrimary)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Spacer(modifier = Modifier.weight(1f))
            Card (
//                border = BorderStroke(1.dp, ColorHint),
                elevation = CardDefaults.elevatedCardElevation(10.dp)
            ) {
                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundSecondary)
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.padding(10.dp))
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
                    Spacer(modifier = Modifier.padding(10.dp))
                    Text(
                        text = stringResource(R.string.ai_tutor_platform),
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.personilized_learning_message),
                        color = TextSecondary,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.padding(10.dp))


                    /**
                     * A button to with Google ICON and Continue with Google Text
                     * onClick ->
                     *  1. It will call the GoogleSignIn class which has a helper object doGoogleSignIn
                     *  2. On successful login it will return the GoogleUserInfo class
                     *  3. The details that re passed on success are stored on sharedPreference for later use
                     *  4. The it will navigate to the UserDetailEntryScreen and clear the back-stach so user can't
                     *  go back to GoogleSignInScreen
                     */
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = ColorHint),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 50.dp),
                        onClick = {
                            DebugLogger.debugLog("SignUpScreen", "Google Sign In Button Clicked")

                            GoogleSignIn.doGoogleSignIn(
                                context = context,
                                scope = scope,
                                launcher = launcher,
                                onLoginSuccess = { userInfo ->
                                    SharedPreferenceUtils.saveUserInfo(context, SharedPreferenceUtils.KEY_ID, userInfo.id)
                                    SharedPreferenceUtils.saveUserInfo(context, SharedPreferenceUtils.KEY_EMAIL, userInfo.email)
                                    SharedPreferenceUtils.saveUserInfo(context, SharedPreferenceUtils.KEY_DISPLAY_NAME, userInfo.displayName)
                                    SharedPreferenceUtils.saveUserInfo(context, SharedPreferenceUtils.KEY_PROFILE_PIC, userInfo.profilePictureUri)

                                    db.addUser()

                                    val phoneNumber = SharedPreferenceUtils.getUserInfo(context, SharedPreferenceUtils.KEY_PHONE_NUMBER)

                                    if (SharedPreferenceUtils.isUserLoggedIn(context)) {
                                        if (phoneNumber.isNullOrEmpty()) {
                                            // If phone number is empty → go to user detail entry
                                            navController.navigate("userDetailEntry") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        } else {
                                            // If phone number exists → go to study session setup
                                            navController.navigate("studySessionSetup") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    }


                                }
                            )
                        },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,

                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google sign-in icon",
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.google_sign_in),
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium

                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(5.dp))
                    Text(
                        text = stringResource(R.string.welcome_back),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            SignUpPageFooterModel()
        }
    }
}