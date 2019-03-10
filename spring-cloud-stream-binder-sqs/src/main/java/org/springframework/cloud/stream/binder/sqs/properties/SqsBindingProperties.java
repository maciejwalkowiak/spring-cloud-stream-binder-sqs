package org.springframework.cloud.stream.binder.sqs.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * The SQS-specific binding configuration properties.
 *
 * @author Maciej Walkowiak
 */
public class SqsBindingProperties implements BinderSpecificPropertiesProvider {

    private SqsConsumerProperties consumer = new SqsConsumerProperties();

    private SqsProducerProperties producer = new SqsProducerProperties();

    @Override
    public SqsConsumerProperties getConsumer() {
        return consumer;
    }

    @Override
    public SqsProducerProperties getProducer() {
        return producer;
    }

    public void setConsumer(SqsConsumerProperties consumer) {
        this.consumer = consumer;
    }

    public void setProducer(SqsProducerProperties producer) {
        this.producer = producer;
    }
}
