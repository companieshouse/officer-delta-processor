package uk.gov.companieshouse.officer.delta.processor.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;

// NOTE: This code is temporary and will not be deployed to STAGING or LIVE. It is only to investigate a deployment
//       issue in the CiDev environment

@Component
public class HealthCheckLoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (httpRequest.getRequestURI().endsWith("/healthcheck")) {
            logger.info("Health check endpoint was called from IP: {}", request.getRemoteAddr());
        }

        chain.doFilter(request, response);
    }
}