package com.ttn.ck.apn.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@AllArgsConstructor
public class ApplicationConfig implements WebMvcConfigurer {

    private ApplicationInterceptor applicationInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] corsMethods = {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"};
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods(corsMethods)
                .allowedHeaders("*")
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(applicationInterceptor);
        log.info("Added application interceptor for logging");
    }

}
