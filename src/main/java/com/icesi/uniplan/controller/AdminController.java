package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.request.RegistroOrganizadorRequest;
import com.icesi.uniplan.dto.response.ApiResponse;
import com.icesi.uniplan.dto.response.UsuarioResponse;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.service.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BIENESTAR')")
public class AdminController {

    private final IUsuarioService usuarioService;

    @PostMapping("/organizadores")
    public ResponseEntity<ApiResponse<UsuarioResponse>> registrarOrganizador(
            @Valid @RequestBody RegistroOrganizadorRequest request) {
        Usuario usuario = usuarioService.registrarOrganizador(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Organizador registrado exitosamente", toResponse(usuario)));
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
