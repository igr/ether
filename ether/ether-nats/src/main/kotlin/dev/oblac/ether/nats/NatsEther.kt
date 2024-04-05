package dev.oblac.ether.nats

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.oblac.ether.*
import io.nats.client.*
import io.nats.client.impl.NatsMessage
import org.slf4j.LoggerFactory
import java.time.Duration

class NatsEther(val ngn: NatsEtherNgn) : Ether {
    private val log = LoggerFactory.getLogger(this.javaClass)

    // we need some sort of serializer
    // todo make serializer implementation pluggable
    private val mapper = jacksonObjectMapper()

    init {
        ngn.forEachRealm(::realmSubscriber)
    }

    /**
     * Creates a subscriber for each registered realm.
     */
    private fun realmSubscriber(realm: EventRealm, dispatcher: Dispatcher) {
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

        val streamName = realm.toString() + "Stream"
        val subjectName = realm.toString()

        val so = PushSubscribeOptions.builder()
            .stream(streamName)
            .durable("$streamName-Durable")
            .build()

        // we are using the same dispatcher for all subjects
        // this means that all messages will be processed in the same thread
        ngn.js.subscribe("${subjectName}.*", dispatcher, handler, false, so)
        log.info("Subscribed to $subjectName")
    }

    override fun emit(event: Event, msgHandler: EtherInPlaceMessageHandler) {
        val streamName = event.meta.realm.toString() + "Stream"
        val subjectName = event.meta.realm.toString()
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
            // finisher is added to end the queue and remove the subscription
        }

        val dispatcher = ngn.dispatcherOf(event.meta.realm)
        s = ngn.js.subscribe(emitSubjectName, dispatcher, handler, true, so)

        // emit
        val msg: Message = NatsMessage.builder()
            .subject(emitSubjectName)
            .data(mapper.writeValueAsBytes(event))
            .build()

        log.info("Emit-A event to $emitSubjectName : $event")
        ngn.js.publishAsync(msg)
    }

    override fun emit(event: Event) {
        val subjectName = event.meta.realm.toString()
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

        log.info("Emit-J event to $subject : $event")
        ngn.js.publishAsync(msg)
    }
}
