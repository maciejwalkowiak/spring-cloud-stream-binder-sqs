package org.springframework.cloud.stream.binder.sqs;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;

/**
 * Creates local SQS with TestContainers and deletes all queues when finished.
 *
 * @author Maciej Walkowiak
 */
public class LocalAwsResource extends TestWatcher {

    private LocalStackContainer localstack = new LocalStackContainer().withServices(Service.SQS,
                                                                                    Service.SNS);

    private AmazonSQS sqsClient;
    private AmazonSNS snsClient;


    @Override
    protected void starting(Description description) {
        this.localstack.start();
        this.sqsClient = createSqsClient();
        this.snsClient = createSnsClient();
    }

    @Override
    protected void finished(Description description) {
        sqsClient.listQueues()
                 .getQueueUrls()
                 .forEach(queueUrl -> sqsClient.deleteQueue(queueUrl));
        snsClient.listTopics()
                 .getTopics()
                 .forEach(topic -> snsClient.deleteTopic(topic.getTopicArn()));
        localstack.stop();
    }

    public AwsClientBuilder.EndpointConfiguration getEndpoint(Service service) {
        return localstack.getEndpointConfiguration(service);
    }

    public AWSCredentialsProvider getCredentialsProvider() {
        return localstack.getDefaultCredentialsProvider();
    }

    private AmazonSQS createSqsClient() {
        AmazonSQSAsyncClientBuilder builder = AmazonSQSAsyncClientBuilder.standard();
        builder.setEndpointConfiguration(localstack.getEndpointConfiguration(Service.SQS));
        builder.setCredentials(localstack.getDefaultCredentialsProvider());
        return builder.build();
    }

    private AmazonSNS createSnsClient() {
        AmazonSNSClientBuilder builder = AmazonSNSClientBuilder.standard();
        builder.setEndpointConfiguration(localstack.getEndpointConfiguration(Service.SNS));
        builder.setCredentials(localstack.getDefaultCredentialsProvider());
        return builder.build();
    }
}
