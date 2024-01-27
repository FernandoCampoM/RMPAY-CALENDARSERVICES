package com.retailmanager.rmpaydashboard.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.retailmanager.rmpaydashboard.security.JWTAthenticationFilter;
import com.retailmanager.rmpaydashboard.security.JWTAuthorizationFilter;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {


    private final UserDetailsService userDetailsService;

    private final JWTAuthorizationFilter jwtAuthorizationFilter;

    
    /** 
     * @param http
     * @param authenticationManager
     * @return SecurityFilterChain
     * @throws Exception
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception{

        JWTAthenticationFilter jwtAthenticationFilter= new JWTAthenticationFilter();

        jwtAthenticationFilter.setAuthenticationManager(authenticationManager);
        jwtAthenticationFilter.setFilterProcessesUrl("/login");
        
        return http
                .cors().and().csrf().disable()
                .authorizeHttpRequests()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(jwtAthenticationFilter)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build(); 
    }
    /* @Bean 
    UserDetailsService userDetailsService(){
        InMemoryUserDetailsManager manager= new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("admin")
        .password(passwordEncoder()
        .encode("admin"))
        .roles()
        .build());
        return manager;
    } */
    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception{
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                    .userDetailsService(userDetailsService)
                    .passwordEncoder(passwordEncoder())
                    .and()
                    .build();
    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    /*  @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.debug(true)
        .ignoring().requestMatchers("/api/services");
    } */
 /* public static void main(String[] args) {
     System.out.println("pass:"+new BCryptPasswordEncoder().encode("developer@601"));
 }  */
    
}