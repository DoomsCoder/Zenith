package com.example.zenith.ui.screens.settings

import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.util.query
import com.example.zenith.ui.theme.MutedGray

@Composable
fun SettingsScreen(
    onCategoryClick: (String) -> Unit = {},
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val filteredCategories = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            settingsCategories
        } else {
            settingsCategories.filter {
                it.title.contains(searchQuery,ignoreCase = true) ||
                it.subtitle.contains(searchQuery,ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .statusBarsPadding()
    ) {
        SettingsHeader(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(filteredCategories) { category ->
                SettingsRow(
                    category = category,
                    onClick = {
                        focusManager.clearFocus()
                        onCategoryClick(category.id)
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 64.dp),
                    thickness = 0.5.dp,
                    color = Color.White.copy(alpha = 0.07f)
                )
            }

            if (filteredCategories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No settings found for \"$searchQuery\"",
                            color = MutedGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(32.dp).offset(x = (-8).dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Go Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Rounded Search Bar
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            placeholder = {
                Text("Search settings...", color = MutedGray.copy(alpha = 0.5f), fontSize = 15.sp)
            },
            leadingIcon = {
                Icon(Icons.Outlined.Search, null, tint = MutedGray, modifier = Modifier.size(20.dp))
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF111111),
                unfocusedContainerColor = Color(0xFF111111),
                disabledContainerColor = Color(0xFF111111),
                cursorColor = Color(0xFF6366F1), // Soft Indigo cursor
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 15.sp)
        )
    }
}

@Composable
private fun SettingsRow(
    category: SettingsCategory,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{ onClick() }
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = MutedGray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = category.subtitle,
                color = MutedGray,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )
        }
    }
}