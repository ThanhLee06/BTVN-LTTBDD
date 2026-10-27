package com.example.ex_w4.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ex_w4.data.LibraryRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen() {
    val students = LibraryRepository.students
    val books = LibraryRepository.books

    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var showStudentPicker by remember { mutableStateOf(false) }
    var showAddBookDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newAuthor by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(students.size) {
        if (selectedStudent == null || students.none { it.id == selectedStudent?.id }) {
            selectedStudent = students.firstOrNull()
        }
    }

    val borrowedIds = if (selectedStudent != null)
        LibraryRepository.getBorrowedBookIds(selectedStudent!!.id)
    else emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Title ──────────────────────────────────────────────────────────
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Hệ thống\nQuản lý Thư viện",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF111111),
            lineHeight = 34.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Sinh viên section ──────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 22.dp)) {
            Text(
                text = "Sinh viên",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = Color(0xFF111111)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedStudent?.name ?: "Chưa có sinh viên",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111111)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFB0B0B0),
                        focusedBorderColor = Color(0xFF234EA4),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { if (students.isNotEmpty()) showStudentPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF234EA4)),
                    enabled = students.isNotEmpty(),
                    modifier = Modifier.height(58.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp)
                ) {
                    Text(
                        text = "Thay đổi",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // ── Danh sách sách ─────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 22.dp)) {
            Text(
                text = "Danh sách sách",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = Color(0xFF111111)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Gray container for book list
        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFE4E5E7))
                .weight(1f)
        ) {
            if (books.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Chưa có sách nào",
                        color = Color(0xFF888888),
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(books, key = { it.id }) { book ->
                        val isBorrowed = borrowedIds.contains(book.id)
                        val isEnabled = selectedStudent != null

                        // Pill-shaped white card with subtle shadow
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isEnabled) Modifier.clickable {
                                        val sid = selectedStudent!!.id
                                        if (isBorrowed) LibraryRepository.returnBook(sid, book.id)
                                        else LibraryRepository.borrowBook(sid, book.id)
                                    } else Modifier
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Red checkbox matching mockup
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isBorrowed) Color(0xFF932938) else Color.Transparent)
                                        .border(
                                            2.dp,
                                            if (isBorrowed) Color(0xFF932938) else Color(0xFFAAAAAA),
                                            RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isBorrowed) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = book.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Centered Thêm button matching mockup ───────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { showAddBookDialog = true },
                modifier = Modifier
                    .width(170.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF234EA4))
            ) {
                Text(
                    text = "Thêm",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // ── Student picker dialog ──────────────────────────────────────────────
    if (showStudentPicker) {
        AlertDialog(
            onDismissRequest = { showStudentPicker = false },
            title = {
                Text("Chọn Sinh Viên", fontWeight = FontWeight.Bold, color = Color(0xFF1A3A8F))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    students.forEach { student ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selectedStudent?.id == student.id)
                                        Color(0xFF1A3A8F).copy(alpha = 0.08f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    selectedStudent = student
                                    showStudentPicker = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A3A8F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    student.name.firstOrNull()?.uppercase() ?: "?",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(student.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text(student.studentId, fontSize = 12.sp, color = Color(0xFF757575))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if (selectedStudent?.id == student.id) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF1A3A8F)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStudentPicker = false }) {
                    Text("Đóng", color = Color(0xFF757575))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Add book dialog ────────────────────────────────────────────────────
    if (showAddBookDialog) {
        AlertDialog(
            onDismissRequest = { showAddBookDialog = false; errorMsg = "" },
            title = {
                Text("Thêm Sách Mới", fontWeight = FontWeight.Bold, color = Color(0xFF1A3A8F))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Tên sách *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAuthor,
                        onValueChange = { newAuthor = it },
                        label = { Text("Tác giả *") },
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
                        if (newTitle.isBlank() || newAuthor.isBlank()) {
                            errorMsg = "Vui lòng nhập tên sách và tác giả"
                        } else {
                            LibraryRepository.addBook(newTitle.trim(), newAuthor.trim(), 2024, "Khác")
                            newTitle = ""; newAuthor = ""; errorMsg = ""
                            showAddBookDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A8F))
                ) { Text("Thêm") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddBookDialog = false; errorMsg = ""
                    newTitle = ""; newAuthor = ""
                }) { Text("Hủy", color = Color(0xFF757575)) }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
