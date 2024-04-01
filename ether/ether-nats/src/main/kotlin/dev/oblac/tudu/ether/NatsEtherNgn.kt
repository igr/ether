package dev.oblac.tudu.ether

import io.nats.client.JetStream
import io.nats.client.JetStreamManagement
import io.nats.client.Nats
import io.nats.client.api.RetentionPolicy
import io.nats.client.api.StorageType
import io.nats.client.api.StreamConfiguration
import java.time.Duration

class NatsEtherNgn(natsUrl: String) : EtherNgn {

    internal val nc = Nats.connect(natsUrl)
    private val jsm: JetStreamManagement = nc.jetStreamManagement()
    internal val dispatcher = nc.createDispatcher()
    val js: JetStream = nc.jetStream()

    override fun off() {
        nc.close()
    }

    // subjects

    val subjects = mutableSetOf<EventSubject>()

    @Synchronized
    fun bind(subject: EventSubject) {
        if (subject in subjects) {
            return
        }

        val streamName = subject.toString() + "Stream"
        val subjectName = subject.toString()

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

        subjects.add(subject)
    }

    // pipes

    val commands = mutableMapOf<String, MutableList<Pipe<Event>>>()

    @Synchronized
    inline fun <reified IN : Event> bind(pipe: Pipe<IN>) {
        val key = IN::class.qualifiedName!!
        val list = commands.computeIfAbsent(key) { mutableListOf() }
        list.add(pipe as Pipe<Event>)
    }

    fun <T : Event> lookup(event: T): List<Pipe<Event>> {
        val key = event::class.qualifiedName!!
        return commands[key] ?: listOf()
    }
}
