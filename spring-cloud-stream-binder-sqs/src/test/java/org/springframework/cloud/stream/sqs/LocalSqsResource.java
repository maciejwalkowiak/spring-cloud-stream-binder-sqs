package org.springframework.cloud.stream.sqs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.testcontainers.containers.GenericContainer;

/**
 * Creates local SQS with TestContainers and deletes all queues when finished.
 *
 * @author Maciej Walkowiak
 */
public class LocalSqsResource extends TestWatcher {

    private GenericContainer sqs = new GenericContainer<>("roribio16/alpine-sqs")
            .withExposedPorts(9324);

    private String endpoint;
    private AmazonSQSAsync sqsClient;

    @Override
    protected void starting(Description description) {
        this.sqs.start();
        this.endpoint = "http://" + sqs.getContainerIpAddress() + ":" + sqs.getMappedPort(9324);

        AmazonSQSAsyncClientBuilder builder = AmazonSQSAsyncClientBuilder.standard();
        builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint,
                                                                                    Regions.DEFAULT_REGION.getName()));
        builder.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("", "")));
        this.sqsClient = builder.build();
    }

    @Override
    protected void finished(Description description) {
        sqsClient.listQueues()
                 .getQueueUrls()
                 .forEach(queueUrl -> sqsClient.deleteQueue(queueUrl));
        sqs.stop();
    }

    public String getEndpoint() {
        return endpoint;
    }
}
