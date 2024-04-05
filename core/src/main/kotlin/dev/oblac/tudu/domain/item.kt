package dev.oblac.tudu.domain

@JvmInline
value class ToDoId(private val value: Int)

@JvmInline
value class ToDoTitle(private val value: String)

@JvmInline
value class ToDoDescription(private val value: String)

data class ToDoItem(
    val id: ToDoId,
    val title: ToDoTitle,
    val description: ToDoDescription
)
