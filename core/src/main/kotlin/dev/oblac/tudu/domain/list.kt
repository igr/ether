package dev.oblac.tudu.domain

import java.util.*

@JvmInline
value class ToDoListId(private val value: UUID) {
    companion object {
        fun new(): ToDoListId = ToDoListId(UUID.randomUUID())
    }

    override fun toString() = value.toString()
}

@JvmInline
value class ToDoListName(private val value: String)

data class ToDoList(
    val id: ToDoListId,
    val name: ToDoListName,
)

data class DraftToDoList(
    val id: ToDoListId,
)

data class ToDoListWithItems(
    val list: ToDoList,
    val items: List<ToDoItem>
)
