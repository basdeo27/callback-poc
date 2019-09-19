package com.example.demo.serialize;

import com.example.demo.domain.ExternalCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ExternalCallbackDeserializer implements Deserializer<ExternalCallback> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ExternalCallback deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, ExternalCallback.class);
        } catch (IOException e) {
            log.error("Failed to deserialize [{}] [{}]", new String(data), topic);
            return null;
        }
    }


}
