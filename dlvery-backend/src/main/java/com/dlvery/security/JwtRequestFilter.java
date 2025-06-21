////package com.dlvery.security;
////
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
////import org.springframework.security.core.context.SecurityContextHolder;
////import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
////import org.springframework.stereotype.Component;
////import org.springframework.web.filter.OncePerRequestFilter;
////import jakarta.servlet.FilterChain;
////import jakarta.servlet.ServletException;
////import jakarta.servlet.http.HttpServletRequest;
////import jakarta.servlet.http.HttpServletResponse;
////import java.io.IOException;
////
////@Component
////public class JwtRequestFilter extends OncePerRequestFilter {
////    @Autowired
////    private JwtUtil jwtUtil;
////
////    @Override
////    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
////            throws ServletException, IOException {
////        String authHeader = request.getHeader("Authorization");
////        String username = null;
////        String jwt = null;
////
////        if (authHeader != null && authHeader.startsWith("Bearer ")) {
////            jwt = authHeader.substring(7);
////            username = jwtUtil.getUsernameFromToken(jwt);
////        }
////
////        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
////            if (jwtUtil.validateToken(jwt)) {
////                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
////                        username, null, null);
////                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
////                SecurityContextHolder.getContext().setAuthentication(auth);
////            }
////        }
////        chain.doFilter(request, response);
////    }
////}
//
//package com.dlvery.security;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//// ADDED: Import for SimpleGrantedAuthority and Collections
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import java.util.Collections;
//// ADDED: Import for Logger
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@Component
//public class JwtRequestFilter extends OncePerRequestFilter {
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    // ADDED: Logger for debugging
//    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//        String authHeader = request.getHeader("Authorization");
//        String username = null;
//        String jwt = null;
//
//        // ADDED: Log the request URI and Authorization header
//        logger.debug("Processing request for URI: {}", request.getRequestURI());
//        logger.debug("Authorization header: {}", authHeader);
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            jwt = authHeader.substring(7);
//            // ADDED: Log the extracted JWT
//            logger.debug("Extracted JWT: {}", jwt.substring(0, Math.min(jwt.length(), 10)) + "...");
//            try {
//                username = jwtUtil.getUsernameFromToken(jwt);
//                // ADDED: Log extracted username
//                logger.debug("Extracted username: {}", username);
//            } catch (Exception e) {
//                // ADDED: Log token parsing errors
//                logger.error("Failed to extract username from token: {}", e.getMessage());
//            }
//        }
//
//        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            if (jwtUtil.validateToken(jwt)) {
//                // ADDED: Extract role from token
//                String role = jwtUtil.getRoleFromToken(jwt);
//                logger.debug("Extracted role: {}", role);
//                // ADDED: Set authorities with the role
//                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
//                        username, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
//                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(auth);
//                // ADDED: Log successful authentication
//                logger.debug("Authentication set for user: {} with role: {}", username, role);
//            } else {
//                // ADDED: Log validation failure
//                logger.debug("Token validation failed for user: {}", username);
//            }
//        } else {
//            // ADDED: Log why authentication was not set
//            logger.debug("No authentication set: username={}, auth={}",
//                    username, SecurityContextHolder.getContext().getAuthentication());
//        }
//        chain.doFilter(request, response);
//    }
//}

package com.dlvery.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    // ADDED: Define public endpoints to skip
    private static final String[] PUBLIC_ENDPOINTS = {"/api/auth/", "/api/health"};

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        logger.debug("Processing request for URI: {}", request.getRequestURI());
        logger.debug("Authorization header: {}", authHeader);

        // ADDED: Skip filtering for public endpoints
        String requestURI = request.getRequestURI();
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (requestURI.startsWith(publicEndpoint)) {
                logger.debug("Skipping JWT filter for public endpoint: {}", requestURI);
                chain.doFilter(request, response);
                return;
            }
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            logger.debug("Extracted JWT: {}", jwt.substring(0, Math.min(jwt.length(), 10)) + "...");
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
                logger.debug("Extracted username: {}", username);
            } catch (Exception e) {
                logger.error("Failed to extract username from token: {}", e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt)) {
                String role = jwtUtil.getRoleFromToken(jwt);
                logger.debug("Extracted role: {}", role);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.debug("Authentication set for user: {} with role: {}", username, role);
            } else {
                logger.debug("Token validation failed for user: {}", username);
            }
        } else {
            logger.debug("No authentication set: username={}, auth={}",
                    username, SecurityContextHolder.getContext().getAuthentication());
        }
        chain.doFilter(request, response);
    }
}