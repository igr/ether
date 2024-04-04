package dev.oblac.tudu.ether

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.nats.client.JetStreamSubscription
import io.nats.client.Message
import io.nats.client.MessageHandler
import io.nats.client.PushSubscribeOptions
import io.nats.client.impl.NatsMessage
import org.slf4j.LoggerFactory
import java.time.Duration

class NatsEther(private val ngn: NatsEtherNgn) : Ether {
    private val log = LoggerFactory.getLogger(this.javaClass)

    // we need some sort of serializer
    // todo make serializer implementation pluggable
    private val mapper = jacksonObjectMapper()

    init {
        ngn.subjects.forEach {
            subjectSubscriber(it)
        }
    }

    /**
     * Creates a subscriber for each registered subject.
     * Subjects represents the boundary?
     */
    private fun subjectSubscriber(subject: EventSubject) {
        val handler = MessageHandler { msg ->
            val json = String(msg.data)
            log.debug("Received message: $json")

            val event = mapper.readValue<Event>(json)

            ngn.lookup(event)
                .map {
                    it(event)
                }.filter {
                    it !is BlackHole        // stop on BlackHoles
                }.forEach {
                    justEmit(msg.subject, it)
                }

            msg.ack()   // ack, even if we don't process it
        }

        val streamName = subject.toString() + "Stream"
        val subjectName = subject.toString()

        val so = PushSubscribeOptions.builder()
            .stream(streamName)
            .durable("$streamName-Durable")
            .build()

        // we are using the same dispatcher for all subjects
        // this means that all messages will be processed in the same thread
        // todo create a dispatcher for each subject?
        ngn.js.subscribe("${subjectName}.*", ngn.dispatcher, handler, false, so)
        log.info("Subscribed to $subjectName")
    }

    override fun emit(event: Event, msgHandler: EtherInPlaceMessageHandler) {
        val streamName = event.meta.subject.toString() + "Stream"
        val subjectName = event.meta.subject.toString()
        // append unique timestamp to the subject name, to avoid collisions and distinguish the events
        val emitSubjectName = "${subjectName}.${event.meta.timestamp}"

        // inner subscribe
        var s: JetStreamSubscription? = null
        val so = PushSubscribeOptions.builder().stream(streamName).build()

        // in-place message handler for tracking ONLY the events in the context of the current emit
        val handler = MessageHandler { msg ->
            val json = String(msg.data)
            val msgEvent = mapper.readValue<Event>(json)
            log.info("Emit got it: $msgEvent")
            msgHandler(msgEvent) { s?.drain(Duration.ofMillis(100)) }   // todo do we need finishers?
        }
        s = ngn.js.subscribe(emitSubjectName, ngn.dispatcher, handler, true, so)

        // emit
        val msg: Message = NatsMessage.builder()
            .subject(emitSubjectName)
            .data(mapper.writeValueAsBytes(event))
            .build()

        log.info("Emit-A event to $emitSubjectName : $event")
        ngn.js.publishAsync(msg)
    }

    override fun emit(event: Event) {
        val subjectName = event.meta.subject.toString()
        val emitSubjectName = "${subjectName}.${event.meta.timestamp}"

        val msg: Message = NatsMessage.builder()
            .subject(emitSubjectName)
            .data(mapper.writeValueAsBytes(event))
            .build()

        log.info("Emit-B event to $emitSubjectName : $event")
        ngn.js.publishAsync(msg)
    }

    private fun justEmit(subject: String, event: Event) {
        val msg: Message = NatsMessage.builder()
            .subject(subject)
            .data(mapper.writeValueAsBytes(event))
            .build()

        log.info("Emit-I event to $subject : $event")
        ngn.js.publishAsync(msg)
    }

    fun listen(subjects: Array<EventSubject>, ha: (Event) -> Unit) {
        val handler = MessageHandler { msg ->
            val json = String(msg.data)
            log.debug("Received message: $json")

            val event = mapper.readValue<Event>(json)
            ha(event)
        }

        // val dispatcher = ngn.nc.createDispatcher()      // ???

        subjects.forEach { subject ->
            val streamName = subject.toString() + "Stream"
            val subjectName = subject.toString()

            val so = PushSubscribeOptions.builder()
                .stream(streamName)
                .durable("$streamName-Durable")
                .build()

            ngn.js.subscribe("${subjectName}.*", ngn.dispatcher, handler, false, so)
            log.info("Listen to $subjects")
        }
    }
}
