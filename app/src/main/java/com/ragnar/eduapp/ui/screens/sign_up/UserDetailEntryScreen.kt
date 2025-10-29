package com.ragnar.eduapp.ui.screens.sign_up

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Person3
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ragnar.eduapp.R
import com.ragnar.eduapp.core.GoogleSignIn
import com.ragnar.eduapp.data.dataClass.User
import com.ragnar.eduapp.data.repository.DBHelper
import com.ragnar.eduapp.data.repository.LocalDataRepository
import com.ragnar.eduapp.ui.components.SignUpPageFooterModel
import com.ragnar.eduapp.ui.theme.AccentBlue
import com.ragnar.eduapp.ui.theme.AiMessageBackground
import com.ragnar.eduapp.ui.theme.BackgroundPrimary
import com.ragnar.eduapp.ui.theme.BackgroundSecondary
import com.ragnar.eduapp.ui.theme.Black
import com.ragnar.eduapp.ui.theme.ChipBackground
import com.ragnar.eduapp.ui.theme.ColorError
import com.ragnar.eduapp.ui.theme.ColorHint
import com.ragnar.eduapp.ui.theme.TextOnPrimary
import com.ragnar.eduapp.ui.theme.TextPrimary
import com.ragnar.eduapp.ui.theme.TextSecondary
import com.ragnar.eduapp.ui.theme.White
import com.ragnar.eduapp.utils.DebugLogger
import com.ragnar.eduapp.utils.SharedPreferenceUtils

@Composable
fun UserDetailEntryScreen(navController: NavController) {

    val context: Context = LocalContext.current

    //  getting full name from already google login which is stored in shared preference
    val activeUser: User? = LocalDataRepository.getActiveUser()

    var fullName by remember { mutableStateOf(activeUser?.name ?: "") }
    var phoneNumber by remember { mutableStateOf("") }
    var ambitionText by remember { mutableStateOf(" ") }
    var schoolName by remember { mutableStateOf("") }

    var phoneError by remember { mutableStateOf<String?>(null) }
    var schoolError by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
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
                    Text(
                        text = stringResource(R.string.welcome_to_ai_tutor_message),
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.lets_go_to_know_you_message),
                        color = TextSecondary,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.padding(10.dp))

                    /**
                     * TextField to entry Full Name
                     * ReadOnly
                     */
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {

                        },
                        readOnly = true,
                        label = { Text(stringResource(R.string.full_name_label)) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.full_name_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "School Icon"
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedPlaceholderColor = ColorHint,
                            unfocusedPlaceholderColor = Black,
                            focusedLabelColor = TextSecondary,
                            unfocusedLabelColor = TextSecondary,
                            unfocusedTextColor = Black,
                            focusedTextColor = Black,
                            focusedBorderColor = AccentBlue,
                            unfocusedContainerColor = ChipBackground,
                            focusedLeadingIconColor = TextSecondary,
                            unfocusedLeadingIconColor = TextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.padding(5.dp))
                    /**
                     * TextField to entry PhoneNumber
                     * On change it will update the mutable variable phoneNumber
                     */
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            // Dynamic validation logic
                            phoneError = when {
                                phoneNumber.isBlank() -> "Phone number cannot be empty"
                                phoneNumber.matches(Regex("^[0-5]")) -> "Phone number should start from 6 to 9"
                                !phoneNumber.matches(Regex("^(?:\\+91|91)?[6-9]\\d{9}$")) -> "Enter a valid 10-digit number"
                                else -> null
                            }
                        },
                        label = { Text(stringResource(R.string.phone_number_label)) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.phone_number_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Email Icon"
                            )
                        },
                        isError = phoneError != null,
                        supportingText = {
                            if (phoneError != null) {
                                Text(
                                    text = phoneError!!,
                                    color = ColorError,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedPlaceholderColor = ColorHint,
                            unfocusedPlaceholderColor = Black,
                            focusedLabelColor = TextSecondary,
                            unfocusedLabelColor = TextSecondary,
                            unfocusedTextColor = Black,
                            focusedTextColor = Black,
                            focusedBorderColor = AccentBlue,
                            unfocusedContainerColor = ChipBackground,
                            focusedLeadingIconColor = TextSecondary,
                            unfocusedLeadingIconColor = ColorHint,
                            errorTextColor = ColorError
                        )
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                    /**
                     * TextField to entry student ambition
                     * On change it will update the mutable variable ambitionText
                     */
                    OutlinedTextField(
                        value = ambitionText,
                        onValueChange = {
                            ambitionText = it
                        },
                        label = { Text(stringResource(R.string.your_ambition_label)) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.your_ambition_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "School Icon"
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedPlaceholderColor = ColorHint,
                            unfocusedPlaceholderColor = Black,
                            focusedLabelColor = TextSecondary,
                            unfocusedLabelColor = TextSecondary,
                            unfocusedTextColor = Black,
                            focusedTextColor = Black,
                            focusedBorderColor = AccentBlue,
                            unfocusedContainerColor = ChipBackground,
                            focusedLeadingIconColor = TextSecondary,
                            unfocusedLeadingIconColor = ColorHint,
                            errorTextColor = ColorError
                        )
                    )
                    Spacer(modifier = Modifier.padding(5.dp))

                    /**
                     * TextField to entry school name
                     * On change it will update the mutable variable schoolName
                     */
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = {
                            schoolName = it
                            // Dynamic validation logic
                            schoolError = when {
                                schoolName.isBlank() -> "School name can not be empty"
                                schoolName.length < 3 -> "School name must be at least 3 characters"
                                !schoolName.matches(Regex("^[a-zA-Z0-9 .,'-]{3,}$")) -> "School name should only contain alphabet"
                                else -> null
                            }
                        },
                        label = { Text(stringResource(R.string.school_name_label)) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.school_name_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "School Icon"
                            )
                        },
                        isError = schoolError != null,
                        supportingText = {
                            if (schoolError != null) {
                                Text(
                                    text = schoolError!!,
                                    color = ColorError,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedPlaceholderColor = ColorHint,
                            unfocusedPlaceholderColor = Black,
                            focusedLabelColor = TextSecondary,
                            unfocusedLabelColor = TextSecondary,
                            unfocusedTextColor = Black,
                            focusedTextColor = Black,
                            focusedBorderColor = AccentBlue,
                            unfocusedContainerColor = ChipBackground,
                            focusedLeadingIconColor = TextSecondary,
                            unfocusedLeadingIconColor = ColorHint,
                            errorTextColor = ColorError
                        )
                    )
                    Spacer(modifier = Modifier.padding(15.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(horizontal = 20.dp),

                        //  the button is only enabled if phoneNumber and schoolName is filled
                        // Which makes the Name, Phone Number, and School Name as required and ambition as optional
                        enabled = phoneError == null && schoolError == null,
                        onClick = {
                            DebugLogger.debugLog("UserDetailEntryScreen", "Get Started Button Clicked")
                            DebugLogger.debugLog(
                                "UserDetailEntryScreen", """
                                    ******* User Detail from UserDetail Screen**********
                                    Name: $fullName
                                    Phone number: $phoneNumber
                                    School: $schoolName
                                    Ambition: $ambitionText
                                    ****************************************************
                                    """.trimIndent()
                            )

                            val phoneResult: Boolean = LocalDataRepository.updateUserDetail(
                                DBHelper.USER_PHONE_NUMBER, phoneNumber.trim())
                            val schoolResult: Boolean = LocalDataRepository.updateUserDetail(
                                DBHelper.USER_SCHOOL, schoolName.trim())
                            val ambitionResult: Boolean =LocalDataRepository.updateUserDetail(
                                DBHelper.USER_AMBITION, ambitionText.trim())

                            if (phoneResult && schoolResult && ambitionResult) {
                                DebugLogger.debugLog("UserDetailEntryScreen", "User data update successfully")
                                navController.navigate("studentLevelAssessment") {
                                    popUpTo(0) { inclusive = true } // remove back stack of the screen
                                    // on back press it will close the screen
                                }
                            } else {
                                DebugLogger.errorLog("UserDetailEntryScreen", "Failed to update user detail")
                                Toast.makeText(context, "Failed to update user detail", Toast.LENGTH_LONG).show()
                            }
                        },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            ) {
                            Text(
                                text = stringResource(R.string.get_started),
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium

                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(5.dp))

                }
            }
            Spacer(modifier = Modifier.weight(1f))
            SignUpPageFooterModel()

        }
    }
}
