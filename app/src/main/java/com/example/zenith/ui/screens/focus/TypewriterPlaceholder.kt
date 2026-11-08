package com.example.zenith.ui.screens.focus

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TypewriterPlaceHolder(
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(0.2f)
) {
    var displayedText by rememberSaveable { mutableStateOf("") }
    var customSuggestionIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(customSuggestionIndex) {
        val targetText = suggestions[customSuggestionIndex]

        for (i in 1..targetText.length) {
            displayedText = targetText.substring(0,i)
            delay(100.milliseconds)
        }

        delay(2000.milliseconds)

        for (i in targetText.length downTo 0) {
            displayedText = targetText.substring(0,i)
            delay(50.milliseconds)
        }

        delay(500.milliseconds)

        customSuggestionIndex = (customSuggestionIndex + 1) % suggestions.size
    }

    Text(
        text = displayedText,
        color = color,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    )
}