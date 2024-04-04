package dev.oblac.tudu.ether

/**
 * Function that processes an input event and returns a resulting event.
 * This is the encapsulation of the processing logic.
 * Function are connected in a chain to form a processing pipeline.
 */
fun interface Pipe<in IN : Event> {
    operator fun invoke(event: IN): Event
}
