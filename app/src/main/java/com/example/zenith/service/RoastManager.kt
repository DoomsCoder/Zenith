package com.example.zenith.service

object RoastManager {
    private val tier1Roasts = listOf( // 0-30 seconds
        "Focused? That's adorable." to "Your attention span is shorter than this text.",
        "The timer is still running..." to "...but your discipline clearly isn't.",
        "Is that Instagram I see?" to "Scrolling reels won't finish your project."
    )

    private val tier2Roasts = listOf( // 30-90 seconds
        "Still here?" to "Your future self is taking notes. Bad ones.",
        "Oh, still scrolling?" to "Your deadline doesn't care about your feed.",
        "Achievement Unlocked: Failure" to "You just earned a focus score penalty. Congrats."
    )

    private val brutalRoasts = listOf( // 90+ seconds
        "5 minutes? Wow." to "Your goals are officially on life support.",
        "The distraction won." to "You might as well uninstall me and give up.",
        "Is this 'Deep Work'?" to "Because it looks like 'Deep Procrastination' to me."
    )

    fun getRoast(isBrutal: Boolean = false, tier: Int = 1): Pair<String, String> {
        return when {
            isBrutal -> brutalRoasts.random()
            tier >= 2 -> tier2Roasts.random()
            else -> tier1Roasts.random()
        }
    }
}