package dev.oblac.tudu.matter

import dev.oblac.tudu.app.SaveToDoListState
import dev.oblac.ether.Event
import dev.oblac.ether.Matter
import dev.oblac.tudu.event.ToDoListSaveRequested
import dev.oblac.tudu.event.ToDoListSaved

/**
 * Simple implementation of a Matter that uses a Store to load and save state.
 */
class StoreMatter : Matter {
    override fun <S> loadState(event: Event): S {
        return when (event) {
			is ToDoListSaveRequested -> {
                SaveToDoListState(
                    name = event.name,
                    draftToDoList = Store.findDraftToDoList(event.listId),
                    existingTodoListByName = Store.findToDoListByName(event.name)
                ) as S
			}
			else -> {
				throw IllegalArgumentException("Unsupported event: $event")
			}
		}
    }

    override fun <SIN> saveState(state: SIN, event: Event) {
        when (event) {
            is ToDoListSaved -> {
				val toDoList = event.toDoList
				Store.deleteDraftToDoList(toDoList.id)
				Store.saveToDoList(toDoList.id, toDoList.name)      // todo is this correct?
			}
			else -> {
				throw IllegalArgumentException("Unsupported event: $event")
			}
        }
    }

    // we dont need cancel implementation
}
