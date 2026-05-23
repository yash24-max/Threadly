package dev.threadly.core.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds security-hardening HTTP response headers to every request.
 *
 * <p>Runs at highest precedence so headers are always present regardless of other filter results.
 */
@Component
@Order(Integer.MIN_VALUE)
public class SecurityHeadersFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-Frame-Options", "DENY");
    response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
    response.setHeader("Content-Security-Policy", "default-src 'self'");
    response.setHeader("X-XSS-Protection", "1; mode=block");
    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
    chain.doFilter(request, response);
  }
}
