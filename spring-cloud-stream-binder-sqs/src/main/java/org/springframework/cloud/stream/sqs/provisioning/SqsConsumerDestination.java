package org.springframework.cloud.stream.sqs.provisioning;

import org.springframework.cloud.stream.provisioning.ConsumerDestination;

/**
 * SQS specific implementation of {@link ConsumerDestination}.
 *
 * @author Maciej Walkowiak
 */
public class SqsConsumerDestination implements ConsumerDestination {

    private final String name;

    public SqsConsumerDestination(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
