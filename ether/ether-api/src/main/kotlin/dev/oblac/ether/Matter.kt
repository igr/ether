package dev.oblac.ether

/**
 * Pipes can be decomposed into two parts: the Matter and the [Pure].
 * The Matter is the part that deals with the state of the pipe,
 * while the [Pure] is the part that deals with the logic.
 * @see Pure
 * @see Pipe
 */
interface Matter {

    /**
     * Loads state for incoming event.
     */
    fun <S> loadState(event: Event): S

    /**
     * Saves state for resulting event.
     */
    fun <S> saveState(state: S, event: Event)

    /**
     * Cancels the state change, in case of error
     */
    fun <S> cancel(state: S) = Unit

}
