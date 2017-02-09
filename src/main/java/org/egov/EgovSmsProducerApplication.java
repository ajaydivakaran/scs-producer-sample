package org.egov;

import lombok.Data;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.egov.resolver.AuthUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
@EnableKafka
public class EgovSmsProducerApplication {

    @Autowired
    private KafkaTemplate<String, SmsRequest> kafkaTemplate;

    public void send(SmsRequest smsRequest) {
        ListenableFuture<SendResult<String, SmsRequest>> future = kafkaTemplate.send("sms3", smsRequest);
        future.addCallback(
                new ListenableFutureCallback<SendResult<String, SmsRequest>>() {
                    @Override
                    public void onSuccess(SendResult<String, SmsRequest> stringTSendResult) {
                        System.out.println("Success");
                        System.out.println(stringTSendResult);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        System.out.println("Error");
                        System.out.println(throwable.getMessage());
                    }
                }
        );
    }

    @Bean
    public ProducerFactory<String, SmsRequest> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public KafkaTemplate<String, SmsRequest> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public WebMvcConfigurerAdapter webMvcConfigurerAdapter(AuthUserResolver authUserResolver) {
        return new WebMvcConfigurerAdapter() {

            @Override
            public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
                configurer.defaultContentType(MediaType.APPLICATION_JSON_UTF8);
            }

            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
                argumentResolvers.add(authUserResolver);
            }

        };
    }

    @PostMapping("/sms")
    public void submitSms(@RequestBody SmsRequest smsRequest) {
        send(smsRequest);
    }


    public static void main(String[] args) {
        SpringApplication.run(EgovSmsProducerApplication.class, args);
    }
}

@Data
class SmsRequest {
    private String message;
    private String mobileNumber;
}
