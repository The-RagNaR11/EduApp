package com.ragnar.ai_tutor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ragnar.eduapp.ui.theme.AccentBlue
import com.ragnar.eduapp.ui.theme.BackgroundSecondary
import com.ragnar.eduapp.ui.theme.ColorHint
import com.ragnar.eduapp.ui.theme.TextPrimary
import com.ragnar.eduapp.ui.theme.TextSecondary

/**
 * A reusable composable that displays a selectable option item in a card.
 *
 * @param title The main title text to display.
 * @param subtitle The descriptive subtitle text to display.
 * @param icon The vector icon to display next to the text.
 * @param isSelected Whether this item is currently selected.
 * @param onClick The callback to be invoked when this item is clicked.
 * @param modifier The modifier to be applied to the Card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntentListViewModel(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 25.dp),
        modifier = modifier.fillMaxWidth()
//            .background(BackgroundSecondary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(BackgroundSecondary)
                .padding(horizontal = 8.dp, vertical = 12.dp)

        ) {
            // RadioButton for selection state
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier.scale(0.85f),
                colors = RadioButtonDefaults.colors(
                    unselectedColor = ColorHint,
                    selectedColor = AccentBlue
                )
            )

            // Icon
            Icon(
                imageVector = icon,
                contentDescription = "Icon",
                modifier = Modifier
                    .padding(start = 8.dp, end = 16.dp)
                    .size(24.dp),
                tint = iconTint
            )

            // Column for Title and Subtitle
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
