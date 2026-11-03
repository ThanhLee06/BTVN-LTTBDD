package com.example.uthtodo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.example.uthtodo.model.CategoryItem
import com.example.uthtodo.model.ReminderInfo
import com.example.uthtodo.model.TaskItem
import com.example.uthtodo.ui.theme.DotInactive
import com.example.uthtodo.ui.theme.IllustrationBackground
import com.example.uthtodo.ui.theme.PlaceholderGrey
import com.example.uthtodo.ui.theme.PrimaryBlue
import com.example.uthtodo.ui.theme.TextDescription
import com.example.uthtodo.ui.theme.TextTitle

@Composable
fun AppLogoIcon(
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    if (isLarge) {
        Box(
            modifier = modifier
                .size(86.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = Color(0x1A000000),
                    spotColor = Color(0x2A000000)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Line 1: Blue circle with checkmark + rounded bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(width = 32.dp, height = 7.dp)
                            .clip(RoundedCornerShape(3.5.dp))
                            .background(PrimaryBlue)
                    )
                }
                // Line 2: Blue circle outline + rounded bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .border(width = 2.5.dp, color = PrimaryBlue, shape = CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 22.dp, height = 7.dp)
                            .clip(RoundedCornerShape(3.5.dp))
                            .background(PrimaryBlue)
                    )
                }
            }
        }
    } else {
        // Small Logo in TopBar: Blue card with white elements
        Box(
            modifier = modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryBlue),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Line 1: White circle with checkmark + white bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(6.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(width = 13.dp, height = 2.5.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(Color.White)
                    )
                }
                // Line 2: White circle outline + white bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .border(width = 1.2.dp, color = Color.White, shape = CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 9.dp, height = 2.5.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingTopBar(
    appNamePrefix: String,
    appNameSuffix: String,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppLogoIcon(isLarge = false)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = appNamePrefix,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTitle
                )
                Text(
                    text = appNameSuffix,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }
        Text(
            text = "Bỏ qua",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextDescription,
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
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalSteps) {
            val isActive = i == currentStep
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PrimaryBlue)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(DotInactive)
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
            containerColor = PrimaryBlue,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 16.5.sp,
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
            .clip(RoundedCornerShape(24.dp))
            .background(IllustrationBackground),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(220.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = Color(0x14000000),
                    spotColor = Color(0x1F000000)
                )
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
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(2.dp, DotInactive, CircleShape)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(item.barWidthDp.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(PlaceholderGrey)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryIllustration(
    categories: List<CategoryItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(280.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(IllustrationBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            categories.forEach { item ->
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0x12000000),
                            spotColor = Color(0x18000000)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(item.backgroundColor))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.title,
                                color = Color(item.textColor),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(item.barWidthDp.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(PlaceholderGrey)
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
            .clip(RoundedCornerShape(24.dp))
            .background(IllustrationBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // White circle with notification bell
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = Color(0x14000000),
                        spotColor = Color(0x1E000000)
                    )
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
            // Rounded pill badge with reminder note & time
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        ambientColor = Color(0x10000000),
                        spotColor = Color(0x14000000)
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "${reminder.note} ${reminder.reminderTime}",
                    color = TextTitle,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

