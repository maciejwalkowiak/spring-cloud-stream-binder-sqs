package org.springframework.cloud.stream.sqs.properties;

import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;

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
}
