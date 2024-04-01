package dev.oblac.tudu.app

import dev.oblac.tudu.Store
import dev.oblac.tudu.ether.Pipe
import dev.oblac.tudu.event.*

val createToDoList = Pipe<ToDoListCreateRequested> {
    println("Creating draft list: ${it.listId}")
    val list = Store.createNewDraftToDoList(it.listId)
    println("Created draft list: ${it.listId}")
    ToDoListCreated(list.id)
}

val saveToDoList = Pipe<ToDoListSaveRequested> {
    println("Saving draft list: ${it.listId} as ${it.name}")
    Store.findDraftToDoList(it.listId) ?: return@Pipe ToDoListNotSaved("Draft list not found");
    if (Store.findToDoListByName(it.name) != null) {
        return@Pipe ToDoListNotSaved("List with name already exists")
    }

    val result = Store.saveToDoList(it.listId, it.name)

    Store.deleteDraftToDoList(it.listId)
    println("Saved draft list: ${it.listId} as ${it.name}")
    ToDoListSaved(result)
}

val deleteToDoList = Pipe<ToDoListDeleteRequested> {
    Store.deleteToDoList(it.listId)
    ToDoListDeleted()
}
