package org.springframework.cloud.stream.binder.sqs.samples.sourceandsink;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication(exclude = ContextStackAutoConfiguration.class)
@EnableBinding({Source.class, Sink.class})
@EnableScheduling
public class SourceAndSinkApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceAndSinkApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SourceAndSinkApplication.class, args);
    }

    @Autowired
    private Source source;

    @Scheduled(fixedRate = 10000L)
    void publishJsonMessageJob() {
        Person payload = new Person("Lena");
        LOGGER.info("Publishing a message with payload: {}", payload);
        source.output().send(MessageBuilder.withPayload(payload)
                                           .build());
    }

    @StreamListener(Sink.INPUT)
    void listen(Message<Person> message) {
        LOGGER.info("Received message with payload: {}", message.getPayload());
    }

    static class Person {
        private final String name;

        @JsonCreator
        public Person(@JsonProperty("name") String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Person{" +
                   "name='" + name + '\'' +
                   '}';
        }
    }
}
