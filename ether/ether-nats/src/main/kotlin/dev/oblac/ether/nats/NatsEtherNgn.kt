package dev.oblac.ether.nats

import dev.oblac.ether.EtherNgn
import dev.oblac.ether.Event
import dev.oblac.ether.EventRealm
import dev.oblac.ether.Pipe
import io.nats.client.Dispatcher
import io.nats.client.JetStream
import io.nats.client.JetStreamManagement
import io.nats.client.Nats
import io.nats.client.api.RetentionPolicy
import io.nats.client.api.StorageType
import io.nats.client.api.StreamConfiguration
import java.time.Duration

class NatsEtherNgn(natsUrl: String) : EtherNgn {

    private val nc = Nats.connect(natsUrl)
    private val jsm: JetStreamManagement = nc.jetStreamManagement()
    val js: JetStream = nc.jetStream()

    override fun off() {
        nc.close()
    }

    // subjects and dispatchers

    private val realms = mutableMapOf<EventRealm, Dispatcher>()

    fun forEachRealm(consumer: (EventRealm, Dispatcher) -> Unit) {
        realms.forEach(consumer)
    }

    internal fun dispatcherOf(realm: EventRealm): Dispatcher = realms[realm]!!

    @Synchronized
    override fun bind(realm: EventRealm) {
        if (realm in realms) {
            return
        }

        val streamName = realm.toString() + "Stream"
        val subjectName = realm.toString()

        // Build the configuration
        val sc = StreamConfiguration.builder()
            .name(streamName)
            .retentionPolicy(RetentionPolicy.Interest)
            .maxAge(Duration.ofHours(1))
            .maxMessages(1000)
            .storageType(StorageType.Memory)
            .subjects(subjectName, "${subjectName}.>")
            .build()

        jsm.addStream(sc)

        realms[realm] = nc.createDispatcher()
    }

    // pipes

    val commands = mutableMapOf<String, MutableList<Pipe<Event>>>()

    @Synchronized
    inline fun <reified IN : Event> bind(pipe: Pipe<IN>) {
        val key = IN::class.qualifiedName!!
        val list = commands.computeIfAbsent(key) { mutableListOf() }
        list.add(pipe as Pipe<Event>)
    }

    override fun <T : Event> lookup(event: T): List<Pipe<Event>> {
        val key = event::class.qualifiedName!!
        return commands[key] ?: listOf()
    }
}
