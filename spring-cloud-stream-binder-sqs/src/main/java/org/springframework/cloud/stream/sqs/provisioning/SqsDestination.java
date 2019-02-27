package org.springframework.cloud.stream.sqs.provisioning;

import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;

/**
 * SQS specific implementation of {@link ConsumerDestination} and {@link ProducerDestination}.
 *
 * @author Maciej Walkowiak
 */
public class SqsDestination implements ConsumerDestination, ProducerDestination {

    private final String name;

    public SqsDestination(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNameForPartition(int partition) {
        return name;
    }
}
