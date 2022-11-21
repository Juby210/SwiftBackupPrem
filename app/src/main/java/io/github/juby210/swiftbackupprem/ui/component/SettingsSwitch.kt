package io.github.juby210.swiftbackupprem.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsSwitch(
    label: String,
    secondaryLabel: String,
    pref: Boolean,
    onPrefChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onPrefChange(!pref) },
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(0.95f, true)
        ) {
            ProvideTextStyle(
                MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp
                )
            ) {
                Text(text = label, softWrap = true)
            }
            ProvideTextStyle(
                MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            ) {
                Text(text = secondaryLabel)
            }
        }

        Spacer(Modifier.weight(0.05f, true))

        Switch(
            checked = pref,
            onCheckedChange = { onPrefChange(!pref) }
        )
    }
}
