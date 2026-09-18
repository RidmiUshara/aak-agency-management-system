package lk.aak.agency.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/** Tags every request with a short correlation id for log tracing, exposed as X-Request-Id. */
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String MDC_KEY = "requestId";
    private static final String HEADER_NAME = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);

        try {
            MDC.put(MDC_KEY, requestId);
            response.setHeader(HEADER_NAME, requestId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
