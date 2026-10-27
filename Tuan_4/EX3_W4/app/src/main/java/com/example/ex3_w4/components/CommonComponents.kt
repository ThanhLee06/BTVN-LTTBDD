package com.example.ex3_w4.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ex3_w4.R

// Colors matching the design
val BrandBlue = Color(0xFF388AF6)
val SoftBlueBackground = Color(0xFFE4F0FF)
val LightGrayBackground = Color(0xFFF6F8FA)
val BorderGray = Color(0xFFE2E8F0)
val TextDark = Color(0xFF1E293B)
val TextSecondary = Color(0xFF64748B)

@Composable
fun HeaderSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // UTH Logo image or text branding
        Box(
            modifier = Modifier
                .height(65.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.uth),
                contentDescription = "UTH Logo",
                modifier = Modifier
                    .height(60.dp)
                    .wrapContentWidth(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SmartTasks App Title
        Text(
            text = "SmartTasks",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = BrandBlue,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFF67B0FE))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightGrayBackground,
            unfocusedContainerColor = LightGrayBackground,
            disabledContainerColor = LightGrayBackground,
            focusedBorderColor = BrandBlue,
            unfocusedBorderColor = BorderGray,
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark
        ),
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingIcon = trailingIcon,
        placeholder = {
            Text(
                text = placeholder,
                color = TextSecondary.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandBlue,
            contentColor = Color.White,
            disabledContainerColor = BrandBlue.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.8f)
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
