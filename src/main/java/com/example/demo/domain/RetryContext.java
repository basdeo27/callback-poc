package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RetryContext {
    @JsonIgnore
    private static final long FIFTEEN_MINUTES_MILLIS = 900000L;
    @JsonIgnore
    private static final long ONE_MINUTES_MILLIS = 60000L;
    @JsonIgnore
    private static final long TEN_SECONDS_MILLIS = 10000L;

    private final String url;
    private final String body;
    private final long start;
    private long nextRetry;

   /*
    @JsonCreator
    public ExternalCallback(@JsonProperty("url") String url,
                            @JsonProperty("body") String body) {
        this.url = url;
        this.body = body;
    }
    */

    public RetryContext(String url, String body) {
        this.url = url;
        this.body = body;
        this.start = System.currentTimeMillis();
        this.nextRetry = start + TEN_SECONDS_MILLIS;
    }

    @JsonCreator
    public RetryContext(@JsonProperty("url") String url,
                        @JsonProperty("body") String body,
                        @JsonProperty("start") long start,
                        @JsonProperty("nextRetry") long nextRetry) {
        this.url = url;
        this.body = body;
        this.start = start;
        this.nextRetry = nextRetry;
    }

    public String getUrl() {
        return url;
    }

    public String getBody() {
        return body;
    }

    public long getStart() {
        return start;
    }

    public long getNextRetry(){
        return nextRetry;
    }

    public void incrementNextRetry() {
        nextRetry += TEN_SECONDS_MILLIS;
    }

    public long getMillisUntilNextRetry() {
        return nextRetry - start;
    }
}
