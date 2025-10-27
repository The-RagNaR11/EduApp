package com.ragnar.eduapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ragnar.eduapp.data.model.ChatMessageModel

/**
 * Handles all local database operations for EduApp.
 * Stores user data and AI chat history in two tables: users and chats.
 */
class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "eduapp.db"
        private const val DATABASE_VERSION = 1

        // Table names
        const val TABLE_USER = "users"
        const val TABLE_CHAT = "chats"

        // User columns
        const val USER_ID = "id" // string
        const val USER_NAME = "name" // string
        const val USER_EMAIL = "email" // string
        const val USER_LANGUAGE = "language" // string
        const val USER_PROFILE_PIC_URL = "profile_pic_url" // string (URL)
        const val USER_PHONE_NUMBER = "phone" // number
        const val USER_SCHOOL = "school" // string
        const val USER_AMBITION = "ambition" // string
        const val USER_CLASS = "class" // number
        const val USER_PACE = "pace" // string (FAST/ MEDIUM/ SLOW)
        const val USER_CHAPTER_LIST = "chapter_list" // arraylist (stored as JSON or CSV)
        const val USER_SUBJECT = "subject" // string
        const val USER_SYLLABUS = "syllabus" // string (NCERT, CBSE, etc)
        const val IS_ACTIVE = "is_active" // 1 = current active user

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
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USER_NAME TEXT NOT NULL,
                $USER_EMAIL TEXT UNIQUE NOT NULL,
                $USER_LANGUAGE TEXT,
                $USER_PROFILE_PIC_URL TEXT,
                $USER_PHONE_NUMBER TEXT,
                $USER_SCHOOL TEXT,
                $USER_AMBITION TEXT,
                $USER_CLASS INTEGER,
                $USER_PACE TEXT,
                $USER_CHAPTER_LIST TEXT,
                $USER_SUBJECT TEXT,
                $USER_SYLLABUS TEXT,
                $IS_ACTIVE INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createChatTable = """
            CREATE TABLE $TABLE_CHAT (
                $CHAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $CHAT_USER_ID INTEGER NOT NULL,
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
    //  User Helper Methods
    // ------------------------

    /**
     * Add or activate a user.
     * If adding new, it sets the user as active and deactivates all previous ones.
     */
    fun addUser(name: String, email: String): Long {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_USER SET $IS_ACTIVE = 0") // deactivate old users

        val values = ContentValues().apply {
            put(USER_NAME, name)
            put(USER_EMAIL, email)
            put(IS_ACTIVE, 1)
        }
        return db.insert(TABLE_USER, null, values)
    }

    fun getUserByEmail(email: String): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USER WHERE $USER_EMAIL = ?", arrayOf(email))
    }

    fun getAllUsers(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USER", null)
    }

    fun getActiveUser(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_USER WHERE $IS_ACTIVE = 1 LIMIT 1", null)
    }

    fun deleteUser(email: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_USER, "$USER_EMAIL = ?", arrayOf(email))
    }

    // ------------------------
    //  Chat Helper Methods
    // ------------------------

    fun addChat(userId: Int, userMessage: String, aiResponse: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(CHAT_USER_ID, userId)
            put(CHAT_USER_MESSAGE, userMessage)
            put(CHAT_AI_RESPONSE, aiResponse)
        }
        return db.insert(TABLE_CHAT, null, values)
    }

    /**
     * Add chat for currently active user.
     */
    fun addChatForActiveUser(userMessage: String, aiResponse: String): Long {
        var result: Long = -1
        val db = readableDatabase
        db.rawQuery("SELECT $USER_ID FROM $TABLE_USER WHERE $IS_ACTIVE = 1 LIMIT 1", null).use { cursor ->
            if (cursor.moveToFirst()) {
                val userId = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID))
                result = addChat(userId, userMessage, aiResponse)
            }
        }
        return result
    }

    /**
     * Get all chats for a given user.
     */
    fun getChatsByUserId(userId: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TABLE_CHAT WHERE $CHAT_USER_ID = ? ORDER BY $CHAT_TIMESTAMP ASC",
            arrayOf(userId.toString())
        )
    }

    /**
     * Get all chats for currently active user.
     */
    fun getChatsForActiveUser(): Cursor? {
        val db = readableDatabase
        var cursor: Cursor? = null
        db.rawQuery("SELECT $USER_ID FROM $TABLE_USER WHERE $IS_ACTIVE = 1 LIMIT 1", null).use { userCursor ->
            if (userCursor.moveToFirst()) {
                val userId = userCursor.getInt(userCursor.getColumnIndexOrThrow(USER_ID))
                cursor = getChatsByUserId(userId)
            }
        }
        return cursor
    }

    fun clearChatsForUser(userId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_CHAT, "$CHAT_USER_ID = ?", arrayOf(userId.toString()))
    }

    fun clearAllChats(): Int {
        val db = writableDatabase
        return db.delete(TABLE_CHAT, null, null)
    }
    /**
     * Returns all chats (for all users) as a list of ChatMessageModel objects.
     */
    fun getAllChatsAsModelList(): List<ChatMessageModel> {
        val chatList = mutableListOf<ChatMessageModel>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $CHAT_USER_MESSAGE, $CHAT_AI_RESPONSE, $CHAT_TIMESTAMP FROM $TABLE_CHAT ORDER BY $CHAT_TIMESTAMP ASC",
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val userMessage = it.getString(it.getColumnIndexOrThrow(CHAT_USER_MESSAGE))
                    val aiResponse = it.getString(it.getColumnIndexOrThrow(CHAT_AI_RESPONSE))
                    val timestamp = it.getLong(it.getColumnIndexOrThrow(CHAT_TIMESTAMP))

                    // Add user message if exists
                    if (!userMessage.isNullOrEmpty()) {
                        chatList.add(
                            ChatMessageModel(
                                sender = "user",
                                content = userMessage,
                                timestamp = timestamp
                            )
                        )
                    }

                    // Add AI response if exists
                    if (!aiResponse.isNullOrEmpty()) {
                        chatList.add(
                            ChatMessageModel(
                                sender = "ai",
                                content = aiResponse,
                                timestamp = timestamp
                            )
                        )
                    }
                } while (it.moveToNext())
            }
        }

        return chatList
    }

}
