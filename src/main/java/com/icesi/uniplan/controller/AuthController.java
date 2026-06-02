package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.request.RegistroEstudianteRequest;
import com.icesi.uniplan.service.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/public/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUsuarioService usuarioService;

    @GetMapping("/login")
    public String loginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error",
                    "No pudimos iniciar sesión. Verifica tu correo y contraseña, y vuelve a intentarlo.");
        }
        if (logout != null) {
            model.addAttribute("info", "Tu sesión se cerró correctamente.");
        }
        return "auth/login";
    }

    @GetMapping("/registro/estudiante")
    public String registroForm(Model model) {
        model.addAttribute("request", new RegistroEstudianteRequest());
        return "auth/registro-estudiante";
    }

    @PostMapping("/registro/estudiante")
    public String registrarEstudiante(
            @Valid @ModelAttribute RegistroEstudianteRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.registrarEstudiante(request);
            redirectAttributes.addFlashAttribute("success", "Estudiante registrado exitosamente");
            return "redirect:/public/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/public/auth/registro/estudiante";
        }
    }
}
