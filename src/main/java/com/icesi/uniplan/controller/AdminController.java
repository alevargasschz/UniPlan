package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.request.RegistroOrganizadorRequest;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.embedded.LiderEstudiantil;
import com.icesi.uniplan.model.mongo.embedded.PersonalBienestar;
import com.icesi.uniplan.model.mongo.embedded.Profesor;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.service.IUsuarioService;
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
        model.addAttribute("modoEdicion", false);
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

    @GetMapping("/organizadores/{id}/editar")
    public String editarOrganizadorForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Usuario organizador = usuarioService.obtenerOrganizadorPorId(id);
            model.addAttribute("request", toRequest(organizador));
            model.addAttribute("modoEdicion", true);
            model.addAttribute("organizadorId", id);
            return "admin/crear-organizador";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/organizadores";
        }
    }

    @PostMapping("/organizadores/{id}/editar")
    public String editarOrganizador(
            @PathVariable String id,
            @ModelAttribute RegistroOrganizadorRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.actualizarOrganizador(id, request);
            redirectAttributes.addFlashAttribute("success", "Organizador actualizado exitosamente");
            return "redirect:/admin/organizadores";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/organizadores/" + id + "/editar";
        }
    }

    @PostMapping("/organizadores/{id}/eliminar")
    public String eliminarOrganizador(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarOrganizador(id);
            redirectAttributes.addFlashAttribute("success", "Organizador eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/organizadores";
    }

    private RegistroOrganizadorRequest toRequest(Usuario organizador) {
        RegistroOrganizadorRequest request = new RegistroOrganizadorRequest();
        request.setNombre(organizador.getNombre());
        request.setCorreo(organizador.getCorreo());
        request.setContrasena("");
        request.setTipo(organizador.getTipo());

        if (organizador.getDatosEspecificos() instanceof Profesor profesor) {
            request.setFacultad(profesor.getFacultad());
            request.setDepartamento(profesor.getDepartamento());
            request.setEspecializacion(profesor.getEspecializacion());
        } else if (organizador.getDatosEspecificos() instanceof LiderEstudiantil lider) {
            request.setPrograma(lider.getPrograma());
            request.setSemestre(lider.getSemestre());
            request.setRepresentacion(lider.getRepresentacion());
        } else if (organizador.getDatosEspecificos() instanceof PersonalBienestar bienestar) {
            request.setAreaAdministrativa(bienestar.getAreaAdministrativa());
            request.setCargo(bienestar.getCargo());
        }

        return request;
    }
}
