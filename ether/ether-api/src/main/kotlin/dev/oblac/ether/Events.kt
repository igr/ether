package dev.oblac.ether

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant

/**
 * Event Realms.
 * All events with the same realm should be processed by the same dispatcher.
 */
@JvmInline
value class EventRealm(private val value: String) {
    override fun toString() = value
}

/**
 * Event.
 */
// todo remove Jackson dependency
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
interface Event {
    val meta: EventMeta
}

/**
 * Event's metadata.
 */
class EventMeta(val realm: EventRealm) {
    val timestamp: Long = Instant.now().toEpochMilli()  // todo use Instant
}

/**
 * The sinkhole event.
 */
object BlackHole : Event {
    override val meta: EventMeta = EventMeta(EventRealm("BlackHole"))
}
