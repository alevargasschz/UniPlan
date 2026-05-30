package com.icesi.uniplan.security;

import com.icesi.uniplan.model.mongo.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class MongoUserDetails implements UserDetails {

    @Getter
    private final Usuario usuario;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
       return List.of(new SecurityAuthority("ROLE_" + usuario.getTipo().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getContrasena();
    }

    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
