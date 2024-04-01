package dev.oblac.tudu

import dev.oblac.tudu.app.TodoListsProjection
import dev.oblac.tudu.domain.ToDoListId
import dev.oblac.tudu.domain.ToDoListName
import dev.oblac.tudu.event.*
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.Router
import java.util.*

class MainVerticle : AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        val router = Router.router(vertx)

        router
            .get("/hello")
            .handler { routingContext ->
                val response = routingContext.response()
                response.putHeader("content-type", "text/plain")
                response.end("Hello from Vert.x!")
            }

        router
            .get("/list")
            .handler { queryToDoLists(it.response()) }

        router
            .post("/list")
            .handler { createDraftToDoList(it.response()) }

        router
            .post("/b-list")
            .handler { createDraftToDoListBlocking(it.response()) }

        router
            .post("/list/:id/:name")
            .handler {
                val id = it.request().getParam("id")
                val name = it.request().getParam("name")
                saveDraftToDoList(it.response(), ToDoListId(UUID.fromString(id)), ToDoListName(name))
            }

        router
            .get("/ops/:id")
            .handler {
                val id = it.request().getParam("id")
                val result = UIOperations.of(id)
                if (result == null) {
                    it.response().setStatusCode(404)
                    it.response().end("Operation not found")
                    return@handler
                }
                result.fold(
                    { event -> it.response().end("Success: $event") },
                    { failure -> it.response().end("Failure: ${failure.cause}") }
                )
            }



        // start server
        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(8888) { http ->
                if (http.succeeded()) {
                    startPromise.complete()
                    println("HTTP server started on port 8888")
                } else {
                    startPromise.fail(http.cause());
                }
            }
    }
}

// handlers

fun createDraftToDoListBlocking(response: HttpServerResponse) {
    ether.emit(ToDoListCreateRequested(ToDoListId.new())) { event, finish ->
        if (event is ToDoListCreated) {
            response.putHeader("content-type", "text/plain")
            response.end("LIST created: ${event.listId}")
            finish()
        }
    }
}

fun createDraftToDoList(response: HttpServerResponse) {
    val id = ToDoListId.new()                   // todo should come from the Store
    ether.emit(ToDoListCreateRequested(id))
    response.putHeader("content-type", "text/plain")
    response.statusCode = 201
    response.end("LIST created: $id")
}

fun saveDraftToDoList(response: HttpServerResponse, listId: ToDoListId, name: ToDoListName) {
    val operationId = UIOperations.start()

    ether.emit(ToDoListSaveRequested(listId, name)) { event, finish ->
        if (event is ToDoListSaved) {
            UIOperations.success(operationId, event)
            finish()
        } else if (event is ToDoListNotSaved) {
            UIOperations.failure(operationId, event)
            finish()
        }
    }
    response.putHeader("content-type", "text/plain")
    response.statusCode = 201
    response.end("LIST to be saved: $listId as $name (operationId: $operationId)")
}


fun saveDraftToDoListBlocking(response: HttpServerResponse, listId: ToDoListId, name: ToDoListName) {
    ether.emit(ToDoListSaveRequested(listId, name)) { event, finish ->
        if (event is ToDoListSaved) {
            response.putHeader("content-type", "text/plain")
            response.end("LIST save: ${event.toDoList.id} as ${event.toDoList.name}")
            finish()
        } else if (event is ToDoListNotSaved) {
            response.putHeader("content-type", "text/plain")
            response.setStatusCode(500)
            response.end("LIST not saved: ${event.reason}")
            finish()
        }
    }
}

fun queryToDoLists(response: HttpServerResponse) {
    response.putHeader("content-type", "text/plain")
    response.end(TodoListsProjection.todoLists.toString())
}
