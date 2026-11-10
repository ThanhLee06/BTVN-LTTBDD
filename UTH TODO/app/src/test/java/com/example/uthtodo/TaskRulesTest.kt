package com.example.uthtodo

import com.example.uthtodo.data.TaskClock
import com.example.uthtodo.data.TaskRules
import com.example.uthtodo.model.TodoTask
import org.junit.Assert.*
import org.junit.Test

class TaskRulesTest {
    private fun task(id: String, title: String, date: String, done: Boolean = false, category: String = "Học tập") =
        TodoTask(id, title, category, "14:00", isCompleted = done, date = date)

    @Test fun progressIncludesOnlyReferenceDayAndHandlesZero() {
        val tasks = listOf(task("4", "A", "2026-10-20", true), task("1", "B", "2026-10-20", true),
            task("2", "C", "2026-10-20"), task("3", "D", "2026-10-20"), task("6", "E", "2026-10-21"))
        assertEquals(50, TaskRules.percent(TaskRules.today(tasks, "2026-10-20")))
        assertEquals(0, TaskRules.percent(TaskRules.today(tasks, "2026-11-10")))
    }

    @Test fun searchAndCategoryCombineWithoutChangingServerOrder() {
        val tasks = listOf(task("6", "Ôn Chương 3", "2026-10-22"),
            task("2", "Ôn chương 2", "2026-10-20"), task("1", "Ôn chương ở nhà", "2026-10-20", category = "Cuộc sống"))
        assertEquals(listOf("6", "2"), TaskRules.filter(tasks, "  ÔN CHƯƠNG ", "Học tập").map { it.id })
        assertEquals(3, tasks.count { !it.isCompleted })
        assertTrue(TaskRules.filter(tasks, "Không tồn tại", "all").isEmpty())
    }

    @Test fun dueDatesRespectTomorrowAndSundayBoundary() {
        val clock = TaskClock("2026-10-20")
        assertEquals("Hôm nay", clock.dueLabel("2026-10-20"))
        assertEquals("Ngày mai", clock.dueLabel("2026-10-21"))
        assertEquals("Tuần này", clock.dueLabel("2026-10-25"))
        assertEquals("26/10/2026", clock.dueLabel("2026-10-26"))
        assertEquals("19/10/2026", clock.dueLabel("2026-10-19"))
        assertEquals("Ngày mai", TaskClock("2026-10-25").dueLabel("2026-10-26"))
    }

    @Test fun validationRejectsImpossibleDatesAndTime() {
        val valid = task("1", "Công việc", "2026-10-20")
        assertTrue(TaskRules.valid(valid))
        assertFalse(TaskRules.valid(valid.copy(date = "2026-02-30")))
        assertFalse(TaskRules.valid(valid.copy(time = "25:00")))
        assertFalse(TaskRules.valid(valid.copy(title = " ")))
    }
}
