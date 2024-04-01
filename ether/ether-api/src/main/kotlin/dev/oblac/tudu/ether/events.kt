package dev.oblac.tudu.ether

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant

@JvmInline
value class EventSubject(private val value: String) {
    override fun toString() = value
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
interface Event {
    val meta: EventMeta
}

class EventMeta(val subject: EventSubject) {
    val timestamp: Long = Instant.now().toEpochMilli()  // todo use Instant
}

object BlackHole : Event {
    override val meta: EventMeta = EventMeta(EventSubject("BlackHole"))
}
