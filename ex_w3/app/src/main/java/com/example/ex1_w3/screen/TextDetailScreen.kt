package com.example.ex1_w3.screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
@Composable
fun TextDetailScreen(navController: NavHostController) {
    Scaffold(
        topBar = { DetailTopBar("Text Detail", navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = buildAnnotatedString {
                    append("The ")

                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append("quick")
                    }
                    append(" ")

                    withStyle(SpanStyle(color = BrownColor, fontSize = 44.sp)) {
                        append("Brown")
                    }
                    append("\nfox ")

                    withStyle(SpanStyle(letterSpacing = 8.sp)) {
                        append("jumps")
                    }
                    append(" ")

                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
                    ) {
                        append("over")
                    }
                    append("\n")

                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("the")
                    }
                    append(" ")

                    withStyle(
                        SpanStyle(fontFamily = FontFamily.Cursive, fontStyle = FontStyle.Italic)
                    ) {
                        append("lazy")
                    }
                    append(" dog.")
                },
                fontSize = 32.sp,
                lineHeight = 52.sp
            )
            HorizontalDivider(modifier = Modifier.padding(top = 24.dp))
        }
    }
}