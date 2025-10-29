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
    val userClass: Int,
    val pace: String,
    val chapterList: List<String>,
    val subject: String,
    val syllabus: String,
    val learningIntent: String,
    val isActive: Int
)