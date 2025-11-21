package com.userservise.app.security.filter;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                AuthResponse auth = authServiceClient.validate(token);

                if (auth != null && auth.isValid()) {
                    UsernamePasswordAuthenticationToken authentication = getUsernamePasswordAuthenticationToken(auth);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    throw new InvalidTokenException(ErrorMessage.INVALID_TOKEN.getMessage());
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(AuthResponse auth) {

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + auth.getRole()));

        CustomUserDetails userDetails = new CustomUserDetails(auth.getUserId(), auth.getRole());

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
        );
    }
}