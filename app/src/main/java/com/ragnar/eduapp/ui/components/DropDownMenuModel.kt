package com.ragnar.eduapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ragnar.eduapp.ui.theme.BackgroundPrimary
import com.ragnar.eduapp.ui.theme.ColorHint
import com.ragnar.eduapp.ui.theme.LightGray
import com.ragnar.eduapp.ui.theme.TextPrimary
import com.ragnar.eduapp.ui.theme.TextSecondary
import com.ragnar.eduapp.ui.theme.textFieldBackgroundColor
import kotlin.text.ifEmpty


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenuModel(
    label: String, // a string for label of the dropdown menu
    options: List<String>, // a list of strings as content of drop down menu
    selectedValue: String, // a string representing the currently selected to display it in Drop dropdown menu
    onValueSelected: (String) -> Unit // a lambda function that takes a String parameter and returns nothing used for updating the selected value
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = textFieldBackgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        TextField(
            readOnly = true,
            value = selectedValue,
            onValueChange = {},
            modifier = Modifier.menuAnchor(),
            label = { Text(label) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = textFieldBackgroundColor,
                unfocusedContainerColor = textFieldBackgroundColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLabelColor = TextSecondary,
                unfocusedLabelColor = ColorHint
            ),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(textFieldBackgroundColor)
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option, color = TextPrimary) },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                // adds a divider between each item except the last one
                if (index != options.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        thickness = 1.dp,
                        color = LightGray
                    )
                }
            }
        }
    }
}
