package com.example.uthtodo.data

import com.example.uthtodo.model.TodoTask
import com.example.uthtodo.model.WeeklyStat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** An injectable date keeps the reference screenshots reproducible without changing API dates. */
class TaskClock(private val referenceDate: String = "") {
    fun today(): String = referenceDate.takeIf { it.isNotBlank() }
        ?: SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

    fun greetingDate(): String {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(today()) ?: Date()
        return SimpleDateFormat("EEEE, d 'tháng' M", Locale.forLanguageTag("vi-VN"))
            .format(parsed).replaceFirstChar { it.titlecase(Locale.forLanguageTag("vi-VN")) }
    }

    fun dueLabel(date: String): String {
        if (date == today()) return "Hôm nay"
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }
        val due = runCatching { format.parse(date) }.getOrNull() ?: return date
        val now = format.parse(today()) ?: return date
        val tomorrow = Calendar.getInstance().apply { time = now; add(Calendar.DAY_OF_MONTH, 1) }
        if (format.format(tomorrow.time) == date) return "Ngày mai"
        val weekEnd = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_MONTH, 7 - ((get(Calendar.DAY_OF_WEEK) + 5) % 7))
        }
        if (due.after(now) && due.before(weekEnd.time)) return "Tuần này"
        return SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(due)
    }
}

object TaskRules {
    fun weeklyStats(tasks: List<TodoTask>, today: String): List<WeeklyStat> {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        val calendar = Calendar.getInstance().apply { time = format.parse(today) ?: Date() }
        val weekday = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
        calendar.add(Calendar.DAY_OF_MONTH, -weekday)
        val counts = (0..6).map {
            val date = format.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            tasks.count { task -> task.isCompleted && task.date == date }
        }
        return counts.mapIndexed { index, count ->
            WeeklyStat(if (index == 6) "CN" else "T${index + 2}", count,
                counts.maxOrNull()?.coerceAtLeast(1) ?: 1, index == weekday)
        }
    }
    fun today(tasks: List<TodoTask>, date: String) = tasks.filter { it.date == date }
    fun percent(tasks: List<TodoTask>): Int = if (tasks.isEmpty()) 0 else tasks.count { it.isCompleted } * 100 / tasks.size
    fun filter(tasks: List<TodoTask>, query: String, category: String): List<TodoTask> = tasks
        .filter { (category == "all" || it.category == category) && it.title.contains(query.trim(), ignoreCase = true) }
    fun valid(task: TodoTask): Boolean = task.title.trim().length in 1..200 &&
        task.category in listOf("Học tập", "Cuộc sống") &&
        Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(task.time) &&
        runCatching {
            val f = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }
            f.parse(task.date)?.let { f.format(it) == task.date } == true
        }.getOrDefault(false)
}
