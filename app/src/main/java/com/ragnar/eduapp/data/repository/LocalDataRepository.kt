package com.ragnar.eduapp.data.repository

import android.content.Context
import com.ragnar.eduapp.data.dataClass.ChatMessageModel
import com.ragnar.eduapp.data.dataClass.User
import org.intellij.lang.annotations.Language
import org.json.JSONArray

/**
 * Singleton repository layer that interacts with DBHelper
 * and returns structured data classes instead of raw cursors.
 */
object LocalDataRepository {

    private lateinit var dbHelper: DBHelper

    /**
     * Initializes the repository with context.
     * Should be called once in Application class.
     */
    fun init(context: Context) {
        if (!::dbHelper.isInitialized) {
            dbHelper = DBHelper(context)
        }
    }

    // -------------------------------------------------
    //  USER METHODS
    // -------------------------------------------------

    /**
     * a wrapper around add user method of DBHelper class
     * return if adding user was successful or not
     */
    fun addUser(name: String, email: String, profilePicUrl: String, userId: String, language: String): Boolean {
        return dbHelper.addUser(name, email, profilePicUrl, userId, language) != -1L
    }
    /**
     * Fetches the currently active user and returns it as a [User] object.
     */
    fun getActiveUser(): User? {
        var user: User? = null
        dbHelper.getActiveUser().use { cursor ->
            if (cursor.moveToFirst()) {
                user = User(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_ID)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_EMAIL)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_NAME)),
                    language = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_LANGUAGE)) ?: "",
                    profilePicUrl = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_PROFILE_PIC_URL)) ?: "",
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_PHONE_NUMBER)) ?: "",
                    school = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_SCHOOL)) ?: "",
                    ambition = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_AMBITION)) ?: "",
                    `class` = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.USER_CLASS)),
                    pace = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_PACE)) ?: "",
                    chapterList = parseChapterList(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_CHAPTER_LIST))),
                    subject = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_SUBJECT)) ?: "",
                    syllabus = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.USER_SYLLABUS)) ?: "",
                    isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.IS_ACTIVE))
                )
            }
        }
        return user
    }

    /**
     * Converts stored JSON or CSV string into a list of chapters.
     */
    private fun parseChapterList(rawValue: String?): List<String> {
        if (rawValue.isNullOrEmpty()) return emptyList()

        return try {
            // Try JSON array first
            val jsonArray = JSONArray(rawValue)
            List(jsonArray.length()) { i -> jsonArray.getString(i) }
        } catch (e: Exception) {
            // Fallback to CSV split
            rawValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    /**
     * Updates a single field for the active user.
     * @param key Column name from DBHelper
     * @param value New value to set
     */
    fun updateUserDetail(key: String, value: String): Boolean {
        return dbHelper.updateUserDetail(key, value) > 0
    }

    // -------------------------------------------------
    //  CHAT METHODS
    // -------------------------------------------------

    /**
     * Fetches all chat messages for the active user as a list of [ChatMessageModel].
     */
    fun getChatsForActiveUser(): List<ChatMessageModel> {
        val chats = mutableListOf<ChatMessageModel>()
        dbHelper.getChatsForActiveUser()?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val userMessage = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.CHAT_USER_MESSAGE))
                    val aiResponse = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.CHAT_AI_RESPONSE))
                    val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.CHAT_TIMESTAMP))

                    if (!userMessage.isNullOrEmpty()) {
                        chats.add(ChatMessageModel(sender = "user", content = userMessage, timestamp = timestamp))
                    }
                    if (!aiResponse.isNullOrEmpty()) {
                        chats.add(ChatMessageModel(sender = "ai", content = aiResponse, timestamp = timestamp))
                    }
                } while (cursor.moveToNext())
            }
        }
        return chats
    }

    /**
     * Adds a chat entry for the currently active user.
     */
    fun addChat(userMessage: String, aiResponse: String): Boolean {
        return dbHelper.addChatForActiveUser(userMessage, aiResponse) != -1L
    }

    /**
     * Deletes all chat records for the currently active user.
     */
    fun clearChatsForActiveUser(): Boolean {
        val activeUser = getActiveUser() ?: return false
        return dbHelper.clearChatsForUser(activeUser.id) > 0
    }
}
