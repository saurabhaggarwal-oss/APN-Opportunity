package com.ttn.ck.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Order
@Component
@AllArgsConstructor
public class SnowplugInterceptor implements HandlerInterceptor {

    private final SnowplugAuthService snowplugAuthService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        snowplugAuthService.authenticate(request);
        return true;
    }

}
