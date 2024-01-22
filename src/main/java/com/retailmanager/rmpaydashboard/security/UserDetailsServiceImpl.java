package com.retailmanager.rmpaydashboard.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpaydashboard.models.User;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
    @Autowired
    private UserRepository usuarioRepository;

    
    /** 
     * @param username
     * @return UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User usuario= usuarioRepository.findOneByUsername(username)
                    .orElseThrow(()-> new UsernameNotFoundException("El usuario con user "+username+" no existe"));
        System.out.println("ENCONTRO EL USUARIO "+usuario.getName()+ " pass: "+usuario.getPassword()+" username: "+usuario.getEmail());
        return new UserDetailsImpl(usuario);    
    }
}
