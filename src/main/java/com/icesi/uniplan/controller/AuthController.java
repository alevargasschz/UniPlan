package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.request.LoginRequest;
import com.icesi.uniplan.dto.request.RegistroEstudianteRequest;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.service.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/public/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest request, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasena()));
            // Session will be managed by Spring Security
            return "redirect:/eventos";
        } catch (BadCredentialsException e) {
            redirectAttributes.addFlashAttribute("error", "Correo o contraseña incorrectos");
            return "redirect:/public/auth/login";
        }
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
