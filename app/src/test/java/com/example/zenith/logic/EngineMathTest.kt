package com.example.zenith.logic

import org.junit.Test
import org.junit.Assert.assertEquals

class EngineMathTest {

    @Test
    fun `calculateTimeDebt returns double the input seconds using FocusMath`() {
        val distractionSeconds = 30
        val expectedDebt = 60
        val actualDebt = FocusMath.calculateTimeDebt(distractionSeconds)
        assertEquals(expectedDebt, actualDebt)
    }

    @Test
    fun `calculateBreakBank standard applies 10 percent using FocusMath`() {
        val missionMins = 60 // 1 Hour
        val standardMins = FocusMath.calculateBreakBank(missionMins, isRelaxed = false)
        assertEquals(6, standardMins)
    }

    @Test
    fun `calculateBreakBank relaxed applies 20 percent using FocusMath`() {
        val missionMins = 60 // 1 Hour
        val relaxedMins = FocusMath.calculateBreakBank(missionMins, isRelaxed = true)
        assertEquals(12, relaxedMins)
    }

    @Test
    fun `calculateBreakBank for 3 hour session scales correctly`() {
        val missionMins = 180 // 3 Hours
        val standardMins = FocusMath.calculateBreakBank(missionMins, isRelaxed = false)
        val relaxedMins = FocusMath.calculateBreakBank(missionMins, isRelaxed = true)
        
        assertEquals(18, standardMins)
        assertEquals(36, relaxedMins)
    }

    @Test
    fun `calculateBreakBank floor value works for short sessions`() {
        val missionMins = 10 
        val standardMins = FocusMath.calculateBreakBank(missionMins, isRelaxed = false)
        val relaxedMins = FocusMath.calculateBreakBank(missionMins, isRelaxed = true)
        
        assertEquals(1, standardMins) // 10% is 1, floor is 1
        assertEquals(2, relaxedMins)  // 20% is 2, floor is 2
    }

    @Test
    fun `calculateBreakBank for 5 minute session returns sensible values`() {
        val missionMins = 5
        val standardMins = FocusMath.calculateBreakBank(missionMins, isRelaxed = false)
        val relaxedMins = FocusMath.calculateBreakBank(missionMins, isRelaxed = true)
        
        assertEquals(1, standardMins) // 10% is 0, but floor is 1
        assertEquals(2, relaxedMins)  // 20% is 1, but floor is 2
    }
}
