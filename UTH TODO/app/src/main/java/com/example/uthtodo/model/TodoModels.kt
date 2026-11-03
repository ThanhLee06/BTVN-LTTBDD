package com.example.uthtodo.model

data class TodoTask(
    val id: String,
    val title: String,
    val category: String,
    val time: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = 0,
    val completedAt: Long? = null
)

data class UserProfile(
    val uid: String = "",
    val name: String = "Khách",
    val email: String? = null,
    val isAnonymous: Boolean = true,
    val isGoogleLinked: Boolean = false,
    val photoUrl: String? = null
) {
    val initial: String get() = name.firstOrNull()?.uppercase() ?: "K"
}

data class WeeklyStat(
    val dayLabel: String,
    val completedCount: Int,
    val maxCount: Int = 5,
    val isToday: Boolean = false
)
