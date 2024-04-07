# **`Ether`** ♒️ & **`Matter`** ⚛️

Welcome to Event Drive thought experiment ended up as a blueprint for a small engine.

⚠️ This is `Either<Stupid, Great>`, still can't figure. Built in 3 days time.

Frankly, there is nothing new here; but I didn't see this exact combination of ideas in the wild. 🤷‍♂️

The premise:

> We can build distributed, scalable event-driven app with only **4** abstractions: `Pipe`, `Event`, `Realm`, `Ether`. If we add fifth: `Matter`, we can achieve a pure business logic.

Every app is an event-driven app.

## Pipes 🌊

```
Pipe == Input -> UnitOfWork -> Output
```

⭐️ The **unit of work** (UOW) is just a function of a _single_ input, producing a _single_ output. We can connect UOWs by their input/output types, like... pipes. Hence we refer the UOW as a **pipe**.

**Q**: Why single input/output? Bear with me. We can always aggregate any number of related objects into a single object, so it is not a limitation.

⭐️ A **pipe** is a function that takes a single _input_ and produces a single _output_. It represents a **unit of work**. Due to singular input/outputs, pipes can be connected to each other, forming a mesh.

We may say that the application is a mesh of pipes, connected together. Here is a beautiful schema that illustrates the idea:

![](./doc/mesh.png)

Blue arrow represent pipes, connected to each other.

Example of a pipe:

```kt
val createToDoList = Pipe<ToDoListCreateRequested> {
    val list = Store.createNewDraftToDoList(it.listId)
    ToDoListCreated(list.id)
}
```

## Events ⚡️

```
Event == Fact, Message, Input, Output
```

⭐️ `Event` has a multitude of meanings. It is a **fact** that something happened. It is a **message** that is passed between pipes. It is an **input** to the pipe. It is an **output** of the pipe. It connects pipes together. Does that make pipe a _event handler_? Possibly. Is a pipe a _command_? Possibly.

⭐️ `Event` holds only the necessary data for the pipe to do its work. It is a simple data object. It is serializable.

⭐️ `BlackHole` is a sink event. It is a special event, used to terminate the event flow. It is like a `null` in the event world.

Pipes that are updating _projections_ are the ones that usually returns the `BlackHole` event.

## Realm 🌌

⭐️ Events belong to a **Realm**. Realm is a simple name that represents a boundary.

⭐️ Events are executed one after the another, in the single-threaded fashion, _within the same boundary_. This is important, as it allows us to have a consistent state of the application.

Having single-threaded pipe execution is a big deal, as it simplifies the state handling. We don't need to worry about the concurrent state changes. Realm allows parallel execution of the pipes in different realms.

⭐️ Realm is distributed, spread over the nodes.


## Ether ♒

```
Ether == Runner
```

⭐️ `Ether` is a glorious name for the Event bus engine abstraction that connects pipes and runs events on it. It is a very simple abstraction, that can be implemented in various ways. In this example, it is implemented with [NATS](https://nats.io/).

Event may be fired (and forget):

```kt
ether.emit(ToDoListCreateRequested(id))
```

This will trigger the execution of all pipes connected somehow to the initial event. The execution is asynchronous and non-blocking to the calling place.

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

Cool thing here is that provided lambda ONLY listens to events in the context of the current execution. It is NOT a global listener. Again, ONLY events that are created by pipes executed during this operation will be handled. This is cool when we want to have a listener that is only active during the current operation (non-blocking request/response)

⭐️ `Ether` is distributed! Pipes may be placed on different nodes:

![](./doc/mesh-2.png)

When the event `A` is fired, it will execute `foo` on node 1 and then `bar` on node 2.

⭐️  Pipe also may be horizontally scaled (⚠️ not implemented in this example). That would mean that the `foo` pipe is executed on multiple nodes, but only one of them will handle the event.

## Matter ⚛️

```
Pipe = Pure + Matter
```

We can go further with abstractions and remove explicit state handling from the `Pipe` functions. This is where the `Matter` comes in. It is a simple interface that knows how to:

+ load state from the storage for given (input) event
+ save state to the storage for given (resulting) event

This allows us to extract state handling and have **pure functions** that are only concerned with the business logic.

⭐️ `Matter` implementation is done by user:

```kt
class StoreMatter : Matter {
    override fun <S> loadState(event: Event): S {
        return when (event) {
            is ToDoListSaveRequested -> {
                SaveToDoListState(...)
            }
		}
    }

    override fun <SIN> saveState(state: SIN, event: Event) {
        when (event) {
            is ToDoListSaved -> {
                // save state
			}
        }
    }
}
```

Notice the `SaveToDoListState` - simple data class that holds the necessary input state for the business logic.

⭐️ Our `Pipe` may be designed now as a `Pure`:

```kt
val saveToDoList = Pure<ToDoListSaveRequested, SaveToDoListState> { _, it ->
    if (it.draftToDoList == null) {
        return@Pure ToDoListNotSaved("Draft list not found")
    }
    val newTodoList = ToDoList(it.draftToDoList.id, it.name)
    ToDoListSaved(newTodoList)
}
```

It is a pure function! Is it a `decider`/`evolver`? Possibly.

`Pure` function is transformed into a `Pipe` by the... well, `Piper`:

```kt
Piper(matter)(saveToDoList)
```

⭐️ `Matter` may be implemented in various ways:

+ transactional, traditional database
+ in-memory, for testing
+ event-sourced, for the event-sourced systems

## Infrastructure ⚙️

```
Instrastructure == Implementation
```

⭐️ Infrastructure is an implementation detail.

⭐️ [NATS](https://nats.io) cluster with JetStream - used as the _implementation_ of the `Ether` in the example. `Ether` itself has very simple interface (abstraction) that could be easily replaced with another event engine. Moreover, we can have an in-memory implementation for local development and testing.

With Nats, pipes can be deployed anywhere. There is also an option that I didn't have time to explore for horizontal scaling of the pipes (using Nats groups).

⚠️ I haven't spent much time on the infrastructure part, so it is a bit rough, maybe not working as expected.

⭐️ [VertX](https://vertx.io/) for the API layer - because of its async nature, VertX seem as an excellent choice for the API layer.


## Example 🎉

This very simple example illustrates the idea.

+ REST endpoint that triggers the creation of the ToDo list (async)
+ Operation tracker that returns the status of the operation (async)
+ Connected pipes
+ Update of the projection
+ In-Place handler
+ Distributed pipes

The storage atm is just a simple in-memory map.

Check out the `http` folder.

## Should I stay or should I go? 🚶‍♂️‍➡️

I _feel_ potential in this engine, but I am just tired and can not think straight 🤷‍♂️ **Let me know.**

TODO:

+ [ ] Horizontal scaling of the pipes using Nats groups
+ [ ] Add Postgress example
+ [ ] Add Event Sourcing example

Finally:

+ If this make sense, I would like to thank: [Dejan](https://github.com/DejanMilicic), [Ivan](https://fraktalio.com). They know way more than me about this stuff.
+ If this is stupid, that's on me only :)
