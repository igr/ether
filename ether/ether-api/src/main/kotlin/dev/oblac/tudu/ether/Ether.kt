package dev.oblac.tudu.ether

/**
 * The Ether is the main interface for the events bus.
 */
interface Ether {
    /**
     * Emits an event to the bus, fire-and-forget style.
     * Events are processed asynchronously by the mesh of pipes.
     * @see Pipe
     */
    fun emit(event: Event)

    /**
     * Emits an event but also listens to all events happening
     * in the _same_ context!
     */
    fun emit(event: Event, msgHandler: EtherInPlaceMessageHandler)

}

fun interface EtherInPlaceMessageHandler {
    operator fun invoke(event: Event, finish: () -> Unit)
}

/**
 * The engine of the Ether.
 */
interface EtherNgn {
    /**
     * Turns the engine OFF.
     */
    fun off()

    /**
     * Registers a new subject to the engine.
     */
    fun bind(subject: EventSubject)

    // todo solve this Kotlin reflection issue better
    //fun <IN : Event> bind(pipe: Pipe<IN>)

    /**
     * Looks up the pipes that are interested in the event.
     */
    fun <T : Event> lookup(event: T): List<Pipe<Event>>
}
