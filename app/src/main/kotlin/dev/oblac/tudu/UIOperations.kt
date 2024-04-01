package dev.oblac.tudu

import dev.oblac.tudu.ether.Event
import java.util.*

object UIOperations {

    // todo add timestamps and timeouts
    private val map = mutableMapOf<UUID, Result<Event>>()

    fun start(): UUID {
        return UUID.randomUUID()
    }

    fun success(operationId: UUID, event: Event) {
        map[operationId] = Result.success(event)
    }
    fun failure(operationId: UUID, event: Event) {
        map[operationId] = Result.failure(Failure(event))
    }

    fun of(id: String?): Result<Event>? {
        return map.remove(UUID.fromString(id))
    }

    class Failure(val event: Event) : RuntimeException()
}
