package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.request.RegistroOrganizadorRequest;
import com.icesi.uniplan.dto.response.UsuarioResponse;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.service.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BIENESTAR')")
public class AdminController {

    private final IUsuarioService usuarioService;

    @GetMapping("/organizadores/crear")
    public String registrarOrganizadorForm(Model model) {
        model.addAttribute("request", new RegistroOrganizadorRequest());
        return "admin/crear-organizador";
    }

    @PostMapping("/organizadores")
    public String registrarOrganizador(
            @Valid @ModelAttribute RegistroOrganizadorRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.registrarOrganizador(request);
            redirectAttributes.addFlashAttribute("success", "Organizador registrado exitosamente");
            return "redirect:/admin/organizadores";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/organizadores/crear";
        }
    }
}
