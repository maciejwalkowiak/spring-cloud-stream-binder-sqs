package org.springframework.cloud.stream.sqs.properties;

import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;

import java.util.HashMap;
import java.util.Map;

import static com.amazonaws.services.sqs.model.QueueAttributeName.DelaySeconds;
import static com.amazonaws.services.sqs.model.QueueAttributeName.MaximumMessageSize;
import static com.amazonaws.services.sqs.model.QueueAttributeName.MessageRetentionPeriod;
import static com.amazonaws.services.sqs.model.QueueAttributeName.Policy;
import static com.amazonaws.services.sqs.model.QueueAttributeName.ReceiveMessageWaitTimeSeconds;
import static com.amazonaws.services.sqs.model.QueueAttributeName.VisibilityTimeout;

/**
 * The SQS-specific consumer binding configuration properties.
 *
 * @author Maciej Walkowiak
 */
public class SqsConsumerProperties {

    /**
     * {@link org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer#maxNumberOfMessages}
     */
    private Integer maxNumberOfMessages;

    /**
     * {@link org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer#visibilityTimeout}
     */
    private Integer visibilityTimeout;

    /**
     * {@link org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer#waitTimeOut}
     */
    private Integer waitTimeOut;

    /**
     * {@link org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer#queueStopTimeout}
     */
    private long queueStopTimeout;

    /**
     * {@link SqsMessageDeletionPolicy} on a messages coming from the SQS queue.
     */
    private SqsMessageDeletionPolicy messageDeletionPolicy;

    private QueueProperties queue = new QueueProperties();

    public Integer getMaxNumberOfMessages() {
        return maxNumberOfMessages;
    }

    public void setMaxNumberOfMessages(Integer maxNumberOfMessages) {
        this.maxNumberOfMessages = maxNumberOfMessages;
    }

    public Integer getVisibilityTimeout() {
        return visibilityTimeout;
    }

    public void setVisibilityTimeout(Integer visibilityTimeout) {
        this.visibilityTimeout = visibilityTimeout;
    }

    public Integer getWaitTimeOut() {
        return waitTimeOut;
    }

    public void setWaitTimeOut(Integer waitTimeOut) {
        this.waitTimeOut = waitTimeOut;
    }

    public long getQueueStopTimeout() {
        return queueStopTimeout;
    }

    public void setQueueStopTimeout(long queueStopTimeout) {
        this.queueStopTimeout = queueStopTimeout;
    }

    public SqsMessageDeletionPolicy getMessageDeletionPolicy() {
        return messageDeletionPolicy;
    }

    public void setMessageDeletionPolicy(SqsMessageDeletionPolicy messageDeletionPolicy) {
        this.messageDeletionPolicy = messageDeletionPolicy;
    }

    public QueueProperties getQueue() {
        return queue;
    }

    public void setQueue(QueueProperties queue) {
        this.queue = queue;
    }

    public static class QueueProperties {
        private Integer delaySeconds;
        private Integer maximumMessageSize;
        private Integer messageRetentionPeriod;
        private String policy;
        private Integer receiveMessageWaitTimeSeconds;
        private Integer visibilityTimeout;

        public Map<String, String> toQueueAttributes() {
            Map<String, String> attributes = new HashMap<>();

            if (this.getDelaySeconds() != null) {
                attributes.put(DelaySeconds.toString(),
                               String.valueOf(this.getDelaySeconds()));
            }

            if (this.getMessageRetentionPeriod() != null) {
                attributes.put(MessageRetentionPeriod.toString(),
                               String.valueOf(this.getMessageRetentionPeriod()));
            }

            if (this.getMaximumMessageSize() != null) {
                attributes.put(MaximumMessageSize.toString(),
                               String.valueOf(this.getMaximumMessageSize()));
            }

            if (this.getPolicy() != null) {
                attributes.put(Policy.toString(),
                               this.getPolicy());
            }

            if (this.getReceiveMessageWaitTimeSeconds() != null) {
                attributes.put(ReceiveMessageWaitTimeSeconds.toString(),
                               String.valueOf(this.getReceiveMessageWaitTimeSeconds()));
            }

            if (this.getVisibilityTimeout() != null) {
                attributes.put(VisibilityTimeout.toString(),
                               String.valueOf(this.getVisibilityTimeout()));
            }

            return attributes;
        }

        public Integer getDelaySeconds() {
            return delaySeconds;
        }

        public void setDelaySeconds(Integer delaySeconds) {
            this.delaySeconds = delaySeconds;
        }

        public Integer getMaximumMessageSize() {
            return maximumMessageSize;
        }

        public void setMaximumMessageSize(Integer maximumMessageSize) {
            this.maximumMessageSize = maximumMessageSize;
        }

        public Integer getMessageRetentionPeriod() {
            return messageRetentionPeriod;
        }

        public void setMessageRetentionPeriod(Integer messageRetentionPeriod) {
            this.messageRetentionPeriod = messageRetentionPeriod;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        public Integer getReceiveMessageWaitTimeSeconds() {
            return receiveMessageWaitTimeSeconds;
        }

        public void setReceiveMessageWaitTimeSeconds(Integer receiveMessageWaitTimeSeconds) {
            this.receiveMessageWaitTimeSeconds = receiveMessageWaitTimeSeconds;
        }

        public Integer getVisibilityTimeout() {
            return visibilityTimeout;
        }

        public void setVisibilityTimeout(Integer visibilityTimeout) {
            this.visibilityTimeout = visibilityTimeout;
        }
    }
}
