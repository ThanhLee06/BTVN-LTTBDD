package com.example.uthtodo.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Consistent thin strokes matching the outline icons in the supplied prototype. */
object TaskIcons {
    private fun icon(name: String, draw: PathBuilder.() -> Unit) = ImageVector.Builder(name,
        24.dp, 24.dp, 24f, 24f).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathBuilder = draw)
    }.build()

    val Home = icon("TaskHome") {
        moveTo(3f, 10f); lineTo(12f, 3f); lineTo(21f, 10f)
        moveTo(5f, 9f); verticalLineTo(21f); horizontalLineTo(10f); verticalLineTo(15f)
        horizontalLineTo(14f); verticalLineTo(21f); horizontalLineTo(19f); verticalLineTo(9f)
    }
    val List = icon("TaskList") {
        moveTo(8f, 6f); horizontalLineTo(21f); moveTo(8f, 12f); horizontalLineTo(21f)
        moveTo(8f, 18f); horizontalLineTo(21f)
        moveTo(3f, 6f); horizontalLineTo(3.1f); moveTo(3f, 12f); horizontalLineTo(3.1f)
        moveTo(3f, 18f); horizontalLineTo(3.1f)
    }
    val Bell = icon("TaskBell") {
        moveTo(18f, 8f); curveTo(18f, 0f, 6f, 0f, 6f, 8f)
        curveTo(6f, 15f, 3f, 15f, 3f, 17f); horizontalLineTo(21f)
        curveTo(21f, 15f, 18f, 15f, 18f, 8f)
        moveTo(10f, 21f); curveTo(11f, 22f, 13f, 22f, 14f, 21f)
    }
    val Settings = icon("TaskSettings") {
        moveTo(3f, 6f); horizontalLineTo(8f); moveTo(12f, 6f); horizontalLineTo(21f)
        moveTo(3f, 12f); horizontalLineTo(14f); moveTo(18f, 12f); horizontalLineTo(21f)
        moveTo(3f, 18f); horizontalLineTo(6f); moveTo(10f, 18f); horizontalLineTo(21f)
        moveTo(12f, 6f); arcTo(2f, 2f, 0f, true, true, 8f, 6f); arcTo(2f, 2f, 0f, true, true, 12f, 6f)
        moveTo(18f, 12f); arcTo(2f, 2f, 0f, true, true, 14f, 12f); arcTo(2f, 2f, 0f, true, true, 18f, 12f)
        moveTo(10f, 18f); arcTo(2f, 2f, 0f, true, true, 6f, 18f); arcTo(2f, 2f, 0f, true, true, 10f, 18f)
    }
    val School = icon("TaskSchool") {
        moveTo(2f, 9f); lineTo(12f, 4f); lineTo(22f, 9f); lineTo(12f, 14f); close()
        moveTo(6f, 11f); verticalLineTo(17f); curveTo(9f, 20f, 15f, 20f, 18f, 17f); verticalLineTo(11f)
        moveTo(22f, 9f); verticalLineTo(15f)
    }
    val Calendar = icon("TaskCalendar") {
        moveTo(8f, 2f); verticalLineTo(6f); moveTo(16f, 2f); verticalLineTo(6f)
        moveTo(3f, 10f); horizontalLineTo(21f)
        moveTo(5f, 4f); horizontalLineTo(19f); quadTo(21f, 4f, 21f, 6f)
        verticalLineTo(20f); quadTo(21f, 22f, 19f, 22f); horizontalLineTo(5f)
        quadTo(3f, 22f, 3f, 20f); verticalLineTo(6f); quadTo(3f, 4f, 5f, 4f); close()
    }
    val Edit = icon("TaskEdit") {
        moveTo(16f, 3f); curveTo(19f, 0f, 23f, 4f, 20f, 7f)
        lineTo(7f, 20f); lineTo(3f, 21f); lineTo(4f, 17f); lineTo(16f, 3f); close()
        moveTo(14f, 5f); lineTo(18f, 9f); moveTo(12f, 21f); horizontalLineTo(21f)
    }
    val Delete = icon("TaskDelete") {
        moveTo(3f, 6f); horizontalLineTo(21f); moveTo(9f, 6f); verticalLineTo(3f); horizontalLineTo(15f); verticalLineTo(6f)
        moveTo(5f, 6f); lineTo(6f, 21f); horizontalLineTo(18f); lineTo(19f, 6f)
        moveTo(10f, 10f); verticalLineTo(17f); moveTo(14f, 10f); verticalLineTo(17f)
    }
    val Search = icon("TaskSearch") {
        moveTo(18f, 10.5f); arcTo(7.5f, 7.5f, 0f, true, true, 3f, 10.5f)
        arcTo(7.5f, 7.5f, 0f, true, true, 18f, 10.5f); moveTo(16f, 16f); lineTo(21f, 21f)
    }
    val ChevronLeft = icon("TaskBack") { moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f) }
    val ChevronRight = icon("TaskNext") { moveTo(9f, 6f); lineTo(15f, 12f); lineTo(9f, 18f) }
    val Check = icon("TaskCheck") { moveTo(5f, 12f); lineTo(10f, 17f); lineTo(19f, 7f) }
    val Add = icon("TaskAdd") { moveTo(12f, 5f); verticalLineTo(19f); moveTo(5f, 12f); horizontalLineTo(19f) }
}
