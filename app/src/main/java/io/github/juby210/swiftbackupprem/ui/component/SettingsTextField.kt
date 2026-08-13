package io.github.juby210.swiftbackupprem.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsTextField(
    label: String,
    pref: String,
    onPrefChange: (String) -> Unit,
) {
    Box(modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = pref,
            onValueChange = onPrefChange,
            label = { Text(label) },
            singleLine = true,
            trailingIcon = if (pref.isNotBlank()) {
                {
                    IconButton(onClick = { onPrefChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear input",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else null
        )
    }
}
