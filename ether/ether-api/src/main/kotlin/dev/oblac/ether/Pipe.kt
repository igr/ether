package dev.oblac.ether

/**
 * Function that processes an input event and returns a resulting event.
 * This is the encapsulation of the processing logic.
 * Function are connected in a chain to form a processing pipeline.
 * Pipe can be decomposed into two parts: the [Matter] and the [Pure].
 */
fun interface Pipe<IN : Event> {
    operator fun invoke(event: IN): Event
}

/**
 * Pure is a function that processes an input event and returns a resulting event.
 * It is the encapsulation of the processing logic.
 * The state is handled by the [Matter].
 * @see Matter
 */
fun interface Pure<IN: Event, SIN> {
    operator fun invoke(event: IN, state: SIN): Event
}

/**
 * Piper is a function that creates a [Pipe] from a [Pure] function using the [Matter] to handle the state.
 * @see Matter
 */
class Piper(private val matter: Matter) {
    operator fun <IN : Event, SIN> invoke(pure: Pure<IN, SIN>): Pipe<IN> {
        return Pipe { event ->
            val stateIn = matter.loadState<SIN>(event)
            try {
                val eventOut = pure(event, stateIn)
                matter.saveState(stateIn, eventOut)
                eventOut
            } catch (e: Exception) {
                matter.cancel(stateIn)
                throw e
            }
        }
    }

}
