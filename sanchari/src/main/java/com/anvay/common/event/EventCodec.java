package com.anvay.common.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/** Reads the payload out of an EventEnvelope JSON string. */
@Component
public class EventCodec {

    private final ObjectMapper mapper;

    public EventCodec(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> T payload(String json, Class<T> type) {
        try {
            JsonNode tree = mapper.readTree(json);
            return mapper.treeToValue(tree.get("payload"), type);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse event as " + type.getSimpleName(), e);
        }
    }
}
