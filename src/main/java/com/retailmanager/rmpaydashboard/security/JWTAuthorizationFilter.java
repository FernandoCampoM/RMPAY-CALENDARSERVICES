package com.retailmanager.rmpaydashboard.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.retailmanager.rmpaydashboard.repositories.TerminalPayAtTableRepository;
import com.retailmanager.rmpaydashboard.repositories.UserPayAtTableRepository;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter{
    
    @Autowired
    private UserRepository usuarioRepository;

    @Autowired
    private UserPayAtTableRepository userPayAtTableRepository;

    @Autowired
    private TerminalPayAtTableRepository terminalPayAtTableRepository;

    /** 
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
    @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
                throws ServletException, IOException {

       String bearerToken=request.getHeader("Authorization");
       
       if(bearerToken!=null && bearerToken.startsWith("Bearer ")){
            String token =bearerToken.replace("Bearer ", "");
            UsernamePasswordAuthenticationToken usernamePAT=TokenUtils.getAuthentication(token,usuarioRepository,userPayAtTableRepository,terminalPayAtTableRepository);
            
            SecurityContextHolder.getContext().setAuthentication(usernamePAT);

       } 
        filterChain.doFilter(request, response);
    }
    
}
