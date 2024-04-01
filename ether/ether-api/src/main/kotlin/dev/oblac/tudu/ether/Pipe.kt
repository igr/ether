package dev.oblac.tudu.ether

fun interface Pipe<in IN : Event> {
    operator fun invoke(event: IN): Event
}
