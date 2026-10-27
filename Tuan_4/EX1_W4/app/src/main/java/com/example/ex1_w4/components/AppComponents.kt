package com.example.ex1_w4.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ex1_w4.model.CategoryTag
import com.example.ex1_w4.model.ReminderInfo
import com.example.ex1_w4.model.TaskItem

@Composable
fun AppLogoIcon(
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val boxSize = if (isLarge) 90.dp else 36.dp
    val cornerRadius = if (isLarge) 24.dp else 10.dp
    val bgColor = if (isLarge) Color.White else Color(0xFF2563EB)
    val itemColor = if (isLarge) Color(0xFF2563EB) else Color.White

    Box(
        modifier = modifier
            .size(boxSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(if (isLarge) 10.dp else 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isLarge) 8.dp else 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(
                            width = if (isLarge) 24.dp else 10.dp,
                            height = if (isLarge) 14.dp else 6.dp
                        )
                        .clip(RoundedCornerShape(if (isLarge) 7.dp else 3.dp))
                        .background(itemColor),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = if (isLarge) 2.dp else 1.dp)
                            .size(if (isLarge) 10.dp else 4.dp)
                            .clip(CircleShape)
                            .background(bgColor)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(
                            width = if (isLarge) 32.dp else 12.dp,
                            height = if (isLarge) 6.dp else 2.5.dp
                        )
                        .clip(RoundedCornerShape(3.dp))
                        .background(itemColor)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isLarge) 8.dp else 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isLarge) 14.dp else 6.dp)
                        .border(
                            width = if (isLarge) 2.5.dp else 1.2.dp,
                            color = itemColor,
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(
                            width = if (isLarge) 42.dp else 16.dp,
                            height = if (isLarge) 6.dp else 2.5.dp
                        )
                        .clip(RoundedCornerShape(3.dp))
                        .background(itemColor)
                )
            }
        }
    }
}

@Composable
fun OnboardingTopBar(
    appName: String,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppLogoIcon(isLarge = false)
            Text(
                text = appName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        }
        Text(
            text = "Bỏ qua",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B),
            modifier = Modifier.clickable { onSkipClick() }
        )
    }
}

@Composable
fun StepDotsIndicator(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalSteps) {
            val isActive = i == currentStep
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2563EB))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCBD5E1))
                )
            }
        }
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2563EB),
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TaskIllustration(
    tasks: List<TaskItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(280.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFFEFF4FF)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(220.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp), ambientColor = Color(0x1A2563EB))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(vertical = 18.dp, horizontal = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                tasks.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (item.isCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(2.dp, Color(0xFFCBD5E1), CircleShape)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(item.widthRatio)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEEF2F6))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryIllustration(
    categories: List<CategoryTag>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(280.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFFEFF4FF)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(220.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp), ambientColor = Color(0x1A2563EB))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(vertical = 20.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                categories.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(tag.backgroundColor))
                            .border(1.dp, Color(tag.borderColor), RoundedCornerShape(10.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tag.title,
                            color = Color(tag.textColor),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderIllustration(
    reminder: ReminderInfo,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(280.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFFEFF4FF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .shadow(elevation = 4.dp, shape = CircleShape, ambientColor = Color(0x202563EB))
                .clip(CircleShape)
                .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(34.dp)
                )
            }
            Box(
                modifier = Modifier
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp), ambientColor = Color(0x1A2563EB))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "${reminder.note} ${reminder.reminderTime}",
                    color = Color(0xFF1E293B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
