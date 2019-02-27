package org.springframework.cloud.stream.sqs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * The extended SQS-specific binding configuration properties.
 *
 * @author Maciej Walkowiak
 */
@ConfigurationProperties("spring.cloud.stream.sqs")
public class SqsExtendedBindingProperties extends AbstractExtendedBindingProperties<SqsConsumerProperties, SqsProducerProperties, SqsBindingProperties> {

    private static final String DEFAULTS_PREFIX = "spring.cloud.stream.sqs.default";

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return SqsBindingProperties.class;
    }
}
