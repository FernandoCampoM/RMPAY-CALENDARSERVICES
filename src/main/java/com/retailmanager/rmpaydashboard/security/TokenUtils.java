package com.retailmanager.rmpaydashboard.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.retailmanager.rmpaydashboard.models.User;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class TokenUtils {
    private final static String ACCESS_TOKEN_SECRET="gybPPZLk9ShzIv6V1Zl/xGL0MjAUOY+u327FmRrt7ZI=";
    private final static Long ACCESS_TOKEN_VALIDITY_SECONDS=2_592_000L;
    

    
    
    /** 
     * @param user
     * @return String
     */
    public static String createToken(User user){
        long expirationTime=ACCESS_TOKEN_VALIDITY_SECONDS *1_000;
        Date expirationDate=new Date(System.currentTimeMillis() + expirationTime);
        Map<String, Object> extra= new HashMap<>();
        extra.put("nombre", user.getName());
        extra.put("roles", user.getRol().toString());
        
        return Jwts.builder()
                    .setSubject(user.getUsername())
                    .setExpiration(expirationDate)
                    .addClaims(extra)
                    .signWith(Keys.hmacShaKeyFor(ACCESS_TOKEN_SECRET.getBytes()))
                    .compact();
    }

    public static UsernamePasswordAuthenticationToken getAuthentication(String token, UserRepository usuarioRepository){
        try {
            Claims claims = Jwts.parserBuilder()
                            .setSigningKey(ACCESS_TOKEN_SECRET.getBytes())
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
        String username=claims.getSubject();
        
        
        User usuario= usuarioRepository.findOneByUsername(username).orElseThrow(()-> new UsernameNotFoundException("El usuario con user "+username+" no existe"));
        UserDetailsImpl objUser=new UserDetailsImpl(usuario);
        return new UsernamePasswordAuthenticationToken(username,null,objUser.getAuthorities());
        } catch (JwtException  e) {
            return null;
        }catch (Exception  e) {
            return null;
        }
        
    }

}
