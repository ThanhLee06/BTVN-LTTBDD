package com.example.ex_w4.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

object LibraryRepository : ILibraryManager {
    val books = mutableStateListOf(
        Book(1, "Sách 01", "Tác giả A", 2020, "Khoa học"),
        Book(2, "Sách 02", "Tác giả B", 2021, "Văn học"),
    )

    val students = mutableStateListOf(
        Student(1, "Nguyen Van A", "SV001"),
        Student(2, "Tran Thi B", "SV002"),
    )

    val borrowRecords = mutableStateListOf<BorrowRecord>()

    var nextBookId = mutableStateOf(3)
    var nextStudentId = mutableStateOf(3)

    fun addBook(title: String, author: String, year: Int, category: String) {
        books.add(Book(nextBookId.value, title, author, year, category))
        nextBookId.value++
    }

    fun removeBook(bookId: Int) {
        books.removeAll { it.id == bookId }
        borrowRecords.removeAll { it.bookId == bookId }
    }

    fun addStudent(name: String, studentId: String) {
        students.add(Student(nextStudentId.value, name, studentId))
        nextStudentId.value++
    }

    fun removeStudent(id: Int) {
        students.removeAll { it.id == id }
        borrowRecords.removeAll { it.studentId == id }
    }

    override fun borrowBook(studentId: Int, bookId: Int): Boolean {
        val student = students.find { it.id == studentId } ?: return false
        val book = books.find { it.id == bookId } ?: return false
        val alreadyBorrowed = borrowRecords.any { it.studentId == studentId && it.bookId == bookId }
        if (!alreadyBorrowed) {
            borrowRecords.add(BorrowRecord(studentId, bookId, student.name, book.title))
            return true
        }
        return false
    }

    override fun borrowBook(student: Student, book: Book): Boolean {
        return borrowBook(student.id, book.id)
    }

    override fun returnBook(studentId: Int, bookId: Int): Boolean {
        return borrowRecords.removeAll { it.studentId == studentId && it.bookId == bookId }
    }

    fun getBorrowedBookIds(studentId: Int): List<Int> {
        return borrowRecords.filter { it.studentId == studentId }.map { it.bookId }
    }
}
