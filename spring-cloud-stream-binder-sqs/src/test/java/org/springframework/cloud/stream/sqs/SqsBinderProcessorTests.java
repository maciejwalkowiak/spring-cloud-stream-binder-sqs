package org.springframework.cloud.stream.sqs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.aws.autoconfigure.context.ContextResourceLoaderAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.messaging.MessagingAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.aws.inbound.SqsMessageDrivenChannelAdapter;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.cloud.stream.bindings.input.group = "
        + SqsBinderProcessorTests.CONSUMER_GROUP,
        "spring.cloud.stream.bindings."
        + SqsBinderProcessorTests.TestSource.TO_PROCESSOR_OUTPUT
        + ".destination = " + Processor.INPUT,
        "spring.cloud.stream.sqs.bindings.input.consumer.idleBetweenPolls = 1",
        "spring.cloud.stream.sqs.bindings.input.producer.useNativeEncoding = true",
        "spring.cloud.stream.sqs.binder.headers = foo",
        "spring.cloud.stream.sqs.binder.checkpoint.table = checkpointTable",
        "spring.cloud.stream.sqs.binder.locks.table = lockTable"})
@DirtiesContext
public class SqsBinderProcessorTests {

    static final String CONSUMER_GROUP = "testGroup";

    @ClassRule
    public static LocalSqsResource localSqs = new LocalSqsResource();

    @Autowired
    private TestSource testSource;

    public static CountDownLatch countDownLatch = new CountDownLatch(1);

    @Test
    public void testProcessorWithSqsBinder() throws Exception {
        Message<Dummy> testMessage = MessageBuilder.withPayload(new Dummy("hello " + new Date()))
                                                    .setHeader("foo", "BAR").build();
        this.testSource.toProcessorOutput().send(testMessage);

        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    /**
     * Test configuration.
     */
    @EnableBinding({Processor.class, TestSource.class })
    @EnableAutoConfiguration(exclude = {MessagingAutoConfiguration.class,
                                        ContextStackAutoConfiguration.class,
                                        ContextResourceLoaderAutoConfiguration.class })
    static class ProcessorConfiguration {

        @Bean
        AWSStaticCredentialsProvider credentialsProvider() {
            return new AWSStaticCredentialsProvider(new BasicAWSCredentials("",""));
        }

        @Bean
        public AmazonSQSAsync amazonSQSAsync() {
            AmazonSQSAsyncClientBuilder builder = AmazonSQSAsyncClientBuilder.standard();
            builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(localSqs.getEndpoint(),
                                                                                        Regions.DEFAULT_REGION.getName()));
            return builder.build();
        }

        @StreamListener(Processor.INPUT)
        public void transform(Message<Dummy> message) {
            String payload = message.getPayload().getName();
            System.out.println(payload);

            countDownLatch.countDown();
        }

        @Bean(name = Processor.INPUT + "." + CONSUMER_GROUP + ".errors")
        public SubscribableChannel consumerErrorChannel() {
            return new PublishSubscribeChannel();
        }

        @Bean
        public MessageProducer sqsMessageDriverChannelAdapter() {
            SqsMessageDrivenChannelAdapter sqsMessageDrivenChannelAdapter = new SqsMessageDrivenChannelAdapter(
                    amazonSQSAsync(), Processor.OUTPUT);
            sqsMessageDrivenChannelAdapter.setOutputChannel(fromProcessorChannel());

            return sqsMessageDrivenChannelAdapter;
        }

        @Bean
        public PollableChannel fromProcessorChannel() {
            return new QueueChannel();
        }

    }

    /**
     * The SCSt contract for testing.
     */
    interface TestSource {

        String TO_PROCESSOR_OUTPUT = "toProcessorOutput";

        @Output(TO_PROCESSOR_OUTPUT)
        MessageChannel toProcessorOutput();

    }
}
