package com.example.uthtodo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uthtodo.components.CategoryIllustration
import com.example.uthtodo.components.OnboardingTopBar
import com.example.uthtodo.components.PrimaryActionButton
import com.example.uthtodo.components.ReminderIllustration
import com.example.uthtodo.components.StepDotsIndicator
import com.example.uthtodo.components.TaskIllustration
import com.example.uthtodo.model.AppMetaData
import com.example.uthtodo.model.BaseOnboardingStep
import com.example.uthtodo.model.CategoryStep
import com.example.uthtodo.model.ReminderStep
import com.example.uthtodo.model.TaskStep
import com.example.uthtodo.ui.theme.TextDescription
import com.example.uthtodo.ui.theme.TextTitle

import androidx.compose.ui.tooling.preview.Preview
import com.example.uthtodo.model.OnboardingRepository
import com.example.uthtodo.ui.theme.EX1_W4Theme

@Composable
fun OnboardingStepScreen(
    metaData: AppMetaData,
    step: BaseOnboardingStep,
    totalSteps: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            OnboardingTopBar(
                appNamePrefix = metaData.appNamePrefix,
                appNameSuffix = metaData.appNameSuffix,
                onSkipClick = onSkip
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (step) {
                        is TaskStep -> TaskIllustration(tasks = step.tasks)
                        is CategoryStep -> CategoryIllustration(categories = step.categories)
                        is ReminderStep -> ReminderIllustration(reminder = step.reminder)
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))
                Text(
                    text = step.getDisplayTitle(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTitle,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = step.getDisplayDescription(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextDescription,
                    lineHeight = 22.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.Start
            ) {
                StepDotsIndicator(
                    totalSteps = totalSteps,
                    currentStep = step.getIndex()
                )
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryActionButton(
                    text = step.getActionText(),
                    onClick = onNext
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingStepScreenPreview() {
    val repo = OnboardingRepository()
    EX1_W4Theme {
        OnboardingStepScreen(
            metaData = repo.getMetaData(),
            step = repo.getStep(0),
            totalSteps = repo.getTotalSteps(),
            onNext = {},
            onSkip = {}
        )
    }
}

