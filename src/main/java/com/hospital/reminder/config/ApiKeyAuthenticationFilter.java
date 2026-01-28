package com.hospital.reminder.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final String apiKeyHeaderName;
    private final String apiKeySecret;

    public ApiKeyAuthenticationFilter(String apiKeyHeaderName, String apiKeySecret) {
        this.apiKeyHeaderName = apiKeyHeaderName;
        this.apiKeySecret = apiKeySecret;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(apiKeyHeaderName);
        
        if (apiKey != null) {
            if (apiKey.equals(apiKeySecret)) {
                log.debug("API Key authentication successful");
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        "api-user", 
                        null, 
                        List.of(new SimpleGrantedAuthority("ROLE_API_USER"))
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("Invalid API Key provided: {}", apiKey);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Invalid API Key\"}");
                return;
            }
        } else {
            // Se não tem API Key, continua sem autenticação (depende da configuração do endpoint)
            log.debug("No API Key provided in header: {}", apiKeyHeaderName);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.equals("/swagger-ui.html");
    }
}
