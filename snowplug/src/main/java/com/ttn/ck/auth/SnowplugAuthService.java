package com.ttn.ck.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SnowplugAuthService {

    public void authenticate(HttpServletRequest request) {
        if (request.getMethod().equals(HttpMethod.OPTIONS.name()) ||
                request.getServletPath().startsWith("/swagger-ui") ||
                request.getServletPath().startsWith("/api-docs")) {
            return;
        }
        if("/pool/export".equalsIgnoreCase(request.getServletPath())) {
            return;
        }
    }

}
