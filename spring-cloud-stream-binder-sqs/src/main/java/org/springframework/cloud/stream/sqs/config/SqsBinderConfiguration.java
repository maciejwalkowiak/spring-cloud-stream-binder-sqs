/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.sqs.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.aws.core.region.RegionProvider;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.sqs.SqsMessageChannelBinder;
import org.springframework.cloud.stream.sqs.properties.SqsBinderConfigurationProperties;
import org.springframework.cloud.stream.sqs.properties.SqsExtendedBindingProperties;
import org.springframework.cloud.stream.sqs.provisioning.SqsStreamProvisioner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.integration.config.IntegrationConverter;

/**
 * The auto-configuration for AWS components and Spring Cloud Stream SQS Binder.
 *
 * @author Maciej Walkowiak
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@EnableConfigurationProperties({SqsBinderConfigurationProperties.class,
                                SqsExtendedBindingProperties.class})
public class SqsBinderConfiguration {

    @Bean
    @ConditionalOnMissingBean(AmazonSNSAsync.class)
    public AmazonSNSAsync amazonSNS(AWSCredentialsProvider awsCredentialsProvider,
                                    RegionProvider regionProvider) {
        AmazonSNSAsyncClientBuilder builder = AmazonSNSAsyncClientBuilder.standard();
        builder.setCredentials(awsCredentialsProvider);
        builder.setRegion(regionProvider.getRegion().getName());
        return builder.build();
    }

    @Bean
    public SqsStreamProvisioner provisioningProvider(AmazonSQSAsync amazonSQSAsync,
                                                     AmazonSNSAsync amazonSNSAsync) {
        return new SqsStreamProvisioner(amazonSQSAsync, amazonSNSAsync);
    }

    @Bean
    public SqsMessageChannelBinder sqsMessageChannelBinder(AmazonSQSAsync amazonSQSAsync,
                                                           AmazonSNSAsync amazonSNSAsync,
                                                           SqsStreamProvisioner provisioningProvider,
                                                           SqsExtendedBindingProperties sqsExtendedBindingProperties) {
        return new SqsMessageChannelBinder(amazonSQSAsync,
                                           amazonSNSAsync,
                                           provisioningProvider,
                                           sqsExtendedBindingProperties);
    }

    @Bean
    @IntegrationConverter
    @Order(Ordered.HIGHEST_PRECEDENCE)
    ByteArrayToStringConverter byteArrayToStringConverter() {
        return new ByteArrayToStringConverter();
    }

    @Bean
    @IntegrationConverter
    @Order(Ordered.HIGHEST_PRECEDENCE)
    StringToByteArrayConverter stringToByteArrayConverter() {
        return new StringToByteArrayConverter();
    }
}

