package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.request.LoginRequest;
import com.icesi.uniplan.dto.request.RegistroEstudianteRequest;
import com.icesi.uniplan.dto.response.ApiResponse;
import com.icesi.uniplan.dto.response.TokenResponse;
import com.icesi.uniplan.dto.response.UsuarioResponse;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.security.JwtUtil;
import com.icesi.uniplan.service.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasena()));

            Usuario usuario = usuarioService.obtenerPorCorreo(authentication.getName());
            String token = jwtUtil.generateToken(usuario.getCorreo(), usuario.getTipo().name());

            TokenResponse tokenResponse = new TokenResponse(
                    token, usuario.getCorreo(), usuario.getNombre(), usuario.getTipo());

            return ResponseEntity.ok(ApiResponse.ok("Login exitoso", tokenResponse));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Correo o contraseña incorrectos"));
        }
    }

    @PostMapping("/registro/estudiante")
    public ResponseEntity<ApiResponse<UsuarioResponse>> registrarEstudiante(
            @Valid @RequestBody RegistroEstudianteRequest request) {
        Usuario usuario = usuarioService.registrarEstudiante(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Estudiante registrado exitosamente", toResponse(usuario)));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(usuario.getId());
        r.setNombre(usuario.getNombre());
        r.setCorreo(usuario.getCorreo());
        r.setTipo(usuario.getTipo());
        return r;
    }
}
