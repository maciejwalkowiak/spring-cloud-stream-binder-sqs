package org.springframework.cloud.stream.binder.sqs.provisioning;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

/**
 * SQS specific implementation of {@link ProducerDestination}.
 *
 * @author Maciej Walkowiak
 */
public class SqsProducerDestination implements ProducerDestination {
    private final String name;
    private final String topicArn;

    public SqsProducerDestination(String name, String topicArn) {
        this.name = name;
        this.topicArn = topicArn;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNameForPartition(int partition) {
        return name;
    }

    public String getTopicArn() {
        return topicArn;
    }
}
