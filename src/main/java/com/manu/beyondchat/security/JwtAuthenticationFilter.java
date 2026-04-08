package com.manu.beyondchat.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtility jwtUtility;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Check if the token is completely missing or doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Pass to the next filter
            return; // Stop execution here
        }

        try{
            // 2. Extract the JWT (remove the "Bearer " prefix, which is 7 characters)
            final String jwt = authHeader.substring(7);
            // 3. Extract the username from the token
            final String username = jwtUtility.extractUsername(jwt);

            // 4. If we have a username and the user isn't already authenticated in this request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Fetch the user from your database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 5. Mathematically verify the token hasn't expired or been tampered with
                if (jwtUtility.isTokenValid(jwt, userDetails)) {

                    // 6. Create the Spring Security Authentication object
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // We don't need the password here because the JWT proved they are legit
                            userDetails.getAuthorities()
                    );

                    // Add some extra details about the web request (like IP address, etc.)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 7. Update the Security Context. This tells Spring: "This user is logged in!"
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // 8. Always hand off to the next filter in the chain
            filterChain.doFilter(request, response);
        }
        catch (Exception ex){
            // If any JWT exception occurs (e.g., ExpiredJwtException, SignatureException),
            // pass it to the resolver. This sends it to your GlobalExceptionHandler.
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }

    }
}
