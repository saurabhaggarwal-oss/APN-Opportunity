package com.ttn.ck.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SnowplugAppConfig implements WebMvcConfigurer {

    @Autowired
    private SnowplugInterceptor snowplugInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(snowplugInterceptor).order(Ordered.LOWEST_PRECEDENCE);
    }

}
