package dev.oblac.tudu.app

import dev.oblac.tudu.Store
import dev.oblac.tudu.ether.BlackHole
import dev.oblac.tudu.ether.Pipe
import dev.oblac.tudu.event.ToDoListSaved

object TodoListsProjection {
    var todoLists = Store.todoLists()   // initial state
}

// eventually update
val updateToDoList = Pipe<ToDoListSaved> {
    println("Updating list: ${it.toDoList.id}")
    TodoListsProjection.todoLists = Store.todoLists()
    BlackHole
}
