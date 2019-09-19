package com.example.demo.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "nexmo-chatapp-callback.kafka")
public class ProducerProperties {

    private Properties retrySenderOptions;

    KafkaSender<String, String> retryKafkaSender() {
        return KafkaSender.create(SenderOptions.create(this.retrySenderOptions));
    }

    public Properties getRetrySenderOptions() {
        return retrySenderOptions;
    }

    public void setRetrySenderOptions(Properties retrySenderOptions) {
        this.retrySenderOptions = retrySenderOptions;
    }
}
