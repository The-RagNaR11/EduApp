package com.ragnar.eduapp.ui.screens.sign_up

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.ragnar.eduapp.R
import com.ragnar.eduapp.data.repository.DBHelper
import com.ragnar.eduapp.data.repository.LocalDataRepository
import com.ragnar.eduapp.ui.components.DropDownMenuModel
import com.ragnar.eduapp.ui.theme.BackgroundPrimary
import com.ragnar.eduapp.ui.theme.BrandPrimary
import com.ragnar.eduapp.ui.theme.ColorHint
import com.ragnar.eduapp.ui.theme.IconSecondary
import com.ragnar.eduapp.ui.theme.TextOnPrimary
import com.ragnar.eduapp.ui.theme.TextPrimary
import com.ragnar.eduapp.ui.theme.TextSecondary
import com.ragnar.eduapp.ui.theme.White
import com.ragnar.eduapp.utils.DebugLogger
import com.ragnar.eduapp.utils.SharedPreferenceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLevelAssessmentScreen(navController: NavController) {
    val context = LocalContext.current

    var selectedClass by remember { mutableStateOf("") }

    val classOptions = (1..10).map { "Class $it" }

    var selectedPace by remember { mutableStateOf("") }

    val paceOptions = listOf(
        "Fast-paced learner - I grasp concepts quickly and like to move ahead",
        "Medium-paced learner - I like a balanced approach to learning",
        "Slow-paced learner - I prefer taking time to understand concepts thoroughly"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Student Level Assessment",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Help us understand your current academic level and learning style",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dropdown Menu
                DropDownMenuModel(
                    label = stringResource(R.string.select_class_label),
                    options = classOptions,
                    selectedValue = selectedClass,
                    onValueSelected = { selectedClass = it },
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Learning Pace Section
                Text(
                    text = "Learning Pace",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Radio Buttons
                paceOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedPace == option,
                                onClick = { selectedPace = option }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPace == option,
                            onClick = { selectedPace = option },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = BrandPrimary,
                                unselectedColor = IconSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        DebugLogger.debugLog("StudentLevelAssessmentScreen", "Class: $selectedClass \n Learning Pace: $selectedPace")

                        val paceResult = LocalDataRepository.updateUserDetail(DBHelper.USER_PACE, selectedPace)
                        val classResult = LocalDataRepository.updateUserDetail(DBHelper.USER_CLASS, selectedClass)

                        if (paceResult && classResult) {
                            DebugLogger.debugLog("StudentLevelAssessmentScreen", "User detail updated successfully")
                            navController.navigate("studySessionSetUp") {
                                popUpTo(0) {inclusive = true}
                            }
                        } else {
                            DebugLogger.errorLog("StudentLevelAssessmentScreen", "Failed to update user detail")
                            Toast.makeText(context, "Failed to update user detail", Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = selectedClass.isNotEmpty() && selectedPace.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimary,
                        disabledContainerColor = ColorHint
                    )
                ) {
                    Text(
                        text = "Set Learning Preferences",
                        color = TextOnPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
