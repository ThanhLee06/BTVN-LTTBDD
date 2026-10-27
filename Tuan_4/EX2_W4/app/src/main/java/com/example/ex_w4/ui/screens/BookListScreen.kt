package com.example.ex_w4.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
fun BookListScreen() {
    val books = LibraryRepository.books
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newAuthor by remember { mutableStateOf("") }
    var newYear by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A237E), Color(0xFF283593))
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
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Danh Sách Sách",
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
                            text = "${books.size} cuốn",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (books.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = Color(0xFFBDBDBD),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Chưa có sách nào",
                            color = Color(0xFF9E9E9E),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(books, key = { it.id }) { book ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
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
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1A237E).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Book,
                                            contentDescription = null,
                                            tint = Color(0xFF1A237E),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = book.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1A1A2E)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Tác giả: ${book.author}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF757575)
                                        )
                                        Text(
                                            text = "Năm: ${book.year} • ${book.category}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF757575)
                                        )
                                    }
                                    IconButton(onClick = { LibraryRepository.removeBook(book.id) }) {
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
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = Color(0xFF1A237E),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm sách")
        }

        // Add Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    errorMsg = ""
                },
                title = {
                    Text(
                        "Thêm Sách Mới",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E)
                    )
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
                        OutlinedTextField(
                            value = newYear,
                            onValueChange = { newYear = it.filter { c -> c.isDigit() } },
                            label = { Text("Năm xuất bản") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newCategory,
                            onValueChange = { newCategory = it },
                            label = { Text("Thể loại") },
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
                                LibraryRepository.addBook(
                                    newTitle.trim(),
                                    newAuthor.trim(),
                                    newYear.toIntOrNull() ?: 2024,
                                    newCategory.trim().ifBlank { "Khác" }
                                )
                                newTitle = ""; newAuthor = ""; newYear = ""; newCategory = ""; errorMsg = ""
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                    ) {
                        Text("Thêm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddDialog = false; errorMsg = ""
                        newTitle = ""; newAuthor = ""; newYear = ""; newCategory = ""
                    }) {
                        Text("Hủy", color = Color(0xFF757575))
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
