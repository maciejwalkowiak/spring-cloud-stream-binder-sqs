package org.springframework.cloud.stream.sqs.provisioning;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.sqs.LocalAwsResource;
import org.springframework.cloud.stream.sqs.properties.SqsConsumerProperties;
import org.springframework.cloud.stream.sqs.properties.SqsProducerProperties;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SqsStreamProvisionerTest {

    private AmazonSQSAsync sqs;
    private AmazonSNSAsync sns;
    private SqsStreamProvisioner sqsStreamProvisioner;

    @ClassRule
    public static LocalAwsResource localAwsResource = new LocalAwsResource();

    @Before
    public void setup() {
        AmazonSQSAsyncClientBuilder builder = AmazonSQSAsyncClientBuilder.standard();
        builder.setEndpointConfiguration(localAwsResource.getEndpoint(LocalStackContainer.Service.SQS));
        builder.setCredentials(localAwsResource.getCredentialsProvider());
        this.sqs = builder.build();

        AmazonSNSAsyncClientBuilder snsBuilder = AmazonSNSAsyncClientBuilder.standard();
        snsBuilder.setEndpointConfiguration(localAwsResource.getEndpoint(LocalStackContainer.Service.SNS));
        snsBuilder.setCredentials(localAwsResource.getCredentialsProvider());
        this.sns = snsBuilder.build();

        this.sqsStreamProvisioner = new SqsStreamProvisioner(sqs, sns);

        // clean up
        this.sqs.listQueues()
                .getQueueUrls()
                .forEach(queueUrl -> this.sqs.deleteQueue(queueUrl));
        this.sns.listTopics()
                .getTopics()
                .forEach(topic -> this.sns.deleteTopic(topic.getTopicArn()));
    }

    // partitioning disabled
    @Test
    public void returnsCreatedQueueNameWhenPartitioningDisabled() {
        ExtendedConsumerProperties<SqsConsumerProperties> consumerProperties = new ExtendedConsumerProperties<>(new SqsConsumerProperties());

        SqsConsumerDestination destination = (SqsConsumerDestination) sqsStreamProvisioner.provisionConsumerDestination("topic-name",
                                                                                                                        "group-name",
                                                                                                                        consumerProperties);

        assertThat(destination.getName()).isEqualTo("group-name");
    }

    @Test
    public void createsSqsQueueWhenPartitioningDisabled() {
        ExtendedConsumerProperties<SqsConsumerProperties> consumerProperties = new ExtendedConsumerProperties<>(new SqsConsumerProperties());

        sqsStreamProvisioner.provisionConsumerDestination("topic-name",
                                                          "group-name",
                                                          consumerProperties);

        List<String> queueUrls = sqs.listQueues("group-name").getQueueUrls();
        assertThat(queueUrls).hasSize(1);
        assertThat(sqs.getQueueAttributes(queueUrls.get(0), Arrays.asList("QueueArn")).getAttributes().get("QueueArn")).endsWith("group-name");
    }

    @Test
    public void createsSnsTopicWhenPartitioningDisabled() {
        ExtendedConsumerProperties<SqsConsumerProperties> consumerProperties = new ExtendedConsumerProperties<>(new SqsConsumerProperties());

        sqsStreamProvisioner.provisionConsumerDestination("topic-name",
                                                          "group-name",
                                                          consumerProperties);

        List<String> topicArns = listTopicsArns();
        assertThat(topicArns).hasSize(1);
        assertThat(sns.getTopicAttributes(topicArns.get(0)).getAttributes().get("TopicArn")).endsWith("topic-name");
        assertThat(sns.listSubscriptionsByTopic(topicArns.get(0)).getSubscriptions()).hasSize(1);
    }

    @Test
    public void createsSubscriptionWhenPartitioningDisabled() {
        ExtendedConsumerProperties<SqsConsumerProperties> consumerProperties = new ExtendedConsumerProperties<>(new SqsConsumerProperties());

        sqsStreamProvisioner.provisionConsumerDestination("topic-name",
                                                          "group-name",
                                                          consumerProperties);

        List<String> topicArns = listTopicsArns();
        assertThat(topicArns).hasSize(1);
        List<Subscription> subscriptions = sns.listSubscriptionsByTopic(topicArns.get(0)).getSubscriptions();
        assertThat(subscriptions).hasSize(1);
        assertThat(sns.getSubscriptionAttributes(subscriptions.get(0).getSubscriptionArn()).getAttributes()).doesNotContainKeys("FilterPolicy");
    }

    // partitioning enabled
    @Test
    public void createsSqsQueueWhenPartitioningEnabled() {
        ExtendedConsumerProperties<SqsConsumerProperties> consumerProperties = new ExtendedConsumerProperties<>(new SqsConsumerProperties());
        consumerProperties.setPartitioned(true);
        consumerProperties.setInstanceIndex(2);

        sqsStreamProvisioner.provisionConsumerDestination("topic-name",
                                                          "group-name",
                                                          consumerProperties);

        List<String> queueUrls = sqs.listQueues("group-name").getQueueUrls();
        assertThat(queueUrls).hasSize(1);
        assertThat(sqs.getQueueAttributes(queueUrls.get(0), Arrays.asList("QueueArn")).getAttributes().get("QueueArn")).endsWith("group-name-2");
    }

    @Test
    public void createsSubscriptionWhenPartitioningEnabled() {
        ExtendedConsumerProperties<SqsConsumerProperties> consumerProperties = new ExtendedConsumerProperties<>(new SqsConsumerProperties());
        consumerProperties.setPartitioned(true);
        consumerProperties.setInstanceIndex(2);

        sqsStreamProvisioner.provisionConsumerDestination("topic-name",
                                                          "group-name",
                                                          consumerProperties);

        List<String> topicArns = listTopicsArns();
        assertThat(topicArns).hasSize(1);
        List<Subscription> subscriptions = sns.listSubscriptionsByTopic(topicArns.get(0)).getSubscriptions();
        assertThat(subscriptions).hasSize(1);
        assertThat(sns.getSubscriptionAttributes(subscriptions.get(0)
                                                              .getSubscriptionArn())
                      .getAttributes()
                      .get("FilterPolicy")).isEqualTo("{\"scst_partition\": [2]}");
    }

    // producer provisioning
    @Test
    public void createsSnsTopicWhenProvisionsProducer() {
        ExtendedProducerProperties<SqsProducerProperties> producerProperties = new ExtendedProducerProperties<>(new SqsProducerProperties());

        sqsStreamProvisioner.provisionProducerDestination("topic-name", producerProperties);

        List<String> topicArns = listTopicsArns();
        assertThat(topicArns).hasSize(1);
        assertThat(sns.getTopicAttributes(topicArns.get(0)).getAttributes().get("TopicArn")).endsWith("topic-name");
    }

    @NotNull
    private List<String> listTopicsArns() {
        return sns.listTopics()
                  .getTopics()
                  .stream()
                  .map(Topic::getTopicArn)
                  .collect(Collectors.toList());
    }

}
