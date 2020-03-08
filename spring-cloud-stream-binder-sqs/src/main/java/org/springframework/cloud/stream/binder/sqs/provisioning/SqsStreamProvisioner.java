package org.springframework.cloud.stream.binder.sqs.provisioning;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;

import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.cloud.stream.binder.sqs.properties.SqsConsumerProperties;
import org.springframework.cloud.stream.binder.sqs.properties.SqsProducerProperties;

import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * The {@link ProvisioningProvider} implementation for Amazon SQS. Provisions both SNS topics and SQS queues.
 *
 * @author Maciej Walkowiak
 */
public class SqsStreamProvisioner implements
                                  ProvisioningProvider<ExtendedConsumerProperties<SqsConsumerProperties>, ExtendedProducerProperties<SqsProducerProperties>> {

    private final AmazonSQSAsync amazonSQSAsync;
    private final AmazonSNSAsync amazonSNSAsync;

    public SqsStreamProvisioner(AmazonSQSAsync amazonSQSAsync, AmazonSNSAsync amazonSNSAsync) {
        this.amazonSQSAsync = amazonSQSAsync;
        this.amazonSNSAsync = amazonSNSAsync;
    }

    @Override
    public ProducerDestination provisionProducerDestination(String name,
                                                            ExtendedProducerProperties<SqsProducerProperties> properties) throws ProvisioningException {

        CreateTopicResult createTopicResult = amazonSNSAsync.createTopic(name);
        return new SqsProducerDestination(name, createTopicResult.getTopicArn());
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
                                                            ExtendedConsumerProperties<SqsConsumerProperties> properties) throws ProvisioningException {

        String queueName = properties.isPartitioned() ? group + "-" + properties.getInstanceIndex() : group;

        if (group == null || group.isEmpty()) { // anonymous queue
            queueName = name + "_anonymous_" + new Random().ints(22, 0, 62)
                .mapToObj(x -> String.valueOf(x < 36 ? Character.forDigit(x, 36) : (char)('A' - 36 + x)))
                .collect(Collectors.joining());
        }

        CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName)
                .withAttributes(properties.getExtension().getQueue() != null ? properties.getExtension().getQueue().toQueueAttributes() : Collections.emptyMap());
        CreateQueueResult createQueueResult = amazonSQSAsync.createQueue(createQueueRequest);

        CreateTopicResult createTopicResult = amazonSNSAsync.createTopic(name);
        String subscriptionArn = Topics.subscribeQueue(amazonSNSAsync,
                                                       amazonSQSAsync,
                                                       createTopicResult.getTopicArn(),
                                                       createQueueResult.getQueueUrl());

        if (properties.isPartitioned()) {
            amazonSNSAsync.setSubscriptionAttributes(subscriptionArn,
                                                     "FilterPolicy",
                                                     "{\"" + BinderHeaders.PARTITION_HEADER + "\": [" + properties.getInstanceIndex() + "]}");
        }

        return new SqsConsumerDestination(queueName);
    }
}
