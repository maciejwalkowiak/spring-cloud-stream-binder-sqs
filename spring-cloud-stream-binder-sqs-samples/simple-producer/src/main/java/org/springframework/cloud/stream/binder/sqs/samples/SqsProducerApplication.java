package org.springframework.cloud.stream.binder.sqs.samples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication(exclude = ContextStackAutoConfiguration.class)
@EnableBinding({Source.class, SqsProducerApplication.MySource.class})
@EnableScheduling
public class SqsProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqsProducerApplication.class, args);
    }

    @Autowired
    private Source source;

    @Autowired
    private MySource mySource;

    @Scheduled(fixedRate = 1000L)
    void publishPlainTextMessageJob() {
        String payload = "Current time is " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
        source.output().send(MessageBuilder.withPayload(payload)
                                           .build());
    }

    @Scheduled(fixedRate = 1000L)
    void publishJsonMessageJob() {
        mySource.output().send(MessageBuilder.withPayload(new Person("Lena"))
                                           .build());
    }

    // if there is a need for more outputs they can be defined in separate interface
    interface MySource {
        @Output("customOutput") // configuration for this output is in application.yml
        MessageChannel output();
    }

    static class Person {
        private final String name;

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
