package org.raghul.auth_engine.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

   @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        /**
         * load necessary data like Roles and permissions(which will be converted to grantedAuthority) into the security context
         * so that @PreAuthorize can use it
         * **/
        if (jwtService.isTokenValid(token)) {

            String userEmail = jwtService.extractUserEmail(token);
            Integer tenantId = jwtService
                    .extractUserDetailsFromToken(token)
                    .get("tenantId", Integer.class);
            /*
            List<String> roles = jwtService.extractRoles(token);
            List<GrantedAuthority> authorities = roles.stream()
                    .map(role ->(GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
                    */
            List<GrantedAuthority> authorities = jwtService.extractAuthorities(token).stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                    .toList();
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userEmail, null, authorities);
            authToken.setDetails(tenantId);


            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        filterChain.doFilter(request, response);
    }
}
