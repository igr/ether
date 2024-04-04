package dev.oblac.tudu.event

import dev.oblac.tudu.domain.ToDoList
import dev.oblac.tudu.domain.ToDoListId
import dev.oblac.tudu.domain.ToDoListName
import dev.oblac.tudu.ether.Event
import dev.oblac.tudu.ether.EventMeta
import dev.oblac.tudu.ether.EventSubject

// collection of event subjects
object Subject {
    val ToDoList = EventSubject("ToDoList")
}

// todo probably a better syntax if possible

// ----------

data class ToDoListCreateRequested(val listId: ToDoListId) : Event {
    override val meta = EventMeta(Subject.ToDoList)
    override fun toString() = "ToDoListCreateRequested"
}
data class ToDoListCreated(val listId: ToDoListId) : Event {
    override val meta = EventMeta(Subject.ToDoList)
}

// ----------
data class ToDoListSaveRequested(val listId: ToDoListId, val name: ToDoListName) : Event {
    override val meta = EventMeta(Subject.ToDoList)
}
data class ToDoListSaved(val toDoList: ToDoList) : Event {
    override val meta = EventMeta(Subject.ToDoList)
}
data class ToDoListNotSaved(val reason: String) : Event {
    override val meta = EventMeta(Subject.ToDoList)
}

// ----------

data class ToDoListDeleteRequested(val listId: ToDoListId) : Event {
    override val meta = EventMeta(Subject.ToDoList)
}
class ToDoListDeleted : Event {
    override val meta = EventMeta(Subject.ToDoList)
    override fun toString() = "ToDoListDeleted"
}
