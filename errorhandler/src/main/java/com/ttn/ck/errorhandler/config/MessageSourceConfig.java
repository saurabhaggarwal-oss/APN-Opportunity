package com.ttn.ck.errorhandler.config;

import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;


@Slf4j
@Configuration
public class MessageSourceConfig {

    @Bean
    @Primary
    public Validator getValidator() {
        log.info("Initializing validator bean");
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource());
        log.info("Configured validation message source for validation messages");
        return validatorFactoryBean;
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(getValidator());
        return methodValidationPostProcessor;
    }

    @Bean
    public MessageSource messageSource() {
        log.info("Configuring message source");
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("message");
        messageSource.setDefaultEncoding("UTF-8");
        log.info("Configured custom messages source");
        return messageSource;
    }

}
