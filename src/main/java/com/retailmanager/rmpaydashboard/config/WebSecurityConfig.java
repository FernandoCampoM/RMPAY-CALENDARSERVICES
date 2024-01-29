package com.retailmanager.rmpaydashboard.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;
import com.retailmanager.rmpaydashboard.security.JWTAthenticationFilter;
import com.retailmanager.rmpaydashboard.security.JWTAuthorizationFilter;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Configuration
//@AllArgsConstructor
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {



    private final JWTAuthorizationFilter jwtAuthorizationFilter;

    
   
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,AuthenticationManager authenticationManager) throws Exception{

        JWTAthenticationFilter jwtAthenticationFilter= new JWTAthenticationFilter();
        jwtAthenticationFilter.setAuthenticationManager(authenticationManager);
        jwtAthenticationFilter.setFilterProcessesUrl("/login");

        return http.csrf(csrf->csrf.disable())
                .authorizeHttpRequests(authRequest->authRequest
                    .requestMatchers("/login").permitAll().requestMatchers(HttpMethod.GET,"/api/services/**").permitAll()
                    .anyRequest()
                    .authenticated())
                .sessionManagement(sesion->sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                .addFilter(jwtAthenticationFilter)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
        
    }
    
}