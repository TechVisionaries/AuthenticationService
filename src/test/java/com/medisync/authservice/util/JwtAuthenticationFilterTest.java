package com.medisync.authservice.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;
    @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() throws Exception {
        SecurityContextHolder.clearContext();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void doFilterInternal_publicEndpoint_shouldCallChainDoFilter() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/users/signup");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void isPublicEndpoint_withNonPublicPath_shouldReturnFalse() {
        when(request.getServletPath()).thenReturn("/api/v1/other");
        boolean result = jwtAuthenticationFilter.isPublicEndpoint(request);
        assertFalse(result);
    }

    @Test
    void doFilterInternal_tokenInvalid_shouldSendUnauthorized() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
        when(jwtUtil.extractEmail("invalidtoken")).thenReturn("user@example.com");
        when(jwtUtil.extractRole("invalidtoken")).thenReturn("USER");
        when(jwtUtil.isTokenValid("invalidtoken", "user@example.com", "USER")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_headerNotBearer_shouldSendUnauthorized() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/other");
        when(request.getHeader("Authorization")).thenReturn("Token sometoken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_emailNull_shouldReturnTrue() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.extractEmail("token")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_authenticationNotNull_shouldReturnTrue() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.extractEmail("token")).thenReturn("user@example.com");
        when(jwtUtil.extractRole("token")).thenReturn("USER");

        var authentication = mock(org.springframework.security.core.Authentication.class);
        var context = mock(org.springframework.security.core.context.SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        try (var mocked = org.mockito.Mockito.mockStatic(org.springframework.security.core.context.SecurityContextHolder.class)) {
            mocked.when(org.springframework.security.core.context.SecurityContextHolder::getContext).thenReturn(context);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
            verify(filterChain, times(1)).doFilter(request, response);
            verify(context, times(1)).getAuthentication();
        }
    }

    @Test
    void doFilterInternal_tokenValidNull_shouldSendUnauthorized() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
        when(jwtUtil.extractEmail("invalidtoken")).thenReturn("user@example.com");
        when(jwtUtil.extractRole("invalidtoken")).thenReturn("USER");
        when(jwtUtil.isTokenValid("invalidtoken", "user@example.com", "USER")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }



    @Test
    void doFilterInternal_validToken_shouldAuthenticateAndCallChain() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken");
        when(jwtUtil.extractEmail("validtoken")).thenReturn("user@example.com");
        when(jwtUtil.extractRole("validtoken")).thenReturn("USER");
        when(jwtUtil.isTokenValid("validtoken", "user@example.com", "USER")).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidToken_shouldSendUnauthorized() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
        when(jwtUtil.extractEmail("invalidtoken")).thenReturn("user@example.com");
        when(jwtUtil.extractRole("invalidtoken")).thenReturn("USER");
        when(jwtUtil.isTokenValid("invalidtoken", "user@example.com", "USER")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_missingAuthorizationHeader_shouldSendUnauthorized() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/other");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_exceptionDuringAuthorization_shouldSendUnauthorized() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.extractEmail("token")).thenThrow(new RuntimeException("Error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, times(1)).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }
}
