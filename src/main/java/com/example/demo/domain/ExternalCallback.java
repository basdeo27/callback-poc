package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalCallback {

    @JsonIgnore
    private static final long FIFTEEN_MINUTES_MILLIS = 900000L;
    @JsonIgnore
    private static final long ONE_MINUTES_MILLIS = 60000L;
    @JsonIgnore
    private static final long TEN_SECONDS_MILLIS = 10000L;
    @JsonIgnore
    public static final long RETRY_INTERVAL = TEN_SECONDS_MILLIS;


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

    public ExternalCallback(ExternalCallback externalCallback) {
        this.url = externalCallback.getUrl();
        this.body = externalCallback.getBody();
        this.start = System.currentTimeMillis();
        this.nextRetry = start + RETRY_INTERVAL;
    }

    @JsonCreator
    public ExternalCallback(@JsonProperty("url") String url,
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
        nextRetry += RETRY_INTERVAL;
    }

    public long getMillisUntilNextRetry() {
        return nextRetry - start;
    }

}
