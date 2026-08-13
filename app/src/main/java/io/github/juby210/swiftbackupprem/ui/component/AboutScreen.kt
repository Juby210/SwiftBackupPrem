package io.github.juby210.swiftbackupprem.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.juby210.swiftbackupprem.BuildConfig
import io.github.juby210.swiftbackupprem.R

@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Card
        MaterialTheme(colorScheme = if (isSystemInDarkTheme()) lightColorScheme() else darkColorScheme()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "SwiftBackupPrem App Icon",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Text(
                        text = "SwiftBackupPrem",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "An Xposed / LSPosed module for Swift Backup providing Premium features and custom Firebase backend integration.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Features Card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Core Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text("Premium Unlocked", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Enables full access to Swift Backup premium capabilities.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text("Custom Firebase Backend", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Optionally connect your personal Firebase database & credentials.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Authors & Maintainers Card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Authors & Maintainers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column {
                            Text("Juby210", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Original Author", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedButton(
                        onClick = { uriHandler.openUri("https://github.com/Juby210") },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("GitHub", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column {
                            Text("s1ddhants1", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Maintainer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedButton(
                        onClick = { uriHandler.openUri("https://github.com/s1ddhants1") },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("GitHub", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Libraries & Credits Card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Libraries & Technologies",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("DexKit by LuckyPray", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Extension, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("LibXposed / Xposed API", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Android, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("AndroidX Jetpack Compose & Material 3", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Repository Action Button
        Button(
            onClick = { uriHandler.openUri("https://github.com/s1ddhants1/SwiftBackupPrem") },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("View Source on GitHub")
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}
