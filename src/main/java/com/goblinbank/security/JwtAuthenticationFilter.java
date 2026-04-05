package com.goblinbank.security;

import com.goblinbank.GoblinConstants;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    String token = header.substring(7);
    try {
      Claims claims = jwtService.parse(token);
      String role = claims.get("role", String.class);
      if (GoblinConstants.ROLE_BANKER.equals(role)) {
        String sub = claims.getSubject();
        String user = sub != null && sub.startsWith("banker:") ? sub.substring(7) : sub;
        var auth =
            new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + GoblinConstants.ROLE_BANKER)));
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
      } else if (GoblinConstants.ROLE_HOUSE_PORTAL.equals(role)) {
        Number hid = claims.get("houseId", Number.class);
        if (hid == null) {
          filterChain.doFilter(request, response);
          return;
        }
        long houseId = hid.longValue();
        var auth =
            new UsernamePasswordAuthenticationToken(
                Long.valueOf(houseId),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + GoblinConstants.ROLE_HOUSE_PORTAL)));
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    } catch (Exception ignored) {
      // invalid token => anonymous
    }
    filterChain.doFilter(request, response);
  }
}
