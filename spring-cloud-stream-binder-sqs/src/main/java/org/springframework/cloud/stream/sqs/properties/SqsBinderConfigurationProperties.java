package org.springframework.cloud.stream.sqs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cloud.stream.sqs.binder")
public class SqsBinderConfigurationProperties {

    private String[] headers = new String[] {};

    public String[] getHeaders() {
        return this.headers;
    }

    public void setHeaders(String... headers) {
        this.headers = headers;
    }
}
