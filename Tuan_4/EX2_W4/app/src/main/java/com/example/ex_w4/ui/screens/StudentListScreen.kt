package com.example.ex_w4.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ex_w4.data.LibraryRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen() {
    val students = LibraryRepository.students
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newStudentId by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF4A148C), Color(0xFF6A1B9A))
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F6FA))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = gradientBrush)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Danh Sách Sinh Viên",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${students.size} người",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (students.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFFBDBDBD),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Chưa có sinh viên nào", color = Color(0xFF9E9E9E), fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(students, key = { it.id }) { student ->
                        val borrowedCount = LibraryRepository.getBorrowedBookIds(student.id).size
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circle
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.name.firstOrNull()?.uppercase() ?: "?",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1A1A2E)
                                    )
                                    Text(
                                        text = "MSSV: ${student.studentId}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF757575)
                                    )
                                    if (borrowedCount > 0) {
                                        Text(
                                            text = "Đang mượn: $borrowedCount sách",
                                            fontSize = 12.sp,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                IconButton(onClick = { LibraryRepository.removeStudent(student.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Xóa",
                                        tint = Color(0xFFE53935)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = Color(0xFF4A148C),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm sinh viên")
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false; errorMsg = "" },
                title = {
                    Text("Thêm Sinh Viên", fontWeight = FontWeight.Bold, color = Color(0xFF4A148C))
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Họ và tên *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newStudentId,
                            onValueChange = { newStudentId = it },
                            label = { Text("Mã số sinh viên *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (errorMsg.isNotEmpty()) {
                            Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isBlank() || newStudentId.isBlank()) {
                                errorMsg = "Vui lòng nhập đầy đủ thông tin"
                            } else {
                                LibraryRepository.addStudent(newName.trim(), newStudentId.trim())
                                newName = ""; newStudentId = ""; errorMsg = ""
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C))
                    ) { Text("Thêm") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddDialog = false; errorMsg = ""
                        newName = ""; newStudentId = ""
                    }) { Text("Hủy", color = Color(0xFF757575)) }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
