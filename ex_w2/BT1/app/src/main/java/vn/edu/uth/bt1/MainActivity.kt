package com.example.studentlist_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
data class StudentInfo(
    val fullName: String,
    val mail: String?,
    val phoneNum: String?
)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val studentList = listOf(
        StudentInfo("Nguyễn Văn An", "an@sv.edu.vn", "0901 234 567"),
        StudentInfo("Trần Thị Bình", null, "0912 888 999"),
        StudentInfo("Lê Minh Chi", "chi@sv.edu.vn", null),
        StudentInfo("Phạm Quốc Dũng", null, null),
        StudentInfo("Võ Thanh Em", "em@sv.edu.vn", "0933 111 222")
    )
    var countMissingMail = 0
    for (item in studentList) {
        if (item.mail == null) {
            countMissingMail++
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách sinh viên", color = Color.White, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF233B65))
            )
        },
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            for (student in studentList) {
                StudentItemCard(student)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E8F0), shape = RoundedCornerShape(8.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tổng: ${studentList.size} sinh viên",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Row {
                    Text(text = "Thiếu email: ", color = Color(0xFF333333))
                    Text(text = "$countMissingMail", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable
fun StudentItemCard(info: StudentInfo) {
    val displayMail = info.mail ?: "Chưa cập nhật"
    val displayPhone = info.phoneNum ?: "Chưa cập nhật"
    val colorMail = if (info.mail == null) Color.Red else Color.Gray
    val colorPhone = if (info.phoneNum == null) Color.Red else Color.Gray
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = info.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Email: $displayMail", color = colorMail, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "SĐT: $displayPhone", color = colorPhone, fontSize = 14.sp)
        }
    }
}