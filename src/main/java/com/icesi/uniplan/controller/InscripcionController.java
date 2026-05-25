package com.icesi.uniplan.controller;

import com.icesi.uniplan.service.IEventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inscripciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ESTUDIANTE')")
public class InscripcionController {

    private final IEventoService eventoService;

    @PostMapping("/{eventoId}")
    public String inscribirse(
            @PathVariable String eventoId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            eventoService.inscribirEstudiante(eventoId, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Inscripción realizada exitosamente");
            return "redirect:/eventos/" + eventoId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/eventos/" + eventoId;
        }
    }

    @PostMapping("/{eventoId}/cancelar")
    public String cancelar(
            @PathVariable String eventoId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            eventoService.cancelarInscripcion(eventoId, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Inscripción cancelada exitosamente");
            return "redirect:/eventos/" + eventoId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/eventos/" + eventoId;
        }
    }
}
