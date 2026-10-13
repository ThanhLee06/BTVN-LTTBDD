package com.example.ranking_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RankingScreen() }
    }
}
fun getRankName(score: Double): String {
    return if (score < 0.0 || score > 10.0) "Không hợp lệ"
    else if (score >= 8.0) "Giỏi"
    else if (score >= 6.5) "Khá"
    else if (score >= 5.0) "Trung bình"
    else "Yếu"
}
fun getTextColor(score: Double): Color {
    return if (score < 0.0 || score > 10.0) Color.Gray
    else if (score >= 8.0) Color(0xFF2E7D32)
    else if (score >= 6.5) Color(0xFF1565C0)
    else if (score >= 5.0) Color(0xFFE65100)
    else Color(0xFFC62828)
}
fun getBgColor(score: Double): Color {
    return if (score < 0.0 || score > 10.0) Color(0xFFEEEEEE)
    else if (score >= 8.0) Color(0xFFE8F5E9)
    else if (score >= 6.5) Color(0xFFE3F2FD)
    else if (score >= 5.0) Color(0xFFFFF3E0)
    else Color(0xFFFFEBEE)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen() {
    val currentScore = 8.5
    val testScores = listOf(9.0, 7.0, 5.5, 3.0, 11.0)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xếp loại học lực", color = Color.White, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF233B65))
            )
        },
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Điểm trung bình", color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = currentScore.toString(), fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().background(getBgColor(currentScore), shape = RoundedCornerShape(8.dp)).padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = getRankName(currentScore), color = getTextColor(currentScore), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "KIỂM TRA THÊM", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    testScores.forEachIndexed { index, score ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val scoreText = if (score % 1 == 0.0) "Điểm ${score.toInt()}" else "Điểm $score"
                            Text(text = scoreText, fontSize = 14.sp)
                            Box(
                                modifier = Modifier.background(getBgColor(score), shape = RoundedCornerShape(6.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = getRankName(score), color = getTextColor(score), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (index < testScores.size - 1) {
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}