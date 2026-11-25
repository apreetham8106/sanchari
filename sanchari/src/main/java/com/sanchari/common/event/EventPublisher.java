package com.sanchari.common.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Publishes JSON-wrapped events to Kafka. Use publishAfterCommit inside @Transactional methods. */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public EventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.kafka = kafka;
        this.mapper = mapper;
    }

    public void publish(String topic, String key, String type, Object payload) {
        String json;
        try {
            json = mapper.writeValueAsString(new EventEnvelope<>(UUID.randomUUID(), type, Instant.now(), payload));
//        } catch (JsonProcessingException e) {
//            throw new IllegalStateException("Cannot serialize event " + type, e);
//        }
        
	    } catch (JsonProcessingException e) {
	        log.error("Failed to serialize event type: {}", type, e);
	        throw new IllegalStateException("Cannot serialize event " + type, e);
	    }
        kafka.send(topic, key, json).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} to {}", type, topic, ex);
            }
        });
    }

    /** Publishes after the surrounding DB transaction commits (or immediately if there is none). */
    public void publishAfterCommit(String topic, String key, String type, Object payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(topic, key, type, payload);
                }
            });
        } else {
            publish(topic, key, type, payload);
        }
    }
}
