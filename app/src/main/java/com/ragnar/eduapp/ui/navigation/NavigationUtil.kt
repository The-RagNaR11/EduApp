package com.ragnar.eduapp.ui.navigation

import com.ragnar.eduapp.data.dataClass.User
import com.ragnar.eduapp.data.repository.LocalDataRepository

/**
 * Navigation Flow (Fixed)

 * Not logged in → Language Selection
 * Logged in, no details → User Detail Entry
 * Has details, no level → Student Level Assessment
 * Has level, no session → Study Session Setup
 * Has session, no intent → Learning Intent
 * Everything complete → ChatBot
 */
object NavigationUtil {
    private val currentUser: User?
        get() = LocalDataRepository.getActiveUser()

    fun isLoggedIn(): Boolean {
        return currentUser?.isActive == 1
    }

    fun isUserDetailAvailable(): Boolean {
        val user = currentUser ?: return false
        return user.phone.isNotEmpty()
    }

    fun isLevelAssessmentSetUp(): Boolean {
        val user = currentUser ?: return false
//        val classInt = user.userClass.toIntOrNull() ?: return false
//        return user.pace.isNotEmpty() && classInt in 1..10
        return user.pace.isNotEmpty()
    }

    fun isStudySessionSetUp(): Boolean {
        val user = currentUser ?: return false
        return user.syllabus.isNotEmpty() && user.subject.isNotEmpty()
    }

    fun isIntentSetUp(): Boolean {
        val user = currentUser ?: return false
        return user.learningIntent.isNotEmpty()
    }
}