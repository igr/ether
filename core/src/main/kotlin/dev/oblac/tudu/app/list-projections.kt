package dev.oblac.tudu.app

import dev.oblac.ether.BlackHole
import dev.oblac.ether.Pipe
import dev.oblac.tudu.event.ToDoListSaved
import dev.oblac.tudu.matter.Store

object TodoListsProjection {
    var todoLists = Store.todoLists()   // initial state
}

// eventually update
val updateToDoList = Pipe<ToDoListSaved> {
    println("Updating list: ${it.toDoList.id}")
    TodoListsProjection.todoLists = Store.todoLists()
    BlackHole
}
