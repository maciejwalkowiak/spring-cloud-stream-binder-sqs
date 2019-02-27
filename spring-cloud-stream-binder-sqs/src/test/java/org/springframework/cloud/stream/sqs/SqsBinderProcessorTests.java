package org.springframework.cloud.stream.sqs;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    // TODO: add LocalResource for SQS

    @Autowired
    private TestSource testSource;

    @Autowired
    private PollableChannel fromProcessorChannel;

    @Autowired
    private SubscribableChannel errorChannel;

    @Autowired
    @Qualifier(Processor.INPUT + "." + CONSUMER_GROUP + ".errors")
    private SubscribableChannel consumerErrorChannel;

    public static CountDownLatch countDownLatch = new CountDownLatch(1);

    @Test
    @SuppressWarnings("unchecked")
    public void testProcessorWithSqsBinder() throws Exception {
        Message<String> testMessage = MessageBuilder.withPayload("hello " + new Date())
                                                    .setHeader("contentType", "text/plain")
                                                    .setHeader("foo", "BAR").build();
        this.testSource.toProcessorOutput().send(testMessage);

        countDownLatch.await();

//        Message<Dummy> receive = (Message<Dummy>) this.fromProcessorChannel
//                .receive(100_000);
//        assertThat(receive).isNotNull();
//
//
//        assertThat(receive.getPayload()).isEqualTo("FOO".getBytes());
//
//        assertThat(receive.getHeaders().get(MessageHeaders.CONTENT_TYPE))
//                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
//
//        assertThat(receive.getHeaders().get(AwsHeaders.RECEIVED_STREAM))
//                .isEqualTo(Processor.OUTPUT);
//        assertThat(receive.getHeaders().get("foo")).isEqualTo("BAR");
//
//        BlockingQueue<Message<?>> errorMessages = new LinkedBlockingQueue<>();
//
//        this.errorChannel.subscribe(errorMessages::add);
//        this.consumerErrorChannel.subscribe(errorMessages::add);
//
//        this.testSource.toProcessorOutput().send(new GenericMessage<>("junk"));
//
//        Message<?> errorMessage1 = errorMessages.poll(10, TimeUnit.SECONDS);
//        Message<?> errorMessage2 = errorMessages.poll(10, TimeUnit.SECONDS);
//        assertThat(errorMessage1).isNotNull();
//        assertThat(errorMessage2).isNotNull();
//        assertThat(errorMessage1).isSameAs(errorMessage2);
//        assertThat(errorMessages).isEmpty();

    }

    /**
     * Test configuration.
     */
    @EnableBinding({Processor.class, TestSource.class })
    @EnableAutoConfiguration(exclude = {MessagingAutoConfiguration.class,
                                        ContextStackAutoConfiguration.class,
                                        ContextResourceLoaderAutoConfiguration.class })
    static class ProcessorConfiguration {

        @Bean(destroyMethod = "")
        public AmazonSQSAsync amazonSQSAsync() {
            AmazonSQSAsyncClientBuilder builder = AmazonSQSAsyncClientBuilder.standard();
            builder.setEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:9324",
                                                                                        Regions.DEFAULT_REGION.getName()));
            return builder.build();
        }

        @StreamListener(Processor.INPUT)
        public void transform(Message<String> message) {
            String payload = message.getPayload();
            System.out.println(payload);

            countDownLatch.countDown();
        }

        @Bean(name = Processor.INPUT + "." + CONSUMER_GROUP + ".errors")
        public SubscribableChannel consumerErrorChannel() {
            return new PublishSubscribeChannel();
        }

        @Bean
        public MessageProducer kinesisMessageDriverChannelAdapter() {
            SqsMessageDrivenChannelAdapter kinesisMessageDrivenChannelAdapter = new SqsMessageDrivenChannelAdapter(
                    amazonSQSAsync(), Processor.OUTPUT);
            kinesisMessageDrivenChannelAdapter.setOutputChannel(fromProcessorChannel());

            return kinesisMessageDrivenChannelAdapter;
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
