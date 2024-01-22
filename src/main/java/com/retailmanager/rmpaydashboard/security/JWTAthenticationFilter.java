package com.retailmanager.rmpaydashboard.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTAthenticationFilter extends UsernamePasswordAuthenticationFilter {

    
    
    /** 
     * @param request
     * @param response
     * @return Authentication
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        AuthCredentials authCredentials = new AuthCredentials();
        try {
            authCredentials= new ObjectMapper().readValue(request.getReader(), AuthCredentials.class);
            
        } catch (IOException e) {
            
        }
        
        UsernamePasswordAuthenticationToken usernamePAT= new UsernamePasswordAuthenticationToken(
            authCredentials.getUsername(), authCredentials.getPassword(),Collections.emptyList());
        
            
        return getAuthenticationManager().authenticate(usernamePAT);
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        
        UserDetailsImpl userDetails=(UserDetailsImpl) authResult.getPrincipal();

        String token=TokenUtils.createToken(userDetails.getUserObject());
        
        Token fullToken=new Token();
        fullToken.setAuthorization("Bearer "+token);
        response.addHeader("Authorization", "Bearer "+token);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(fullToken.toJSON());
        response.getWriter().flush();

        super.successfulAuthentication(request, response, chain, authResult);
    }
}
