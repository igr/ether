package dev.oblac.tudu.app

import dev.oblac.tudu.domain.DraftToDoList
import dev.oblac.tudu.domain.ToDoList
import dev.oblac.tudu.domain.ToDoListName
import dev.oblac.ether.Pipe
import dev.oblac.ether.Pure
import dev.oblac.tudu.event.*
import dev.oblac.tudu.matter.Store

val createToDoList = Pipe<ToDoListCreateRequested> {
    println("Creating draft list: ${it.listId}")
    val list = Store.createNewDraftToDoList(it.listId)
    println("Created draft list: ${it.listId}")
    ToDoListCreated(list.id)
}

data class SaveToDoListState(
    val name: ToDoListName,
    val draftToDoList: DraftToDoList?,
    val existingTodoListByName: ToDoList?
)

val saveToDoList = Pure<ToDoListSaveRequested, SaveToDoListState> { _, it ->
    if (it.draftToDoList == null) {
        return@Pure ToDoListNotSaved("Draft list not found")
    }
    if (it.existingTodoListByName != null) {
        return@Pure ToDoListNotSaved("List with name already exists")
    }

    val newTodoList = ToDoList(it.draftToDoList.id, it.name)
    ToDoListSaved(newTodoList)
}

val deleteToDoList = Pipe<ToDoListDeleteRequested> {
    Store.deleteToDoList(it.listId)
    ToDoListDeleted()
}
