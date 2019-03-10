package org.springframework.cloud.stream.binder.sqs;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author Maciej Walkowiak
 */
class SnsPayloadConvertingChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        return MessageBuilder.createMessage(new String((byte[]) message.getPayload()), message.getHeaders());
    }
}
