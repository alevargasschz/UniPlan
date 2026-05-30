package com.icesi.uniplan.security;

import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.repository.mongo.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final IUsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        System.out.println("[UserDetailsService] Buscando usuario con correo: " + correo);

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> {
                    System.out.println("[UserDetailsService] Usuario NO encontrado: " + correo);
                    return new UsernameNotFoundException("Usuario no encontrado: " + correo);
                });

        System.out.println("[UserDetailsService] Usuario encontrado: " + usuario.getCorreo());
        System.out.println("[UserDetailsService] Tipo: " + usuario.getTipo());
        System.out.println("[UserDetailsService] Password hash: " + usuario.getContrasena());
        System.out.println("[UserDetailsService] DatosEspecificos: " + usuario.getDatosEspecificos());
        System.out.println("[UserDetailsService] Password hash: " + usuario.getContrasena());
        System.out.println("[UserDetailsService] Longitud hash: " + usuario.getContrasena().length());
        System.out.println("[UserDetailsService] BCrypt test: " + 
        new BCryptPasswordEncoder().matches("password", usuario.getContrasena()));

        return new MongoUserDetails(usuario);
    }
}