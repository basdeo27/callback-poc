package com.example.demo.kafka;

import com.example.demo.client.CallbackClient;
import com.example.demo.domain.ExternalCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class WebClientProcessor {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private CallbackClient callbackClient;

    public WebClientProcessor(CallbackClient callbackClient) {
        this.callbackClient = callbackClient;
    }

    public Mono<ExternalCallback> process(ExternalCallback message) {
        return callbackClient.sendCallback(message)
                .doOnEach(callbacks ->  log.info("Resolved callbacks: {}", callbacks))
                .doOnError(e -> log.error("An error occurred while resolving callbacks", e));
    }
}
