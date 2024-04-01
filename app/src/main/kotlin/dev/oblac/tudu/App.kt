package dev.oblac.tudu

import dev.oblac.tudu.app.createToDoList
import dev.oblac.tudu.app.saveToDoList
import dev.oblac.tudu.app.updateToDoList
import dev.oblac.tudu.ether.NatsEther
import dev.oblac.tudu.ether.NatsEtherNgn
import dev.oblac.tudu.event.Subject

fun init(): NatsEther {
    val ngn = NatsEtherNgn("nats://localhost:4222")

    ngn.bind(Subject.ToDoList)
    ngn.bind(createToDoList)
    ngn.bind(saveToDoList)
    ngn.bind(updateToDoList)

    // todo add bind za listener

    return NatsEther(ngn)
}

val ether = init()


// projectsions


