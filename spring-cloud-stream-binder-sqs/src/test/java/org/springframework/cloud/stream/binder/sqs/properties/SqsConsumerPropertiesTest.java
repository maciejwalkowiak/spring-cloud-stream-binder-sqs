package org.springframework.cloud.stream.binder.sqs.properties;

import com.amazonaws.services.sqs.model.QueueAttributeName;

import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SqsConsumerPropertiesTest {

    @Test
    public void createsQueueAttributeForSuppliedKey() {
        SqsConsumerProperties sqsConsumerProperties = new SqsConsumerProperties();
        SqsConsumerProperties.QueueProperties queue = sqsConsumerProperties.getQueue();
        queue.setDelaySeconds(10);
        queue.setMaximumMessageSize(1000);

        Map<String, String> queueAttributes = queue.toQueueAttributes();

        assertThat(queueAttributes).containsOnlyKeys(QueueAttributeName.DelaySeconds.toString(),
                                                     QueueAttributeName.MaximumMessageSize.toString())
                                   .containsEntry(QueueAttributeName.DelaySeconds.toString(), "10")
                                   .containsEntry(QueueAttributeName.MaximumMessageSize.toString(), "1000");
    }

}
