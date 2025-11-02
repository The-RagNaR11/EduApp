package com.ragnar.eduapp.data.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import androidx.core.net.toUri
import androidx.core.content.edit

object ExternalDBPathManager {

    private const val PREF_KEY = "db_uri"
    private const val DB_NAME = "eduapp.db"

    fun saveUri(context: Context, uri: Uri) {
        context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            .edit { putString(PREF_KEY, uri.toString()) }
    }

    fun getDbPath(context: Context): String? {
        val uriStr = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
            .getString(PREF_KEY, null) ?: return null

        val uri = uriStr.toUri()
        val docFile = DocumentFile.fromTreeUri(context, uri) ?: return null

        val dbFile = docFile.findFile(DB_NAME) ?: docFile.createFile("application/octet-stream", DB_NAME)
        val dbUri = dbFile?.uri ?: return null

        val docId = DocumentsContract.getDocumentId(dbUri)
        val realPath = "/sdcard/" + docId.substringAfter(":")
        return realPath
    }
}
