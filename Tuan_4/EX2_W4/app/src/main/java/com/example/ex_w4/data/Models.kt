package com.example.ex_w4.data

interface IBorrowable {
    fun getDisplayInfo(): String
}

interface ILibraryManager {
    fun borrowBook(studentId: Int, bookId: Int): Boolean
    fun borrowBook(student: Student, book: Book): Boolean
    fun returnBook(studentId: Int, bookId: Int): Boolean
}

abstract class Person(
    val id: Int,
    name: String
) : IBorrowable {
    var name: String = name
        private set

    fun updateName(newName: String) {
        if (newName.isNotBlank()) {
            this.name = newName.trim()
        }
    }

    abstract fun getRole(): String

    override fun getDisplayInfo(): String {
        return "${getRole()}: $name (ID: $id)"
    }
}

abstract class LibraryItem(
    val id: Int,
    title: String,
    val year: Int
) : IBorrowable {
    var title: String = title
        private set

    fun updateTitle(newTitle: String) {
        if (newTitle.isNotBlank()) {
            this.title = newTitle.trim()
        }
    }

    abstract fun getItemType(): String

    override fun getDisplayInfo(): String {
        return "[${getItemType()}] $title ($year)"
    }
}

open class Student(
    id: Int,
    name: String,
    val studentId: String
) : Person(id, name) {
    override fun getRole(): String = "Sinh viên"

    override fun getDisplayInfo(): String {
        return "$name - MSSV: $studentId"
    }
}

open class Book(
    id: Int,
    title: String,
    val author: String,
    year: Int,
    val category: String = "Sách tham khảo"
) : LibraryItem(id, title, year) {
    override fun getItemType(): String = "Sách"

    override fun getDisplayInfo(): String {
        return "$title - Tác giả: $author ($year)"
    }
}

data class BorrowRecord(
    val studentId: Int,
    val bookId: Int,
    val studentName: String,
    val bookTitle: String
)
