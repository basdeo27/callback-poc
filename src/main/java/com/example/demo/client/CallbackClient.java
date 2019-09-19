package com.example.demo.client;

import com.example.demo.domain.ExternalCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CallbackClient {

    private static final Logger Log = LoggerFactory.getLogger(CallbackClient.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient client = WebClient.create();

    public Mono<ExternalCallback> sendCallback(ExternalCallback callback) {
        String callbackJson = getJson(callback);
        Log.info("We are trying to send this callback - " + callbackJson);
        return client.post()
                .uri(callback.getUrl())
                .body(BodyInserters.fromObject(callbackJson))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> Log.error("We had an error trying to send the message"))
                .doOnSuccess(a -> Log.info("Success"))
                .then(Mono.just(callback));
    }

    private String getJson(ExternalCallback externalCallback) {
        String callbackJson = "";
        try {
            callbackJson = objectMapper.writeValueAsString(externalCallback);
        } catch (Exception e) {
            Log.info("Failed to parse callback as string");
        }
        return callbackJson;
    }
}
