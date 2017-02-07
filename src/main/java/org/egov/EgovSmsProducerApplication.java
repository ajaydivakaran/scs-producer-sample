package org.egov;

import lombok.Data;
import org.egov.model.User;
import org.egov.resolver.AuthUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
@RestController
public class EgovSmsProducerApplication {

    private SmsService smsService;

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

    @Autowired
    public EgovSmsProducerApplication(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/sms")
    public void submitSms(@RequestBody SmsRequest smsRequest) {
        smsService.handle(smsRequest);
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


@Service
@EnableBinding(Source.class)
class SmsService {

    private MessageChannel output;

    @Autowired
    SmsService(MessageChannel output) {
        this.output = output;
    }

    public void handle(SmsRequest smsRequest) {
        output.send(MessageBuilder.withPayload(smsRequest).build());
    }
}
