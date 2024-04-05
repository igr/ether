package dev.oblac.tudu.matter

import dev.oblac.tudu.domain.*
import java.lang.Thread.sleep
import kotlin.random.Random

/**
 * Simulates a slow store with blocking IO operations.
 * Very simple implementation, just to show the idea.
 * This is a repository.
 */
object Store {

    private val draftLists = mutableMapOf<ToDoListId, DraftToDoList>()
    private val lists = mutableMapOf<ToDoListId, ToDoList>()
    private val items = mutableMapOf<ToDoListId, MutableList<ToDoItem>>()
    private val rnd = Random.Default

    /**
     * Simulate a slow operation by sleeping for a random time (10-15seconds).
     */
    private fun sleep() = sleep(10_000 + rnd.nextLong(0, 5000))

    fun createNewDraftToDoList(id: ToDoListId): DraftToDoList {
        sleep()
        return draftLists.computeIfAbsent(id) {
            DraftToDoList(it)
        }
    }

    fun findDraftToDoList(id: ToDoListId): DraftToDoList? {
        return draftLists[id]
    }

    fun saveToDoList(id: ToDoListId, name: ToDoListName): ToDoList {
        sleep()
        val newList = ToDoList(id, name)
        lists[id] = newList
        return newList
    }

    fun deleteDraftToDoList(id: ToDoListId): Boolean {
        sleep()
        return draftLists.remove(id) != null
    }

    fun findToDoListByName(name: ToDoListName): ToDoList? {
        return lists.values.find { it.name == name }
    }

    fun deleteToDoList(listId: ToDoListId) {
        sleep()
        lists.remove(listId)
        items.remove(listId)
    }

    fun todoLists(): List<ToDoList> {
        return lists.values.toList()
    }
}
