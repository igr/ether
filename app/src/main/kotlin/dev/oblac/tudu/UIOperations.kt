package dev.oblac.tudu

import dev.oblac.ether.Event
import java.util.*

/**
 * Dead simple in-memory operation status tracker.
 * We never return the actual status of the operation, as we are not blocking anything.
 * Here we store the operation statuses asynchronously, and the client can poll for the status.
 * This is a very simple and dump implementation, just to show the concept.
 * todo this probably should be renamed and move to Ether.
 */
object UIOperations {

    // todo add timestamps and timeouts and cleanup
    private val map = mutableMapOf<UUID, Result<Event>>()

    // todo add record in the map
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

    /**
     * Simple error storage compatible with Kotlin's Result.
     */
    class Failure(val event: Event) : RuntimeException()
}
