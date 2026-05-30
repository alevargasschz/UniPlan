package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.request.RegistroOrganizadorRequest;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.service.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BIENESTAR')")
public class AdminController {

    private final IUsuarioService usuarioService;

    @GetMapping("/organizadores")
    public String listarOrganizadores(
            @RequestParam(required = false) TipoUsuario tipo,
            Model model) {

        List<Usuario> organizadores = usuarioService.listarOrganizadores(tipo);
        model.addAttribute("organizadores", organizadores);
        model.addAttribute("tipo", tipo);
        return "admin/listar-organizadores";
    }

    @GetMapping("/organizadores/crear")
    public String registrarOrganizadorForm(Model model) {
        model.addAttribute("request", new RegistroOrganizadorRequest());
        return "admin/crear-organizador";
    }

    @PostMapping("/organizadores/crear")
    public String registrarOrganizador(
             @ModelAttribute RegistroOrganizadorRequest request,
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
