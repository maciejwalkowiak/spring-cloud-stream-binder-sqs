package org.springframework.cloud.stream.binder.sqs;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsync;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.binder.sqs.provisioning.SqsStreamProvisioner;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.binder.sqs.properties.SqsConsumerProperties;
import org.springframework.cloud.stream.binder.sqs.properties.SqsExtendedBindingProperties;
import org.springframework.cloud.stream.binder.sqs.properties.SqsProducerProperties;
import org.springframework.cloud.stream.binder.sqs.provisioning.SqsProducerDestination;
import org.springframework.integration.aws.inbound.SqsMessageDrivenChannelAdapter;
import org.springframework.integration.aws.outbound.SnsMessageHandler;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

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
    private final AmazonSNSAsync amazonSNSAsync;

    private SqsExtendedBindingProperties extendedBindingProperties;

    public SqsMessageChannelBinder(AmazonSQSAsync amazonSQSAsync, AmazonSNSAsync amazonSNSAsync, SqsStreamProvisioner provisioningProvider) {
        this(amazonSQSAsync, amazonSNSAsync, provisioningProvider, new SqsExtendedBindingProperties());
    }

    public SqsMessageChannelBinder(AmazonSQSAsync amazonSQSAsync, AmazonSNSAsync amazonSNSAsync, SqsStreamProvisioner provisioningProvider,
                                   SqsExtendedBindingProperties extendedBindingProperties) {
        super(new String[0], provisioningProvider);
        this.amazonSQSAsync = amazonSQSAsync;
        this.amazonSNSAsync = amazonSNSAsync;
        this.extendedBindingProperties = extendedBindingProperties;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination, ExtendedProducerProperties<SqsProducerProperties> producerProperties,
                                                          MessageChannel errorChannel) throws Exception {
        SqsProducerDestination sqsProducerDestination = (SqsProducerDestination) destination;
        SnsMessageHandler snsMessageHandler = new SnsMessageHandler(amazonSNSAsync);
        snsMessageHandler.setTopicArn(sqsProducerDestination.getTopicArn());
        snsMessageHandler.setFailureChannel(errorChannel);
        snsMessageHandler.setBeanFactory(getBeanFactory());
        return snsMessageHandler;
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
        adapter.setMessageBuilderFactory(new SnsAwareMessageBuilderFactory());
        return adapter;
    }

    @Override
    protected void postProcessOutputChannel(MessageChannel outputChannel, ExtendedProducerProperties<SqsProducerProperties> producerProperties) {
        ((AbstractMessageChannel) outputChannel).addInterceptor(new SnsPayloadConvertingChannelInterceptor());
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
}
