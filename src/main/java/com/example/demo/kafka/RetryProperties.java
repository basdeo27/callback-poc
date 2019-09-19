package com.example.demo.kafka;

import com.example.demo.domain.ExternalCallback;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.ReceiverOptions;

import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "nexmo-chatapp-callback.kafka.consumer-retry")
public class RetryProperties {

    @NotEmpty
    private String topic;
    @NotEmpty
    private Map<String, Object> properties;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getTopic() {
        return topic;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public ReceiverOptions<String, ExternalCallback> asReceiverOptions(){
        if (properties == null){
            properties = new HashMap<>();
        }
        ReceiverOptions<String, ExternalCallback> ops = ReceiverOptions.create(properties);
        return ops.subscription(Collections.singleton(topic))
                .commitBatchSize(10);
    }

    @Override
    public String toString() {
        return "CallbackConsumerProperties{" +
                "properties=" + properties +
                "topic=" + topic +
                '}';
    }
}
