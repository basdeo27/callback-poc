package com.example.demo.kafka;

import com.example.demo.domain.ExternalCallback;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;

@Configuration
public class KafkaConfiguration {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final WebClientProperties webClientProperties;
    private final RetryProperties retryProperties;

    private KafkaReceiver<String, ExternalCallback> retryKafkaReceiver;
    private Flux<ReceiverRecord<String, ExternalCallback>> fluxRetry;
    private Flux<ReceiverRecord<String, ExternalCallback>> fluxWebClient;

    private KafkaProducer kafkaProducer;

    public KafkaConfiguration(WebClientProperties webClientProperties,
                              RetryProperties retryProperties,
                              KafkaProducer kafkaProducer) {
        this.webClientProperties = webClientProperties;
        this.retryProperties = retryProperties;
        this.kafkaProducer = kafkaProducer;
    }

    @Bean
    public Flux<ReceiverRecord<String, ExternalCallback>> webClientConsumer() {
        ReceiverOptions<String, ExternalCallback> options = webClientProperties.asReceiverOptions();
        fluxWebClient = KafkaReceiver.create(options).receive();
        return fluxWebClient;
    }

    @Bean
    public Flux<ReceiverRecord<String, ExternalCallback>> retryPropertiesFlux() {
        ReceiverOptions<String, ExternalCallback> options = retryProperties.asReceiverOptions();
        retryKafkaReceiver = KafkaReceiver.create(options);
        fluxRetry = retryKafkaReceiver.receive();
        return fluxRetry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startWebClientConsumer(ApplicationReadyEvent event) {

        Duration initBackoff = Duration.ofSeconds(1);
        Duration maxBackoff = Duration.ofMinutes(5);
        int maxRetries = 10;

        log.info("Starting http client consumer");
        WebClientProcessor processor = event.getApplicationContext().getBean(WebClientProcessor.class);
        ConsumerRegistry consumerRegistry = event.getApplicationContext().getBean(ConsumerRegistry.class);

        Disposable disposable = fluxWebClient.doOnNext(record -> record.receiverOffset().commit())
                .map(ConsumerRecord::value)
                .parallel()
                .runOn(Schedulers.elastic())
                .map(externalCallback -> new ExternalCallback(externalCallback))
                .flatMap(externalCallback -> processor.process(externalCallback)
                        .retryBackoff(maxRetries, initBackoff, maxBackoff)
                        .onErrorResume(e ->
                            kafkaProducer.sendCallbacks(String.valueOf(Math.random()),
                                                        externalCallback,
                                                        retryProperties.getTopic()).then(Mono.just(externalCallback))
                        ))
                .subscribe();

        consumerRegistry.add("callback-consumer", disposable);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void startRetryConsumer(ApplicationReadyEvent event) {
        log.info("Starting retry consumer");
        WebClientProcessor processor = event.getApplicationContext().getBean(WebClientProcessor.class);
        ConsumerRegistry consumerRegistry = event.getApplicationContext().getBean(ConsumerRegistry.class);

        Disposable disposable = fluxRetry.doOnNext(record -> {
            record.receiverOffset().commit();
           // pause(record);
        }
        )
                .map(ConsumerRecord::value)
                .parallel()
                .runOn(Schedulers.elastic())
                .flatMap(externalCallback -> processRetryCallback(processor, externalCallback))
                .subscribe();

        consumerRegistry.add("callback-consumer", disposable);
    }

    private boolean isTimeToProcessMessage(ExternalCallback externalCallback) {
        return System.currentTimeMillis() > externalCallback.getNextRetry();
    }

    private boolean contextHasNotExpired(ExternalCallback externalCallback) {
        if (System.currentTimeMillis() -  externalCallback.getStart() > 60000L) {
            return false;
        }
        return true;
    }

    private Mono<ExternalCallback> processRetryCallback(WebClientProcessor processor, ExternalCallback externalCallback) {
        if (isTimeToProcessMessage(externalCallback)) {
            return processor.process(externalCallback)
                    .onErrorResume(e -> {
                        if (contextHasNotExpired(externalCallback)) {
                            log.info("Producing callback to retry retry retry retry topic ", e);
                            externalCallback.incrementNextRetry();
                            return kafkaProducer.sendCallbacks(String.valueOf(Math.random()),
                                                               externalCallback,
                                                               retryProperties.getTopic()).then(Mono.just(externalCallback));
                        } else {
                            log.info("This record has now expired.");
                            return Mono.empty();
                        }
                    });
        } else {
            return kafkaProducer.sendCallbacks(String.valueOf(Math.random()),
                                        externalCallback,
                                        retryProperties.getTopic()).then(Mono.just(externalCallback));

        }
    }
/*
    private void pause(ReceiverRecord<String, ExternalCallback> receiverRecord) {
        try {
            Collection<TopicPartition> topicPartitions = Collections.singleton(new TopicPartition("retry-topic", receiverRecord.partition()));
            retryKafkaReceiver.doOnConsumer(consumer -> {
                consumer.pause(topicPartitions);
                return Mono.just(consumer);
            }).subscribe();
            long timeToSleep = receiverRecord.value().getNextRetry() - System.currentTimeMillis();
            while(timeToSleep > 0) {
                Thread.sleep(100L);
                timeToSleep -= 100;
            }
            retryKafkaReceiver.doOnConsumer(consumer -> {
                consumer.resume(topicPartitions);
                return Mono.just(consumer);
            }).subscribe();
        } catch (Exception e) {
            log.error("Thread sleep is interupted");
        }
    }
*/
}
