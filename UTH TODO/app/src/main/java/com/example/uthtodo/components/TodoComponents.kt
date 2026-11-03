package com.example.uthtodo.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.model.TodoTask
import com.example.uthtodo.ui.theme.PrimaryBlue

/**
 * Google 'G' Icon drawn nicely
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier, size: Int = 20) {
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.9).sp,
            color = Color(0xFF4285F4)
        )
    }
}

/**
 * Circular Progress Ring for the Home Progress Banner
 * Designed exactly as the donut indicator in the reference UI
 */
@Composable
fun CircularProgressRing(
    percentage: Int,
    modifier: Modifier = Modifier,
    size: Int = 82,
    strokeWidth: Float = 22f
) {
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val sweepAngle = (percentage.coerceIn(0, 100) / 100f) * 360f

            // Faint track background donut circle
            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            if (sweepAngle > 0f) {
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Text(
            text = "$percentage%",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}

/**
 * Notification Bell Icon with Red Dot (Soft rounded circular icon button)
 */
@Composable
fun NotificationBellButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnread: Boolean = true
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .shadow(elevation = 2.dp, shape = CircleShape, spotColor = Color(0x10000000))
            .clip(CircleShape)
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Thông báo",
            tint = Color(0xFF1E293B),
            modifier = Modifier.size(24.dp)
        )
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .offset(x = 5.dp, y = (-7).dp)
                    .background(Color(0xFFEF4444), CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}

/**
 * Task Item row matching exactly the reference UI screenshot:
 * Pill capsule shape (RoundedCornerShape(26.dp)), ultra-clean shadow,
 * circle checkbox, category badge, time, and right arrow indicator.
 */
@Composable
fun TaskItemRow(
    task: TodoTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x10000000),
                ambientColor = Color(0x06000000)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox circle
        if (task.isCompleted) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Đã hoàn thành",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .border(2.dp, Color(0xFFCBD5E1), CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Task Content
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onEdit() }
        ) {
            Text(
                text = task.title,
                fontSize = 15.sp,
                fontWeight = if (task.isCompleted) FontWeight.SemiBold else FontWeight.Bold,
                color = if (task.isCompleted) Color(0xFF94A3B8) else Color(0xFF1E293B),
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category badge
                val isStudy = task.category.contains("Học", ignoreCase = true)
                val badgeBg = if (isStudy) Color(0xFFEEF2FF) else Color(0xFFFFF7ED)
                val badgeText = if (isStudy) Color(0xFF4362E5) else Color(0xFFEA580C)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = task.category,
                        color = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Time
                Text(
                    text = task.time,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Chevron right icon as shown in screenshot
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Chi tiết",
            tint = Color(0xFFCBD5E1),
            modifier = Modifier
                .size(22.dp)
                .clickable { onEdit() }
        )
    }
}

/**
 * Bottom Navigation Bar with centered round floating blue '+' FAB button
 * Matches the curved white bar with subtle icons
 */
@Composable
fun TodoBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                spotColor = Color(0x15000000)
            )
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trang chủ
            BottomBarItem(
                label = "Trang chủ",
                icon = Icons.Outlined.Home,
                isSelected = currentTab == "home",
                onClick = { onTabSelected("home") }
            )

            // Danh sách
            BottomBarItem(
                label = "Danh sách",
                icon = Icons.Outlined.Checklist,
                isSelected = currentTab == "list",
                onClick = { onTabSelected("list") }
            )

            // Nút Thêm '+' FAB nổi bật ở giữa
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape, spotColor = PrimaryBlue.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(PrimaryBlue)
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm công việc",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Thông báo
            BottomBarItem(
                label = "Thông báo",
                icon = Icons.Outlined.Notifications,
                isSelected = currentTab == "notifications",
                onClick = { onTabSelected("notifications") }
            )

            // Cài đặt
            BottomBarItem(
                label = "Cài đặt",
                icon = Icons.Outlined.Tune,
                isSelected = currentTab == "settings",
                onClick = { onTabSelected("settings") }
            )
        }
    }
}

@Composable
fun BottomBarItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) PrimaryBlue else Color(0xFF94A3B8)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = tint
        )
    }
}
