package dev.oblac.tudu.ether

interface Ether {
//    fun emit(event: Event)
//
//    fun listen(consumer: (Event) -> Unit)

}

fun interface EtherMessageHandler {
    operator fun invoke(event: Event, finish: () -> Unit)
}

interface EtherNgn {
    /**
     * Turns the engine OFF.s
     */
    fun off()
}
