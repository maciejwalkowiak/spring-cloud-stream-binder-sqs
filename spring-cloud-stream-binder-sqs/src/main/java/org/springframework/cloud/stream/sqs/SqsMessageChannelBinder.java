package org.springframework.cloud.stream.sqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.sqs.properties.SqsConsumerProperties;
import org.springframework.cloud.stream.sqs.properties.SqsExtendedBindingProperties;
import org.springframework.cloud.stream.sqs.properties.SqsProducerProperties;
import org.springframework.cloud.stream.sqs.provisioning.SqsStreamProvisioner;
import org.springframework.integration.aws.inbound.SqsMessageDrivenChannelAdapter;
import org.springframework.integration.aws.outbound.SqsMessageHandler;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.MessageBuilder;

/**
 * The Spring Cloud Stream Binder implementation for AWS SQS.
 *
 * @author Maciej Walkowiak
 */
public class SqsMessageChannelBinder extends
                                     AbstractMessageChannelBinder<ExtendedConsumerProperties<SqsConsumerProperties>,
                                             ExtendedProducerProperties<SqsProducerProperties>, SqsStreamProvisioner>
        implements ExtendedPropertiesBinder<MessageChannel, SqsConsumerProperties, SqsProducerProperties> {

    private final AmazonSQSAsync amazonSQSAsync;

    private SqsExtendedBindingProperties extendedBindingProperties;

    public SqsMessageChannelBinder(AmazonSQSAsync amazonSQSAsync, SqsStreamProvisioner provisioningProvider) {
        this(amazonSQSAsync, provisioningProvider, new SqsExtendedBindingProperties());
    }

    public SqsMessageChannelBinder(AmazonSQSAsync amazonSQSAsync, SqsStreamProvisioner provisioningProvider,
                                   SqsExtendedBindingProperties extendedBindingProperties) {
        super(new String[0], provisioningProvider);
        this.amazonSQSAsync = amazonSQSAsync;
        this.extendedBindingProperties = extendedBindingProperties;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination, ExtendedProducerProperties<SqsProducerProperties> producerProperties,
                                                          MessageChannel errorChannel) throws Exception {
        SqsMessageHandler sqsMessageHandler = new SqsMessageHandler(amazonSQSAsync);
        sqsMessageHandler.setQueue(destination.getName());
        sqsMessageHandler.setFailureChannel(errorChannel);
        sqsMessageHandler.setBeanFactory(getBeanFactory());
        sqsMessageHandler.setMessageConverter(new ByteArrayToStringMessageConverter());
        return sqsMessageHandler;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group, ExtendedConsumerProperties<SqsConsumerProperties> properties) throws Exception {
        SqsMessageDrivenChannelAdapter adapter = new SqsMessageDrivenChannelAdapter(this.amazonSQSAsync, destination.getName());
        adapter.setVisibilityTimeout(properties.getExtension().getVisibilityTimeout());
        adapter.setMaxNumberOfMessages(properties.getExtension().getMaxNumberOfMessages());
        adapter.setWaitTimeOut(properties.getExtension().getWaitTimeOut());
        if (properties.getExtension().getMessageDeletionPolicy() != null) {
            adapter.setMessageDeletionPolicy(properties.getExtension().getMessageDeletionPolicy());
        }
        adapter.setQueueStopTimeout(properties.getExtension().getQueueStopTimeout());
        return adapter;
    }

    @Override
    public SqsConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.extendedBindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public SqsProducerProperties getExtendedProducerProperties(String channelName) {
        return this.extendedBindingProperties.getExtendedProducerProperties(channelName);
    }

    @Override
    public String getDefaultsPrefix() {
        return this.extendedBindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.extendedBindingProperties.getExtendedPropertiesEntryClass();
    }

    public void setExtendedBindingProperties(SqsExtendedBindingProperties extendedBindingProperties) {
        this.extendedBindingProperties = extendedBindingProperties;
    }

    // TODO: not sure if that's the right way to deal with SQS messages.
    private static class ByteArrayToStringMessageConverter implements MessageConverter {
        @Override
        public Object fromMessage(Message<?> message, Class<?> targetClass) {
            return new String((byte[]) message.getPayload());
        }

        @Override
        public Message<?> toMessage(Object payload, MessageHeaders headers) {
            return MessageBuilder.createMessage(new String((byte[]) payload), headers);
        }
    }
}
