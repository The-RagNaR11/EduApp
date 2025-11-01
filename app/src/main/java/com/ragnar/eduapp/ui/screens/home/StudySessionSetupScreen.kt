package com.ragnar.eduapp.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ragnar.eduapp.ui.components.DropDownMenuModel
import com.ragnar.eduapp.ui.theme.*
import com.ragnar.eduapp.R
import com.ragnar.eduapp.data.dataClass.User
import com.ragnar.eduapp.data.repository.DBHelper
import com.ragnar.eduapp.data.repository.LocalDataRepository
import com.ragnar.eduapp.ui.components.ChapterSelectionModel
import com.ragnar.eduapp.utils.DebugLogger
import com.ragnar.eduapp.utils.SharedPreferenceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySessionSetupScreen(navController: NavController) {

    val context = LocalContext.current

    val userClass = SharedPreferenceUtils.getUserInfo(context, SharedPreferenceUtils.KEY_CLASS)

    // to store the selected syllabus
    var selectedSyllabus by remember { mutableStateOf("") }
    // to store the selected subject Default as Science
    var selectedSubject by remember { mutableStateOf("Science") }
    // to store list of all selected chapters
    var selectedChapters by remember { mutableStateOf(listOf<String>()) }


    // can be dynamically implemented
    val classOptions = (1..10).map { "Class $it" }
    val syllabusOptions = listOf("CBSE", "NCERT", "Other")
    val subjectOptions = listOf("English", "Hindi", "Science", "Maths", "Other")


    // for now it is hard coded list of chapters
    // later can be dynamically implemented
    val chapters = listOf(
        "Chapter 1: Light - Reflection and Refraction",
        "Chapter 2: The Human Eye and Colourful World",
        "Chapter 3: Electricity",
        "Chapter 4: Magnetic Effects of Electric Current",
        "Chapter 5: Our Environment",
        "Chapter 6: Natural Resources",
        "Chapter 7: Life Processes",
        "Chapter 8: Control and Coordination",
        "Chapter 9: Reproduction"
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
                .fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Study Session Setup",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select your class, syllabus, and chapters for today's learning session",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                /**
                 * Drop down menu to display Chapter
                 */
//                DropDownMenuModel(
//                    label = stringResource(R.string.select_class),
//                    options = classOptions,
//                    selectedValue = selectedClass,
//                    onValueSelected = { selectedClass = it }
//                )

                Spacer(modifier = Modifier.height(8.dp))
                /**
                 * Drop down menu to display Syllabus
                 */
                DropDownMenuModel(
                    label = stringResource(R.string.select_syllabus),
                    options = syllabusOptions,
                    selectedValue = selectedSyllabus,
                    onValueSelected = { selectedSyllabus = it }
                )

                Spacer(modifier = Modifier.height(8.dp))
                /**
                 * Drop down menu to display Subject
                 */
                DropDownMenuModel(
                    label = stringResource(R.string.select_subject),
                    options = subjectOptions,
                    selectedValue = selectedSubject,
                    onValueSelected = { selectedSubject = it }
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Chapters section
                Text(
                    text = stringResource(R.string.select_chapter_to_study),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))


                ChapterSelectionModel(
                    chapters = chapters,
                    selectedChapters = selectedChapters,
                    onSelectionChange = { selectedChapters = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {

                        DebugLogger.debugLog("StudySessionSetupScreen","Syllabus: $selectedSyllabus")
                        DebugLogger.debugLog("StudySessionSetupScreen","Subject: $selectedSubject")
                        DebugLogger.debugLog("StudySessionSetupScreen","Chapters: $selectedChapters")


                        val syllabusResult = LocalDataRepository.updateUserDetail(DBHelper.USER_SYLLABUS, selectedSyllabus)
                        val subjectResult = LocalDataRepository.updateUserDetail(DBHelper.USER_SUBJECT, selectedSubject)
                        val chaptersResult = LocalDataRepository.updateChapterList(selectedChapters)

                        if (syllabusResult && subjectResult && chaptersResult) {
                            DebugLogger.debugLog("StudySessionSetupScreen", "User detail updated successfully")
                            navController.navigate("learningIntent")
                            /**
                             * For Debug purpose only
                             */
                            val user: User? = LocalDataRepository.getActiveUser()
                            DebugLogger.debugLog("ActiveUser", "User: ${user.toString()}")
                        } else {
                            DebugLogger.debugLog("StudySessionSetupScreen", "Failed to update user detail")
                            Toast.makeText(context, "Failed to update user detail", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = selectedSyllabus.isNotEmpty() && selectedChapters.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimary,
                        disabledContainerColor = IconSecondary
                    )
                ) {
                    Text(
                        text = "Start AI Tutoring Session",
                        color = TextOnPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
