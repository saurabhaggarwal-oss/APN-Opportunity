package com.ttn.ck.apn.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class ApplicationConfig implements WebMvcConfigurer {


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] corsMethods = {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"};
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods(corsMethods)
                .allowedHeaders("*")
                .maxAge(3600);
    }

}
