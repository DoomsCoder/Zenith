package com.example.zenith.service

object RoastManager {

    // --- MILD TIER (Nudges) ---
    private val mildPickup = listOf(
        "Put it down." to "The phone isn't your mission. Your work is.",
        "Gravity check?" to "The phone was fine on the table. Focus.",
        "Hands off." to "Every pickup breaks your flow. Stay still.",
        "Not yet." to "You haven't earned a break. Put it back.",
        "Distraction alert" to "Accidental movement? Or intentional weakness?",
        "Eyes up." to "The screen on the desk is the one that matters.",
        "Steady now." to "Don't let your curiosity ruin your streak.",
        "It's just a slab." to "A slab of glass shouldn't control your hands.",
        "Mission first." to "The phone can wait. Your goals cannot.",
        "Restless?" to "Focus on the task, not the device.",
        "Table time." to "Keep the phone flat. Keep your mind sharp.",
        "Ghost buzz?" to "It didn't vibrate. You're just imagining distractions.",
        "Discipline nudged." to "Small movements lead to big failures. Stay put.",
        "The desk is safe." to "Your phone is safe on the table. No need to touch it.",
        "Focus guard." to "I'm watching your hands. Put the phone down.",
        "Stay grounded." to "Your focus is anchored to your work, not your hand."
    )

    private val mildSwitch = listOf(
        "Focus, please." to "That app isn't on your whitelist. Return to base.",
        "Wrong turn." to "You drifted. Get back to your mission.",
        "Small drift detected" to "Stay on track. You've got this.",
        "Eyes on the prize." to "Don't let a notification steal your goals.",
        "Is it urgent?" to "Probably not. Go back to work.",
        "Stay in the zone." to "You were doing so well. Why stop now?",
        "One task at a time." to "Multitasking is just another word for distraction.",
        "The mission is live." to "Don't abandon your post for a quick scroll.",
        "Back to base." to "Your workspace is waiting. Return immediately.",
        "Integrity check." to "Are you working, or just pretending to?",
        "Drifting away." to "Every second elsewhere is a second lost here.",
        "Route correction." to "Wrong app detected. Re-routing to mission.",
        "Focus integrity." to "Keep your digital workspace clean. Close that app.",
        "Notification bait." to "Don't take the bait. Your work is more important.",
        "Mission control." to "Unauthorized app usage detected. Terminate and return.",
        "Stay sharp." to "The more you switch, the slower you get."
    )

    // --- BRUTAL TIER (Hard Truths) ---
    private val brutalPickup = listOf(
        "Addicted much?" to "You can't go 5 minutes without touching it, can you?",
        "Focused? That's adorable." to "Your attention span is shorter than a goldfish.",
        "The phone won." to "It called, and you came running like a servant.",
        "Still here?" to "Your future self is taking notes. They aren't impressed.",
        "Shaky hands?" to "The withdrawal symptoms are real. Stay strong, or fail.",
        "Pathetic reach." to "Your goals are on the desk. The distraction is in your hand.",
        "Puppet on a string." to "Every buzz makes you jump. Who's in charge here?",
        "The itch." to "You just had to check, didn't you? Weak.",
        "Discipline error." to "Your hands moved before your brain could stop them.",
        "Look at you." to "Cradling your phone like it's a lifeline. It's a cage.",
        "Mission compromised." to "A true professional doesn't fidget with toys.",
        "Average behavior." to "Successful people don't have twitchy phone fingers.",
        "Digital leash." to "The phone pulled, and you followed. Pathetic.",
        "Willpower check: Fail." to "A simple piece of plastic just broke your focus.",
        "Hands of clay." to "Mold your future, don't just hold your phone.",
        "Discipline leak." to "Every pickup is a hole in your productivity bucket.",
        "The siren call." to "The black mirror is calling. Are you going to drown?",
        "Focus heist." to "You just let your phone steal your most valuable asset: time."
    )

    private val brutalSwitch = listOf(
        "Instagram again?" to "Scrolling reels won't finish your project.",
        "The timer is still running..." to "...but your discipline clearly isn't.",
        "Oh, still scrolling?" to "Your deadline doesn't care about your feed.",
        "Achievement Unlocked: Failure" to "You just earned a focus score penalty. Congrats.",
        "The rabbit hole." to "You went in for one thing, and now you're lost. Wake up.",
        "Digital zombie." to "Swipe, scroll, repeat. Is this your best life?",
        "Feed the algorithm." to "It grows stronger while your career grows weaker.",
        "Dopamine junkie." to "Chasing tiny red circles instead of big milestones.",
        "Focus is a skill." to "And you're currently losing that skill. Fast.",
        "Not whitelisted." to "That app is a productivity poison. Close it.",
        "The world is watching." to "Well, the algorithm is. And it thinks you're easy.",
        "Time is leaking." to "Every second here is a second stolen from your future.",
        "Mindless drift." to "You're not even choosing to be here. You're just drifting.",
        "The attention tax." to "That app just charged you 2 minutes of your life.",
        "Content consumer." to "Producing nothing. Consuming everything. Sad.",
        "The scroll trap." to "Designed by geniuses to keep average people average.",
        "Dopamine debt." to "Borrowing happiness from your future self. Pay it back.",
        "Focus funeral." to "Here lies your productivity. Buried under a social feed."
    )

    // --- SAVAGE TIER (Emotional Damage) ---
    private val savagePickup = listOf(
        "Willpower: ZERO." to "You're a slave to a piece of glass and silicon.",
        "Give up." to "If you can't even keep your hands off a phone, you've already lost.",
        "EMOTIONAL DAMAGE" to "Your parents expected a CEO, but they got a scroller.",
        "Weakness detected." to "This is why you're still in the same place as last year.",
        "Look at you." to "Trembling for a notification. It's actually sad.",
        "Uninstall me." to "You don't deserve this engine. Go back to being average.",
        "Scraping the bottom." to "This level of discipline is honestly embarrassing.",
        "The Scroller's Fate." to "Destined to watch others succeed through a small screen.",
        "Unfit for Duty." to "A mission this simple, and you're already breaking.",
        "Total surrender." to "The phone owns your time, your focus, and your future.",
        "Is this it?" to "Is this the peak of your ambition? Checking a blank screen?",
        "Shameful." to "A child has more self-control than you right now.",
        "The Great Capitulation." to "You surrendered your goals to a notification. Weak.",
        "Dopamine's Bitch." to "Do you even make your own choices anymore?",
        "Zero respect." to "How can you expect to succeed if you can't even sit still?",
        "Discipline's Grave." to "You're burying your potential every time you pick it up.",
        "The loser's twitch." to "That reflex to check your phone is why you're mediocre.",
        "Beyond help." to "Maybe you should just hire someone to do the hard work for you."
    )

    private val savageSwitch = listOf(
        "Pathetic." to "Your willpower is non-existent. Go back to your mission.",
        "5 minutes? Wow." to "Your goals are officially on life support.",
        "The distraction won." to "You might as well give up on your dreams today.",
        "Is this 'Deep Work'?" to "Because it looks like 'Deep Procrastination' to me.",
        "Waste of potential." to "Imagine what you could do if you weren't so easily bored.",
        "Go ahead, fail." to "The world needs more average people to work for the focused ones.",
        "A quiet life." to "Keep scrolling. The world will pass you by without a sound.",
        "Algorithm's Pet." to "Good boy. Keep watching. Stay unproductive.",
        "The Great Drift." to "You're not working. You're just existing until the next buzz.",
        "Legacy: Scrolled." to "Your biography will be 400 pages of 'And then he opened TikTok'.",
        "Disappointment." to "To yourself, your goals, and everyone who believed in you.",
        "Reality check." to "You're failing. Right now. In real time. Close the app.",
        "Ambition's End." to "This is where dreams go to die: in a social media feed.",
        "The Scroller's Curse." to "Always watching greatness, never achieving it.",
        "Weakness Incarnate." to "You can't resist a single app switch. It's truly sad.",
        "Future janitor." to "Keep scrolling. Someone has to clean up after the focused people.",
        "The 99%." to "This is exactly what the average person does. Enjoy being normal.",
        "Focus failure." to "You're not built for deep work. Go back to your memes."
    )

    fun getRoast(intensity: Int, type: String, isUrgent: Boolean = false): Pair<String, String> {
        if (intensity == 0) return "" to ""

        val pool = when {
            isUrgent || intensity == 3 -> if (type == "PICKUP") savagePickup else savageSwitch
            intensity == 2 -> if (type == "PICKUP") brutalPickup else brutalSwitch
            else -> if (type == "PICKUP") mildPickup else mildSwitch
        }

        return pool.random()
    }
}
