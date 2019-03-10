package org.springframework.cloud.stream.binder.sqs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import java.io.IOException;

/**
 * Unwraps message payload from SNS envelope.
 *
 * TODO: not sure if that's the right place to do this conversion.
 *
 * @author Maciej Walkowiak
 */
class SnsAwareMessageBuilderFactory extends DefaultMessageBuilderFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public <T> MessageBuilder<T> fromMessage(Message<T> message) {

        try {
            JsonNode jsonNode = objectMapper.readValue((String) message.getPayload(), JsonNode.class);
            return (MessageBuilder<T>) MessageBuilder.withPayload(unescapeJson(jsonNode.get("Message").toString()))
                                                     .copyHeaders(message.getHeaders());
        } catch (IOException e) {
            throw new MessagingException(message, e);
        }
    }

    private String unescapeJson(String jsonString) {
        return jsonString.replace("\"{", "{")
                         .replace("}\"", "}")
                         .replace("\\\"", "\"");
    }
}
