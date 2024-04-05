package dev.oblac.tudu

import dev.oblac.tudu.app.*
import dev.oblac.ether.nats.NatsEther
import dev.oblac.ether.nats.NatsEtherNgn
import dev.oblac.ether.Piper
import dev.oblac.tudu.event.Subject
import dev.oblac.tudu.matter.StoreMatter

private fun ether(): NatsEther {
    val ngn = NatsEtherNgn("nats://localhost:4222")

    val matter = StoreMatter()

    // here we need to bind everything we want to handle in this instance

    ngn.bind(Subject.ToDoList)
    ngn.bind(createToDoList)
    Piper(matter)(saveToDoList).let(ngn::bind)
    ngn.bind(updateToDoList)

    return NatsEther(ngn)
}

val ether = ether()
