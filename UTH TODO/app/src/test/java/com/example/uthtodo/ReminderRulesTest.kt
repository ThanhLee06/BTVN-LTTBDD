package com.example.uthtodo

import com.example.uthtodo.features.ReminderRules
import com.example.uthtodo.model.TodoTask
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class ReminderRulesTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private fun epoch(time: String) = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).apply { timeZone = utc }.parse("2026-11-10 $time")!!.time
    private fun task(time: String) = TodoTask("1", "Nhắc việc", "Học tập", time, date = "2026-11-10", remind = true)
    @Test fun triggerIsFifteenMinutesBeforeDue() {
        assertEquals(epoch("12:15"), ReminderRules.trigger(task("12:30"), epoch("12:00"), true, utc))
    }
    @Test fun imminentReminderOnlyFiresImmediatelyForExplicitAction() {
        assertEquals(epoch("12:00") + 1000, ReminderRules.trigger(task("12:10"), epoch("12:00"), true, utc))
        assertEquals(0, ReminderRules.trigger(task("12:10"), epoch("12:00"), false, utc))
    }
    @Test fun disabledCompletedOrPastTaskDoesNotSchedule() {
        listOf(task("11:00"), task("13:00").copy(remind = false), task("13:00").copy(isCompleted = true),
            task("13:00").copy(date = "2026-10-20")).forEach {
            assertEquals(0, ReminderRules.trigger(it, epoch("12:00"), true, utc))
        }
    }
    @Test fun deviceTimezoneIsUsedForDueDate() {
        assertEquals(7 * 3600_000L, ReminderRules.due(task("13:00"), utc) - ReminderRules.due(task("13:00"), TimeZone.getTimeZone("Asia/Ho_Chi_Minh")))
    }
}
