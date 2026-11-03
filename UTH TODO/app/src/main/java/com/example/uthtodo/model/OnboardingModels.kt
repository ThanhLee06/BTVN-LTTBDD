package com.example.uthtodo.model

interface IDisplayable {
    fun getDisplayTitle(): String
    fun getDisplayDescription(): String
}

interface IOnboardingStep : IDisplayable {
    fun getIndex(): Int
    fun getActionText(): String
    fun isSkippable(): Boolean
}

data class AppMetaData(
    val appNamePrefix: String = "UTH",
    val appNameSuffix: String = "Todo",
    val slogan: String = "Việc học, việc nhà — gọn gàng mỗi ngày",
    val version: String = "Phiên bản 2.1.0"
) {
    val fullAppName: String get() = "$appNamePrefix $appNameSuffix"
}

data class TaskItem(
    val isCompleted: Boolean,
    val barWidthDp: Int
)

data class CategoryItem(
    val title: String,
    val textColor: Int,
    val backgroundColor: Int,
    val barWidthDp: Int
)

data class ReminderInfo(
    val note: String,
    val reminderTime: String
)

abstract class BaseOnboardingStep(
    private val stepIndex: Int,
    private val title: String,
    private val description: String,
    private val actionText: String,
    private val skippable: Boolean = true
) : IOnboardingStep {
    override fun getIndex(): Int = stepIndex
    override fun getDisplayTitle(): String = title
    override fun getDisplayDescription(): String = description
    override fun getActionText(): String = actionText
    override fun isSkippable(): Boolean = skippable
}

class TaskStep(
    stepIndex: Int,
    title: String,
    description: String,
    actionText: String,
    val tasks: List<TaskItem>
) : BaseOnboardingStep(stepIndex, title, description, actionText)

class CategoryStep(
    stepIndex: Int,
    title: String,
    description: String,
    actionText: String,
    val categories: List<CategoryItem>
) : BaseOnboardingStep(stepIndex, title, description, actionText)

class ReminderStep(
    stepIndex: Int,
    title: String,
    description: String,
    actionText: String,
    val reminder: ReminderInfo
) : BaseOnboardingStep(stepIndex, title, description, actionText)

class OnboardingRepository {
    private val metaData = AppMetaData()

    private val steps: List<BaseOnboardingStep> = listOf(
        TaskStep(
            stepIndex = 0,
            title = "Mọi việc, gọn trong một chỗ",
            description = "Ghi lại bài tập, việc nhà, lịch hẹn. Không còn quên việc nào.",
            actionText = "Tiếp tục",
            tasks = listOf(
                TaskItem(isCompleted = true, barWidthDp = 120),
                TaskItem(isCompleted = false, barWidthDp = 145),
                TaskItem(isCompleted = false, barWidthDp = 100)
            )
        ),
        CategoryStep(
            stepIndex = 1,
            title = "Chia theo Học tập và Cuộc sống",
            description = "Mỗi việc có nhóm riêng, nhìn là biết hôm nay cần lo gì.",
            actionText = "Tiếp tục",
            categories = listOf(
                CategoryItem(
                    title = "Học tập",
                    textColor = 0xFF4362E5.toInt(),
                    backgroundColor = 0xFFE8EDFE.toInt(),
                    barWidthDp = 135
                ),
                CategoryItem(
                    title = "Cuộc sống",
                    textColor = 0xFFD97706.toInt(),
                    backgroundColor = 0xFFFEF3C7.toInt(),
                    barWidthDp = 105
                )
            )
        ),
        ReminderStep(
            stepIndex = 2,
            title = "Nhắc đúng lúc",
            description = "Bật thông báo để UTH Todo nhắc bạn trước hạn chót.",
            actionText = "Bắt đầu ngay",
            reminder = ReminderInfo(
                note = "Ôn bài Toán lúc",
                reminderTime = "19:00"
            )
        )
    )

    fun getMetaData(): AppMetaData = metaData
    fun getStep(index: Int): BaseOnboardingStep = steps[index]
    fun getAllSteps(): List<BaseOnboardingStep> = steps
    fun getTotalSteps(): Int = steps.size
}

