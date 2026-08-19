package com.example.zenith.service

object RoastManager {
    private val mildRoasts = listOf(
        "Focus, please." to "You're getting distracted. Get back to work.",
        "Gentle reminder" to "Your mission is still active. Let's finish it.",
        "Small drift detected" to "Stay on track. You've got this."
    )

    private val brutalRoasts = listOf(
        "Focused? That's adorable." to "Your attention span is shorter than this text.",
        "The timer is still running..." to "...but your discipline clearly isn't.",
        "Is that Instagram I see?" to "Scrolling reels won't finish your project.",
        "Still here?" to "Your future self is taking notes. Bad ones.",
        "Oh, still scrolling?" to "Your deadline doesn't care about your feed."
    )

    private val savageRoasts = listOf(
        "Achievement Unlocked: Failure" to "You just earned a focus score penalty. Congrats.",
        "5 minutes? Wow." to "Your goals are officially on life support.",
        "The distraction won." to "You might as well uninstall me and give up.",
        "Is this 'Deep Work'?" to "Because it looks like 'Deep Procrastination' to me.",
        "Pathetic." to "Your willpower is non-existent. Go back to your mission.",
        "EMOTIONAL DAMAGE" to "Is this how you plan to reach your goals? By scrolling?"
    )

    fun getRoast(intensity: Int, isUrgent: Boolean = false): Pair<String, String> {
        if (intensity == 0) return "" to "" // Silent mode
        
        return when {
            isUrgent || intensity == 3 -> savageRoasts.random()
            intensity == 2 -> brutalRoasts.random()
            else -> mildRoasts.random()
        }
    }
}
