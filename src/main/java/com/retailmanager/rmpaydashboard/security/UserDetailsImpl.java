package com.retailmanager.rmpaydashboard.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.retailmanager.rmpaydashboard.enums.Rol;
import com.retailmanager.rmpaydashboard.models.User;
import com.retailmanager.rmpaydashboard.models.rmpayAtTheTable.RMPayAtTheTable_User;

import lombok.AllArgsConstructor;

@AllArgsConstructor  
public class UserDetailsImpl implements UserDetails{
    private final User usuario;
    
    
    
    public UserDetailsImpl(RMPayAtTheTable_User usuario) {
        User user=new User();
        user.setUsername(usuario.getUsername());
        user.setPassword(usuario.getPassword());
        user.setEnable(true);
        user.setUserID(usuario.getUserId());
        user.setName(usuario.getBusinessName());
        user.setRol(Rol.ROLE_USERRMPAYATTHETABLE);
        this.usuario=user;
    }
    
    
    /** 
     * @return Collection<? extends GrantedAuthority>
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority>  authorities=new ArrayList<>();
        if(usuario.getRol()!=null){
            authorities.add(new SimpleGrantedAuthority(usuario.getRol().toString()));
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.isEnable();
    }
    public String getName(){
        return usuario.getName();
    }
    public User getUserObject(){
        return this.usuario;
    }
    
}
