package com.example.demo.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConsumerRegistry implements DisposableBean {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ConcurrentHashMap<String, Disposable> map = new ConcurrentHashMap<>();


    public void add(String name, Disposable disposable){
        map.put(name, disposable);
    }

    @Override
    public void destroy() throws Exception {
        map.forEach((name, consumer) -> {
            log.info("Stopping {}" , name);
            consumer.dispose();
        });
    }

    ConcurrentHashMap<String, Disposable> getMap() {
        return map;
    }

}
