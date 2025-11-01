package com.ragnar.eduapp.data.dataClass


data class User(
    val id: String,
    val email: String,
    val name: String,
    val language: String,
    val profilePicUrl: String,
    val phone: String, // Or Long if you prefer to store it as a number
    val school: String,
    val ambition: String,
    val userClass: String,
    val pace: String,
    val chapterList: List<String>,
    val subject: String,
    val syllabus: String,
    val learningIntent: String,
    val isActive: Int
) {
    override fun toString(): String {
        return """
            User Details:
            ID: $id
            Name: $name
            Email: $email
            Phone: $phone
            Profile Pic URL: $profilePicUrl
            Language: $language
            School: $school
            Ambition: $ambition
            Class: $userClass
            Pace: $pace
            Subject: $subject
            Syllabus: $syllabus
            Learning Intent: $learningIntent
            Active User: $isActive
            Chapters: ${chapterList.joinToString(", ")}
        """.trimIndent()
    }
}