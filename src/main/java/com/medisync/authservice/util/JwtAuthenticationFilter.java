package com.medisync.authservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisync.authservice.dto.ApiResponseDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//JwtAuthenticationFilter
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (isPublicEndpoint(request)) {
            chain.doFilter(request, response);
            return;
        }
        if (authenticateRequest(request, response)) {
            chain.doFilter(request, response);
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String responseCode, String responseMsg)
            throws IOException {
        ApiResponseDTO apiResponseDTO = ApiResponseDTO.builder()
                .responseCode(responseCode)
                .responseMsg(responseMsg)
                .content(null)
                .build();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403 status code
        response.setContentType("application/json");

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponseDTO));
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/v1/users/signup")
                || path.equals("/api/v1/users/login")
                || path.equals("/actuator/health")
                || path.equals("/actuator/info");
    }

    private boolean authenticateRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader("Authorization");
        logger.info(authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "03", "No header");
            return false;
        }

        String jwt = authHeader.substring(7);
        try {
            String email = jwtUtil.extractEmail(jwt);
            String role = jwtUtil.extractRole(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.isTokenValid(jwt, email, role)) {
                    String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    return true;
                } else {
                    sendUnauthorizedResponse(response, "03", "Not a valid token");
                    return false;
                }
            }
        } catch (Exception e) {
            sendUnauthorizedResponse(response, "03", "Exception in authorization");
            return false;
        }
        return true;
    }

}
