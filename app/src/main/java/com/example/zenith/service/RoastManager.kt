package com.example.zenith.service

object RoastManager {
    private val generalRoasts = listOf(
        "Oh look, you survived 12 whole seconds without validation. Impressive.",
        "Your focus score is dropping faster than your productivity.",
        "Instagram isn't going to finish your project, is it?",
        "Focused? You? That's adorable.",
        "Go ahead, check that meme. I'll just keep counting your failure."
    )

    private val brutalRoasts = listOf(
        "5 minutes? Your goals are officially on life support.",
        "I've seen snails finish projects faster than this.",
        "The distraction has won. You might as well uninstall me.",
        "Is this really the 'Deep Work' you promised?"
    )

    fun getRoast(type: String): String {
        return when (type) {
            "BRUTAL" -> brutalRoasts.random()
            else -> generalRoasts.random()
        }
    }
}