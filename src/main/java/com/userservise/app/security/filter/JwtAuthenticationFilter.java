package com.userservise.app.security.filter;

import com.userservise.app.security.model.CustomUserDetails;
import com.userservise.app.model.constants.ErrorMessage;
import com.userservise.app.model.dto.AuthResponse;
import com.userservise.app.model.exception.InvalidTokenException;
import com.userservise.app.security.model.CustomUserDetails;
import com.userservise.app.service.Impl.AuthServiceClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String isValidToken = request.getHeader("X-Is-Valid");
        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-Role");

        if (isValidToken == null || isValidToken.equals("false")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (userId == null || role == null) {
            filterChain.doFilter(request, response);
            return;
        }

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        CustomUserDetails userDetails = new CustomUserDetails(Long.parseLong(userId), role);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}