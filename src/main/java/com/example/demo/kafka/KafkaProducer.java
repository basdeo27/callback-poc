package com.example.demo.kafka;

import com.example.demo.domain.ExternalCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

@Service
public class KafkaProducer {

    private final KafkaSender<String, String> kafkaRetrySender;
    private final ObjectMapper mapper = new ObjectMapper();

    public KafkaProducer(final ProducerProperties producerProperties) {
        this.kafkaRetrySender = producerProperties.retryKafkaSender();
    }

    private SenderRecord<String, String, String> createCallbacksSenderRecord(String messageId, ExternalCallback externalCallback, String topic) {
        ProducerRecord<String, String> record =  new ProducerRecord<>(topic,
                                                                      messageId,
                                                                      getJson(externalCallback));

        return SenderRecord.create(record, messageId);
    }

    public Mono<SenderResult<String>> sendCallbacks(String messageId, ExternalCallback callback, String topic) {
        SenderRecord<String, String, String> senderRecord = createCallbacksSenderRecord(messageId, callback, topic);
        return kafkaRetrySender.send(Flux.just(senderRecord)).last();
    }

    private String getJson(ExternalCallback externalCallback) {
        String json = "";
        try {
            json = mapper.writeValueAsString(externalCallback);
        } catch (Exception e) {

        }
        return json;
    }

}
