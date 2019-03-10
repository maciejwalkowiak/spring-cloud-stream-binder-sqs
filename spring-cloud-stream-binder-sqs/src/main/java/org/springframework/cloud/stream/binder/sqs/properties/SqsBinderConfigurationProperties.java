package org.springframework.cloud.stream.binder.sqs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The SQS-specific binder configuration properties.
 *
 * @author Maciej Walkowiak
 */
@ConfigurationProperties(prefix = "spring.cloud.stream.sqs.binder")
public class SqsBinderConfigurationProperties {
}
