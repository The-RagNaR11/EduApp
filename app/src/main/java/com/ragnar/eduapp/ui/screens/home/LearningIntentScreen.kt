package com.ragnar.eduapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.outlined.ImportContacts
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ragnar.ai_tutor.ui.components.IntentListViewModel
import com.ragnar.eduapp.R
import com.ragnar.eduapp.ui.components.LearnerTitleModel
import com.ragnar.eduapp.ui.theme.AccentBlue
import com.ragnar.eduapp.ui.theme.AccentGreen
import com.ragnar.eduapp.ui.theme.BackgroundPrimary
import com.ragnar.eduapp.ui.theme.BackgroundSecondary
import com.ragnar.eduapp.ui.theme.ColorError
import com.ragnar.eduapp.ui.theme.ColorSuccess
import com.ragnar.eduapp.ui.theme.ColorWarning
import com.ragnar.eduapp.ui.theme.IconSecondary
import com.ragnar.eduapp.ui.theme.TextOnPrimary
import com.ragnar.eduapp.ui.theme.TextPrimary
import com.ragnar.eduapp.ui.theme.TextSecondary
import com.ragnar.eduapp.ui.theme.White
import com.ragnar.eduapp.utils.SharedPreferenceUtils


@Composable
fun LearningLatentScreen(navController: NavController) {

    val context = LocalContext.current

    var uiState by remember { mutableStateOf("") }

    var selectedOption by remember { mutableStateOf("") }


    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPrimary)
                .padding(20.dp),
        ) {
            Spacer(modifier = Modifier.padding(50.dp))
            Text(
                text = "\uD83C\uDFAF  ${stringResource(R.string.learning_intent_title)}",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.padding(3.dp))
            Text(
                text = stringResource(R.string.learning_intent_subtitle),
                color = TextSecondary,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.padding(10.dp))

            Text(
                text = stringResource(R.string.what_type_of_learner),
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.padding(3.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Card (
                    modifier = Modifier.weight(0.5f)
                        .height(180.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)

                ) {
                    Column(
                        modifier = Modifier
                            .background(BackgroundSecondary)
                            .fillMaxSize()
                            .padding(vertical = 25.dp, horizontal = 10.dp)
                            .clickable{
                                uiState = "concept"
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,

                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ImportContacts,
                            contentDescription = "School Icon",
                            tint = AccentBlue,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.padding(3.dp))
                        Text(
                            text = stringResource(R.string.concept_learner),
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.padding(3.dp))
                        Text(
                            text = stringResource(R.string.concept_learner_sub),
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(5.dp))
                Card (
                    modifier = Modifier.weight(0.5f)
                        .height(180.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(BackgroundSecondary)
                            .fillMaxSize()
                            .padding(vertical = 25.dp, horizontal = 10.dp)
                            .clickable{
                                uiState = "exam"
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = "School Icon",
                            tint = AccentGreen,
                            modifier = Modifier.size(43.dp)
                        )
                        Spacer(modifier = Modifier.padding(3.dp))
                        Text(
                            text = stringResource(R.string.exam_oriented),
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.padding(3.dp))
                        Text(
                            text = stringResource(R.string.exam_oriented_sub),
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(10.dp))
            if (uiState == "concept"){
                LearnerTitleModel(stringResource(R.string.select_concept_approach))
                Spacer(modifier = Modifier.padding(5.dp))
                IntentListViewModel(
                    title = stringResource(R.string.first_time_learner),
                    subtitle = stringResource(R.string.first_time_learner_sub),
                    icon = Icons.Default.ImportContacts,
                    iconTint = AccentBlue,
                    isSelected = selectedOption == "first_time_learner",
                    onClick = {
                        selectedOption = "first_time_learner"
                    }
                )
                Spacer(modifier = Modifier.padding(5.dp))
                IntentListViewModel(
                    title = stringResource(R.string.revision),
                    subtitle = stringResource(R.string.revision_sub),
                    icon = Icons.Default.ImportContacts,
                    iconTint = AccentGreen,
                    isSelected = selectedOption == "revision_learner",
                    onClick = {
                        selectedOption = "revision_learner"
                    }
                )

            } else if (uiState == "exam") {
                LearnerTitleModel(stringResource(R.string.time_for_learning))
                Spacer(modifier = Modifier.padding(5.dp))
                IntentListViewModel(
                    title = stringResource(R.string.less_time),
                    subtitle = stringResource(R.string.less_time_sub),
                    icon = Icons.Default.Alarm,
                    iconTint = ColorError, // Red Color
                    isSelected = selectedOption == "less_time",
                    onClick = {
                        selectedOption = "less_time"
                    }
                )
                Spacer(modifier = Modifier.padding(5.dp))

                IntentListViewModel(
                    title = stringResource(R.string.medium_time),
                    subtitle = stringResource(R.string.medium_time_sub),
                    icon = Icons.Default.Alarm,
                    iconTint = ColorWarning, // Yellow Color
                    isSelected = selectedOption == "medium_time",
                    onClick = {
                        selectedOption = "medium_time"
                    }
                )
                Spacer(modifier = Modifier.padding(5.dp))

                IntentListViewModel(
                    title = stringResource(R.string.high_time),
                    subtitle = stringResource(R.string.high_time_sub),
                    icon = Icons.Default.Alarm,
                    iconTint = ColorSuccess, // Green Color
                    isSelected = selectedOption == "high_time",
                    onClick = {
                        selectedOption = "high_time"
                    }
                )


            }
            Spacer(modifier = Modifier.padding(20.dp))
            Button(
                onClick = {
                    SharedPreferenceUtils.saveUserInfo(context, SharedPreferenceUtils.KEY_LEARNING_INTENT, selectedOption)

                    navController.navigate("chatBot") {
                        popUpTo(0) { inclusive = true }
                    }


                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = selectedOption.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    disabledContainerColor = IconSecondary
                )
            ) {
                Text(
                    text = stringResource(R.string.start_tutoring),
                    color = TextOnPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}