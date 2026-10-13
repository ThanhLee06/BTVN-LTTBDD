package com.example.scoresheet_ui
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
import kotlin.math.roundToInt
data class StudentScore(val fullName: String, val finalScore: Double?)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ScoreSheetScreen() }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreSheetScreen() {
    val studentList = listOf(
        StudentScore("Nguyễn Văn An", 9.0),
        StudentScore("Trần Thị Bình", 7.2),
        StudentScore("Lê Minh Chi", null),
        StudentScore("Phạm Quốc Dũng", 4.5),
        StudentScore("Võ Thanh Em", 5.8)
    )
    var sumScore = 0.0
    var countHasScore = 0
    var countExcellent = 0
    var countMissing = 0
    var maxScore = 0.0
    for (sv in studentList) {
        val score = sv.finalScore
        if (score != null) {
            sumScore += score
            countHasScore++
            if (score >= 8.0) countExcellent++
            if (score > maxScore) maxScore = score
        } else {
            countMissing++
        }
    }
    val avgScore = if (countHasScore > 0) ((sumScore / countHasScore) * 100).roundToInt() / 100.0 else 0.0
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bảng điểm lớp", color = Color.White, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF233B65))
            )
        },
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF2B477A), shape = RoundedCornerShape(12.dp)).padding(16.dp)
            ) {
                Column {
                    Text("Điểm trung bình lớp", color = Color.White, fontSize = 14.sp)
                    Text("$avgScore", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF5D7399), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Giỏi", color = Color.LightGray, fontSize = 12.sp)
                            Text("$countExcellent", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Chưa có điểm", color = Color.LightGray, fontSize = 12.sp)
                            Text("$countMissing", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Cao nhất", color = Color.LightGray, fontSize = 12.sp)
                            Text("$maxScore", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("DANH SÁCH - ${studentList.size} SINH VIÊN", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            for (sv in studentList) {
                val score = sv.finalScore
                val rankText = if (score == null) "Chưa có điểm"
                else if (score >= 8.0) "Giỏi"
                else if (score >= 6.5) "Khá"
                else if (score >= 5.0) "Trung bình"
                else "Yếu"
                val rankColor = if (score == null) Color.Gray
                else if (score >= 8.0) Color(0xFF2E7D32)
                else if (score >= 6.5) Color(0xFF1565C0)
                else if (score >= 5.0) Color(0xFFE65100)
                else Color(0xFFC62828)
                val scoreDisplay = score?.toString() ?: "—"
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(sv.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(rankText, color = rankColor, fontSize = 12.sp)
                        }
                        Text(scoreDisplay, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}