package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.request.CrearEventoRequest;
import com.icesi.uniplan.dto.response.*;
import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.model.mongo.embedded.Inscripcion;
import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import com.icesi.uniplan.service.IEventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final IEventoService eventoService;

    // -------------------------------------------------------------------------
    // Catálogo de eventos (cualquier usuario autenticado)
    // -------------------------------------------------------------------------

    @GetMapping
    public String listarEventos(
            @RequestParam(required = false) TipoEvento tipo,
            @RequestParam(required = false) EstadoEvento estado,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date inicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date fin,
            Model model) {

        List<EventoResumenResponse> eventos = eventoService.listarEventos(tipo, estado, inicio, fin)
                .stream().map(this::toResumen).collect(Collectors.toList());
        model.addAttribute("eventos", eventos);
        model.addAttribute("tipo", tipo);
        model.addAttribute("estado", estado);
        return "eventos/lista";
    }

    @GetMapping("/{id}")
    public String obtenerEvento(@PathVariable String id, Model model) {
        model.addAttribute("evento", toDetalle(eventoService.obtenerEvento(id)));
        return "eventos/detalle";
    }

    // -------------------------------------------------------------------------
    // Gestión de eventos (solo organizadores)
    // -------------------------------------------------------------------------

    @GetMapping("/crear")
public String crearEventoForm(Model model) {
    if (!model.containsAttribute("request")) {
        model.addAttribute("request", new CrearEventoRequest());
    }
    model.addAttribute("tiposEvento", TipoEvento.values());
    return "eventos/crear";
}

    @PostMapping("/crear")
    public String crearEvento(
            @Valid @ModelAttribute("request") CrearEventoRequest request,
            BindingResult bindingResult,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error -> {
                System.out.println("Campo: " + error.getField());
                System.out.println("Valor: " + error.getRejectedValue());
                System.out.println("Error: " + error.getDefaultMessage());
            });
            model.addAttribute("tiposEvento", TipoEvento.values());
            model.addAttribute("error", "Por favor corrige los errores del formulario.");
            System.out.println("[EventoController] Errores de validación");
            return "eventos/crear";
        }

        try {
            System.out.println("[EventoController] Se paso la validacion");
            Evento evento = eventoService.crearEvento(request, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Evento creado exitosamente");
            return "redirect:/eventos/" + evento.getId();
        } catch (Exception e) {
            System.out.println("[EventoController] Error al crear evento: " + e.getMessage());
            model.addAttribute("tiposEvento", TipoEvento.values());
            model.addAttribute("error", e.getMessage());
            return "eventos/crear"; // forward, no redirect → conserva datos
        }
    }

    @GetMapping("/mis-eventos")
    public String misEventos(Authentication authentication, Model model) {
        List<EventoResumenResponse> eventos = eventoService.listarEventosPorOrganizador(authentication.getName())
                .stream().map(this::toResumen).collect(Collectors.toList());
        model.addAttribute("eventos", eventos);
        return "eventos/mis-eventos";
    }

    @GetMapping("/{id}/inscritos")
    public String listarInscritos(
            @PathVariable String id, Authentication authentication, Model model) {

        List<InscritoResponse> inscritos = eventoService.listarInscritos(id, authentication.getName())
                .stream().map(this::toInscrito).collect(Collectors.toList());
        model.addAttribute("inscritos", inscritos);
        model.addAttribute("eventoId", id);
        return "eventos/inscritos";
    }

    @GetMapping("/{id}/inscritos/export")
    public ResponseEntity<String> exportarInscritos(@PathVariable String id, Authentication authentication) {
        String csv = eventoService.exportarInscritosCSV(id, authentication.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inscritos_" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(csv);
    }

    // -------------------------------------------------------------------------
    // Mapeos de respuesta
    // -------------------------------------------------------------------------

    private EventoResumenResponse toResumen(Evento e) {
        EventoResumenResponse r = new EventoResumenResponse();
        r.setId(e.getId());
        r.setTitulo(e.getTitulo());
        r.setTipo(e.getTipo());
        r.setFechaHoraInicio(e.getFechaHoraInicio());
        r.setFechaHoraFin(e.getFechaHoraFin());
        r.setUbicacion(e.getUbicacion());
        r.setCuposDisponibles(e.getCuposDisponibles());
        r.setMaxAsistentes(e.getMaxAsistentes());
        r.setEstado(e.getEstado());
        r.setOrganizadorNombre(e.getOrganizador() != null ? e.getOrganizador().getNombre() : null);
        return r;
    }

    private EventoResponse toDetalle(Evento e) {
        EventoResponse r = new EventoResponse();
        r.setId(e.getId());
        r.setTitulo(e.getTitulo());
        r.setDescripcion(e.getDescripcion());
        r.setTipo(e.getTipo());
        r.setFechaHoraInicio(e.getFechaHoraInicio());
        r.setFechaHoraFin(e.getFechaHoraFin());
        r.setUbicacion(e.getUbicacion());
        r.setMaxAsistentes(e.getMaxAsistentes());
        r.setTotalInscritos(e.getTotalInscritos());
        r.setCuposDisponibles(e.getCuposDisponibles());
        r.setEstado(e.getEstado());
        r.setDatosEspecificos(e.getDatosEspecificos());
        if (e.getOrganizador() != null) {
            r.setOrganizadorNombre(e.getOrganizador().getNombre());
            r.setOrganizadorCorreo(e.getOrganizador().getCorreo());
        }
        return r;
    }

    private InscritoResponse toInscrito(Inscripcion i) {
        InscritoResponse r = new InscritoResponse();
        r.setNombre(i.getNombre());
        r.setCodigoEstudiante(i.getCodigoEstudiante());
        r.setCorreo(i.getCorreo());
        r.setFechaInscripcion(i.getFechaInscripcion());
        r.setConfirmada(i.getConfirmada());
        return r;
    }
}
