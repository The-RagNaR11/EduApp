package com.ragnar.eduapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ragnar.eduapp.ui.theme.TextPrimary

@Composable
fun LearnerTitleModel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary
    )
}