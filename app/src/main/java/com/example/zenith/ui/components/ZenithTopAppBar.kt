package com.example.zenith.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.OffWhite
import com.example.zenith.ui.theme.SoftIndigo
import com.example.zenith.ui.theme.ZenithTheme
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenithTopAppBar(
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White
        ),
        title = {
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = "ZENITH",
                    color = SoftIndigo,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 6.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
                Text(
                    text = "FOCUS ENGINE",
                    color = MutedGray.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 3.sp,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                    .border(
                        BorderStroke(
                            width = 1.dp,
                            color = Color.White.copy(0.08f)
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { menuExpanded = true}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = MutedGray.copy(0.8f)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false},
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.background(Color(0xFF1A1A1A))
                ) {
                    // Item 1: Settings
                    DropdownMenuItem(
                        text = { Text("Settings", color = OffWhite.copy(0.8f), fontFamily = FontFamily.Monospace) },
                        leadingIcon = {Icon(Icons.Default.Settings, null, tint = OffWhite.copy(0.9f))},
                        onClick = {
                            menuExpanded = false
                            onNavigateToSettings()
                        }
                    )

                    HorizontalDivider(color = Color.DarkGray, thickness = 0.7.dp)

                    // Item 2: Send Feedback
                    DropdownMenuItem(
                        text = { Text("Send Feedback", color = OffWhite.copy(0.8f), fontFamily = FontFamily.Monospace)},
                        leadingIcon = { Icon(Icons.Outlined.Feedback, null, tint = OffWhite.copy(0.9f))},
                        trailingIcon = { Icon(Icons.AutoMirrored.Outlined.Send, null, tint = OffWhite.copy(0.9f))},
                        onClick = {
                            menuExpanded = false
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:support@zenith.app".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, "Zenith Feedback - v1.0")
                            }

                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback if user doesn't have email app
                                Toast.makeText(
                                    context,
                                    "No email application found to send feedback.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )

                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

                    //Item 3: About Zenith
                    DropdownMenuItem(
                        text = { Text("About Zenith", color = OffWhite.copy(0.8f), fontFamily = FontFamily.Monospace) },
                        leadingIcon = { Icon(Icons.Outlined.Info, null, tint = OffWhite.copy(0.9f)) },
                        onClick = {
                            menuExpanded = false
                            showAboutDialog = true
                        }
                    )
                }
            }
        }
    )

    // ---- About Dialog ---
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false},
            containerColor = Color(0xFF1A1A1A),
            title = {
                Text(
                    "ZENITH FOCUS ENGINE",
                    color = SoftIndigo,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Version 1.0\n\nA minimalist focus tracker engineered for deep work.\n\nDeveloped by Vedant Kakade.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("CLOSE", color = SoftIndigo, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ZenithTopAppBarPreview() {
    ZenithTheme {
        ZenithTopAppBar(
            onNavigateToSettings = {}
        )
    }
}