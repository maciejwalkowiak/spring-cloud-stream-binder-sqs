package org.springframework.cloud.stream.sqs;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
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

    private AmazonSQSAsync sqsClient;

    @Override
    protected void starting(Description description) {
        this.localstack.start();
        AmazonSQSAsyncClientBuilder builder = AmazonSQSAsyncClientBuilder.standard();
        builder.setEndpointConfiguration(localstack.getEndpointConfiguration(Service.SQS));
        builder.setCredentials(localstack.getDefaultCredentialsProvider());
        this.sqsClient = builder.build();
    }

    @Override
    protected void finished(Description description) {
        sqsClient.listQueues()
                 .getQueueUrls()
                 .forEach(queueUrl -> sqsClient.deleteQueue(queueUrl));
        // TODO: delete SNS topics
        localstack.stop();
    }

    public AwsClientBuilder.EndpointConfiguration getEndpoint(Service service) {
        return localstack.getEndpointConfiguration(service);
    }

    public AWSCredentialsProvider getCredentialsProvider() {
        return localstack.getDefaultCredentialsProvider();
    }
}
