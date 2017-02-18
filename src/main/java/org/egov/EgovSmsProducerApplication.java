package org.egov;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
    public WebMvcConfigurerAdapter webMvcConfigurerAdapter() {
        return new WebMvcConfigurerAdapter() {

            @Override
            public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
                configurer.defaultContentType(MediaType.APPLICATION_JSON_UTF8);
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
