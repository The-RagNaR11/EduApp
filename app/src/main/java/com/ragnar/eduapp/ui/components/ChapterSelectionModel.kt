package com.ragnar.eduapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ragnar.eduapp.ui.theme.BackgroundSecondary
import com.ragnar.eduapp.ui.theme.BrandPrimary
import com.ragnar.eduapp.ui.theme.TextPrimary

/**
 * A custom composable component that allows the user to select multiple chapters from a list of chapters.
 */
@Composable
fun ChapterSelectionModel(
    chapters: List<String>,
    selectedChapters: List<String>,
    onSelectionChange: (List<String>) -> Unit // e empty lambda method to return the list of
) {
    val selected = remember { mutableStateListOf<String>().apply { addAll(selectedChapters) } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundSecondary)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            chapters.forEach { chapter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .toggleable(
                            value = selected.contains(chapter),
                            onValueChange = {
                                if (it) selected.add(chapter) else selected.remove(chapter)
                                onSelectionChange(selected.toList())
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selected.contains(chapter),
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(checkedColor = BrandPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = chapter, fontSize = 14.sp, color = TextPrimary)
                }
            }
        }
    }
}
