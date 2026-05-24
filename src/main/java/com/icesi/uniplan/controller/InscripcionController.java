package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.response.ApiResponse;
import com.icesi.uniplan.service.IEventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inscripciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ESTUDIANTE')")
public class InscripcionController {

    private final IEventoService eventoService;

    @PostMapping("/{eventoId}")
    public ResponseEntity<ApiResponse<Void>> inscribirse(
            @PathVariable String eventoId, Authentication authentication) {
        eventoService.inscribirEstudiante(eventoId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("Inscripción realizada exitosamente", null));
    }

    @DeleteMapping("/{eventoId}")
    public ResponseEntity<ApiResponse<Void>> cancelar(
            @PathVariable String eventoId, Authentication authentication) {
        eventoService.cancelarInscripcion(eventoId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("Inscripción cancelada exitosamente", null));
    }
}
