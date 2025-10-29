package com.ragnar.eduapp.ui.navigation

import com.ragnar.eduapp.data.dataClass.User
import com.ragnar.eduapp.data.repository.LocalDataRepository

object NavigationUtil {
    private val currentUser : User? = LocalDataRepository.getActiveUser()

    fun isLoggedIn() : Boolean{
        return currentUser?.isActive != 0
    }

    fun isUserDetailAvailable() : Boolean{
        return currentUser?.phone!!.isNotEmpty()
    }

    fun isLevelAssessmentSetUp() : Boolean {
        return currentUser?.pace!!.isNotEmpty() && (currentUser.userClass > 0)
    }

    fun isStudySessionSetUp() : Boolean{
        return currentUser?.syllabus!!.isNotEmpty() && currentUser.subject.isNotEmpty() && currentUser.chapterList.isNotEmpty()
    }

    fun isIntentSetUp() : Boolean {
        return currentUser?.learningIntent!!.isNotEmpty()
    }
}