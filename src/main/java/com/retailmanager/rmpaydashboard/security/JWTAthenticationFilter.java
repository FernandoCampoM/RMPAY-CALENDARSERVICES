package com.retailmanager.rmpaydashboard.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.UserDisabled;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;
import com.retailmanager.rmpaydashboard.repositories.UsersAppRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTAthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UserRepository usuarioRepository;
    private UsersAppRepository usersAppRepository;
    
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
            if(authCredentials.getEmployeeId()!=null){
                if(!usersAppRepository.existsById(authCredentials.getEmployeeId())){
                    throw new EntidadNoExisteException("El User con employeeId "+authCredentials.getEmployeeId()+" no existe en la Base de datos");
                }else{
                    usuarioRepository.updateTempAuthId(authCredentials.getUsername(), authCredentials.getEmployeeId());
                }
            }
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
        if(userDetails.getUserObject().isEnable()==false){
            throw new UserDisabled("");
        }
        usuarioRepository.updateLastLogin(userDetails.getUserObject().getUserID(),LocalDate.now());
        String token=TokenUtils.createTokenWithClaims(userDetails.getUserObject());
        usuarioRepository.updateTempAuthId(userDetails.getUserObject().getUsername(), null);
        
        Token fullToken=new Token();
        fullToken.setAuthorization("Bearer "+token);
        response.addHeader("Authorization", "Bearer "+token);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(fullToken.toJSON());
        response.getWriter().flush();

        super.successfulAuthentication(request, response, chain, authResult);
    }
    public void setUsuarioRepository(UserRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    public void setUsersAppRepository(UsersAppRepository usersAppRepository) {
        this.usersAppRepository = usersAppRepository;
    }
}
