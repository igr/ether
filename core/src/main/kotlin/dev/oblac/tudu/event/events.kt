package dev.oblac.tudu.event

import dev.oblac.tudu.domain.ToDoList
import dev.oblac.tudu.domain.ToDoListId
import dev.oblac.tudu.domain.ToDoListName
import dev.oblac.ether.Event
import dev.oblac.ether.EventMeta
import dev.oblac.ether.EventRealm

// collection of event subjects
object Subject {
    val ToDoList = EventRealm("ToDoList")
}

// Sealed base class with the event subjects
sealed class ToDoListEvent : Event {
    override val meta = EventMeta(Subject.ToDoList)
}

// ----------

data class ToDoListCreateRequested(val listId: ToDoListId) : ToDoListEvent()
data class ToDoListCreated(val listId: ToDoListId) : ToDoListEvent()

// ----------

data class ToDoListSaveRequested(val listId: ToDoListId, val name: ToDoListName) : ToDoListEvent()
data class ToDoListSaved(val toDoList: ToDoList) : ToDoListEvent()
data class ToDoListNotSaved(val reason: String) : ToDoListEvent()

// ----------

data class ToDoListDeleteRequested(val listId: ToDoListId) : ToDoListEvent()
class ToDoListDeleted : ToDoListEvent()
