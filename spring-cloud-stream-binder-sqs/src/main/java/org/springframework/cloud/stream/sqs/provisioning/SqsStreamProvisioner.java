package org.springframework.cloud.stream.sqs.provisioning;

import com.amazonaws.services.sqs.AmazonSQSAsync;

import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.cloud.stream.sqs.properties.SqsConsumerProperties;
import org.springframework.cloud.stream.sqs.properties.SqsProducerProperties;

/**
 * The {@link ProvisioningProvider} implementation for Amazon SQS.
 *
 * TODO: enable applying queue properties.
 *
 * @author Maciej Walkowiak
 */
public class SqsStreamProvisioner implements
                                  ProvisioningProvider<ExtendedConsumerProperties<SqsConsumerProperties>, ExtendedProducerProperties<SqsProducerProperties>> {

    private final AmazonSQSAsync amazonSQSAsync;

    public SqsStreamProvisioner(AmazonSQSAsync amazonSQSAsync) {
        this.amazonSQSAsync = amazonSQSAsync;
    }

    @Override
    public ProducerDestination provisionProducerDestination(String name,
                                                            ExtendedProducerProperties<SqsProducerProperties> properties) throws ProvisioningException {
        return provisionDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
                                                            ExtendedConsumerProperties<SqsConsumerProperties> properties) throws ProvisioningException {
        return provisionDestination(name);
    }

    private SqsDestination provisionDestination(String name) {
        amazonSQSAsync.createQueue(name);
        return new SqsDestination(name);
    }
}
