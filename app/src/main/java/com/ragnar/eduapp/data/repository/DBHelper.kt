package com.ragnar.eduapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Handles all local SQLite database operations for EduApp.
 * Stores user profiles and AI chat history in two tables: users and chats.
 */
class DBHelper(context: Context, customPath: String? = null) :
    SQLiteOpenHelper(context, customPath ?: DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "eduapp.db"
        private const val DATABASE_VERSION = 1

        // Table names
        const val TABLE_USER = "users"
        const val TABLE_CHAT = "chats"

        // User columns
        const val ID = "id"
        const val USER_ID = "user_id"
        const val USER_NAME = "name"
        const val USER_EMAIL = "email"
        const val USER_LANGUAGE = "language"
        const val USER_PROFILE_PIC_URL = "profile_pic_url"
        const val USER_PHONE_NUMBER = "phone"
        const val USER_SCHOOL = "school"
        const val USER_AMBITION = "ambition"
        const val USER_CLASS = "user_class"
        const val USER_PACE = "pace"
        const val USER_CHAPTER_LIST = "chapter_list"
        const val USER_SUBJECT = "subject"
        const val USER_SYLLABUS = "syllabus"
        const val USER_LEARNING_INTENT = "learning_intent"
        const val IS_ACTIVE = "is_active"

        // Chat columns
        const val CHAT_ID = "id"
        const val CHAT_USER_ID = "user_id"
        const val CHAT_USER_MESSAGE = "user_message"
        const val CHAT_AI_RESPONSE = "ai_response"
        const val CHAT_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = """
            CREATE TABLE $TABLE_USER (
                $ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USER_ID TEXT UNIQUE NOT NULL,
                $USER_NAME TEXT NOT NULL,
                $USER_EMAIL TEXT UNIQUE NOT NULL,
                $USER_LANGUAGE TEXT,
                $USER_PROFILE_PIC_URL TEXT,
                $USER_PHONE_NUMBER TEXT,
                $USER_SCHOOL TEXT,
                $USER_AMBITION TEXT,
                $USER_CLASS TEXT,
                $USER_PACE TEXT,
                $USER_CHAPTER_LIST TEXT,
                $USER_SUBJECT TEXT,
                $USER_SYLLABUS TEXT,
                $USER_LEARNING_INTENT TEXT,
                $IS_ACTIVE INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createChatTable = """
            CREATE TABLE $TABLE_CHAT (
                $CHAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $CHAT_USER_ID TEXT NOT NULL,
                $CHAT_USER_MESSAGE TEXT,
                $CHAT_AI_RESPONSE TEXT,
                $CHAT_TIMESTAMP INTEGER DEFAULT (strftime('%s','now')),
                FOREIGN KEY($CHAT_USER_ID) REFERENCES $TABLE_USER($USER_ID)
            )
        """.trimIndent()

        db.execSQL(createUserTable)
        db.execSQL(createChatTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHAT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    // ------------------------
    //  USER METHODS
    // ------------------------

    /**
     * Adds a new user or activates an existing one.
     * if user already exists then it will just update the fields
     * Deactivates all previous users before inserting the new one.
     */
    fun addUser(
        name: String,
        email: String,
        profilePicUrl: String,
        userId: String,
        language: String
    ): Long {
        val db = writableDatabase

        // Deactivate all users
        db.execSQL("UPDATE $TABLE_USER SET $IS_ACTIVE = 0")

        // Prepare values for insert or update
        val values = ContentValues().apply {
            put(USER_NAME, name)
            put(USER_EMAIL, email)
            put(USER_PROFILE_PIC_URL, profilePicUrl)
            put(USER_LANGUAGE, language)
            put(IS_ACTIVE, 1)
        }

        // Check if user already exists by userId or email
        val cursor = db.rawQuery(
            "SELECT $USER_ID FROM $TABLE_USER WHERE $USER_ID = ? OR $USER_EMAIL = ?",
            arrayOf(userId, email)
        )

        val rowId: Long = if (cursor.moveToFirst()) {
            // Update existing user
            db.update(
                TABLE_USER,
                values,
                "$USER_ID = ? OR $USER_EMAIL = ?",
                arrayOf(userId, email)
            ).toLong()
        } else {
            // ✅ Insert new user
            values.put(USER_ID, userId)
            db.insert(TABLE_USER, null, values)
        }

        cursor.close()
        return rowId
    }


    /**
     * Retrieves a user record by their email.
     */
    fun getUserByEmail(email: String): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USER WHERE $USER_EMAIL = ?", arrayOf(email))
    }

    /**
     * Fetches all stored users.
     */
    fun getAllUsers(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USER", null)
    }

    /**
     * Fetches the currently active user (if any).
     */
    fun getActiveUser(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USER WHERE $IS_ACTIVE = 1 LIMIT 1", null)
    }

    /**
     * Deletes a user from the database by email.
     */
    fun deleteUser(email: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_USER, "$USER_EMAIL = ?", arrayOf(email))
    }

    /**
     * Updates a single user field (key-value) for the active user.
     * Example: updateUserDetail("school", "New School Name")
     */
    fun updateUserDetail(key: String, value: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply { put(key, value) }
        return db.update(TABLE_USER, values, "$IS_ACTIVE = 1", null)
    }

    // ------------------------
    //  CHAT METHODS
    // ------------------------

    /**
     * Adds a chat record for the specified user ID.
     */
    fun addChat(userId: String, userMessage: String, aiResponse: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(CHAT_USER_ID, userId)
            put(CHAT_USER_MESSAGE, userMessage)
            put(CHAT_AI_RESPONSE, aiResponse)
        }
        return db.insert(TABLE_CHAT, null, values)
    }

    /**
     * Adds a chat entry for the currently active user.
     */
    fun addChatForActiveUser(userMessage: String, aiResponse: String): Long {
        var result: Long = -1
        val db = readableDatabase
        db.rawQuery("SELECT $USER_ID FROM $TABLE_USER WHERE $IS_ACTIVE = 1 LIMIT 1", null).use { cursor ->
            if (cursor.moveToFirst()) {
                val userId = cursor.getString(cursor.getColumnIndexOrThrow(USER_ID))
                result = addChat(userId, userMessage, aiResponse)
            }
        }
        return result
    }

    /**
     * Returns all chats for a specific user ID.
     */
    fun getChatsByUserId(userId: String): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_CHAT WHERE $CHAT_USER_ID = ? ORDER BY $CHAT_TIMESTAMP ASC",
            arrayOf(userId)
        )
    }

    /**
     * Returns all chats for the currently active user.
     */
    fun getChatsForActiveUser(): Cursor? {
        val db = readableDatabase
        var cursor: Cursor? = null
        db.rawQuery("SELECT $USER_ID FROM $TABLE_USER WHERE $IS_ACTIVE = 1 LIMIT 1", null).use { userCursor ->
            if (userCursor.moveToFirst()) {
                val userId = userCursor.getString(userCursor.getColumnIndexOrThrow(USER_ID))
                cursor = getChatsByUserId(userId)
            }
        }
        return cursor
    }

    /**
     * Clears all chats for a specific user.
     */
    fun clearChatsForUser(userId: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_CHAT, "$CHAT_USER_ID = ?", arrayOf(userId))
    }

    /**
     * Deletes all chat records from the database.
     */
    fun clearAllChats(): Int {
        val db = writableDatabase
        return db.delete(TABLE_CHAT, null, null)
    }
}
