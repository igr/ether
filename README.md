# Tu.Du.

Event Driven Architecture POC on NATS and VertX.

⚠️ This is `Either<Stupid, Great>`, I still can not figure. It's built in 2 days time.

## Key concepts

⭐️ Every app is event-driven. The **unit of work** (UOW) is just a function of a _single_ input, producing a _single_ output. We can connect UOWs by their input/output types, like... pipes. Hence we refer the UOW as a **pipe**.

⭐️ A pipe is a function that takes an input and produces an output. It represents a **unit of work**. Pipes can be connected to each other, forming a mesh.

Example of a pipe:

```kt
val createToDoList = Pipe<ToDoListCreateRequested> {
    val list = Store.createNewDraftToDoList(it.listId)
    ToDoListCreated(list.id)
}
```

⭐️ `Ether` is a glorious name for the Event bus that connects pipes. It is a simple abstraction over some event engine, providing a way to publish and subscribe to events.

Once created, an event may be fired (and forget) like this:

```kt
ether.emit(ToDoListCreateRequested(id))
```

This will trigger the execution of all pipes connected somehow to the initial event. The execution is asynchronous and non-blocking.

Event may be fired with a in-place listener:

```kt
ether.emit(ToDoListSaveRequested(listId, name)) { event, finish ->
    if (event is ToDoListSaved) {
        UIOperations.success(operationId, event)
        finish()
    } else if (event is ToDoListNotSaved) {
        UIOperations.failure(operationId, event)
        finish()
    }
}
```

Cool thing here is that provided lambda ONLY listens to events in the context of the current execution. It is NOT a global listener. So, ONLY events that are created by pipes executed during this operation will be handled.

⭐️ `BlackHole` is a sink event. It is a special event that is not emitted by any pipe. It is used to terminate the event flow. It is like a `null` in the event world.

Pipes that are dealing with _projections_ are the ones that usually emit the `BlackHole` event.

⭐️ Events are executed one after the another, in the single-threaded dispatcher.

## Infrastructure

+ NATS cluster for JetStream
+ VertX for the API
