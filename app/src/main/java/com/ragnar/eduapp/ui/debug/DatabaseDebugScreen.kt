package com.ragnar.eduapp.ui.debug

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.ragnar.eduapp.data.repository.ExternalDBPathManager
import com.ragnar.eduapp.data.repository.LocalDataRepository

@Composable
fun DatabaseDebugScreen() {
    val context = LocalContext.current
    var allUsers by remember { mutableStateOf(emptyList<String>()) }
    var currentUser by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf("") }

    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            ExternalDBPathManager.saveUri(context, uri)
            val docFile = DocumentFile.fromTreeUri(context, uri)
            selectedFolder = docFile?.name ?: uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {

        Text("Database Utility Panel", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(20.dp))

        // Pick folder button
        Button(
            onClick = { folderLauncher.launch(null) },
        ) {
            Text("Pick DB Folder (SAF)")
        }

        Spacer(Modifier.height(10.dp))

        Text("Selected folder: $selectedFolder", modifier = Modifier.padding(6.dp))

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val usersList = LocalDataRepository.getAllUsers()
                val formattedList = usersList.map { user -> "${user.name} (${user.email})" }
                allUsers = formattedList
            }
        ) {
            Text("Show All Users")
        }

        LazyColumn {
            items(allUsers) { Text(it) }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val user = LocalDataRepository.getActiveUser()
                currentUser = user?.toString() ?: "No active user"
            }
        ) {
            Text("Show Current User")
        }

        Text(currentUser)
    }
}
