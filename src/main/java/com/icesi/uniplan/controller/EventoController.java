package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.request.CrearEventoRequest;
import com.icesi.uniplan.dto.request.ConferencistaRequest;
import com.icesi.uniplan.dto.request.DatosEspecificosRequest;
import com.icesi.uniplan.dto.response.*;
import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.model.mongo.embedded.ActividadVoluntariado;
import com.icesi.uniplan.model.mongo.embedded.Charla;
import com.icesi.uniplan.model.mongo.embedded.DatosEspecificos;
import com.icesi.uniplan.model.mongo.embedded.Inscripcion;
import com.icesi.uniplan.model.mongo.embedded.OtroEvento;
import com.icesi.uniplan.model.mongo.embedded.Taller;
import com.icesi.uniplan.model.mongo.embedded.TorneoDeportivo;
import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import com.icesi.uniplan.service.IEventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        Date inicioDate = null;
        Date finDate = null;

        if (inicio != null) {
            LocalDateTime inicioDia = inicio.atStartOfDay();
            inicioDate = Date.from(inicioDia.atZone(ZoneId.systemDefault()).toInstant());
        }

        if (fin != null) {
            LocalDateTime finDia = fin.plusDays(1).atStartOfDay().minusNanos(1);
            finDate = Date.from(finDia.atZone(ZoneId.systemDefault()).toInstant());
        }

        List<EventoResumenResponse> eventos = eventoService.listarEventos(tipo, estado, inicioDate, finDate)
                .stream().map(this::toResumen).collect(Collectors.toList());
        model.addAttribute("eventos", eventos);
        model.addAttribute("tipo", tipo);
        model.addAttribute("estado", estado);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);
        model.addAttribute("tiposEvento", TipoEvento.values());
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
    @PreAuthorize("hasAnyRole('PROFESOR','LIDER_ESTUDIANTIL','BIENESTAR')")
    public String crearEventoForm(Model model) {
        if (!model.containsAttribute("request")) {
            model.addAttribute("request", new CrearEventoRequest());
        }
        model.addAttribute("tiposEvento", TipoEvento.values());
        model.addAttribute("modoEdicion", false);
        return "eventos/crear";
    }

    @PostMapping("/crear")
    @PreAuthorize("hasAnyRole('PROFESOR','LIDER_ESTUDIANTIL','BIENESTAR')")
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
            model.addAttribute("modoEdicion", false);
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
            model.addAttribute("modoEdicion", false);
            model.addAttribute("error", e.getMessage());
            return "eventos/crear"; // forward, no redirect → conserva datos
        }
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyRole('PROFESOR','LIDER_ESTUDIANTIL','BIENESTAR')")
    public String editarEventoForm(@PathVariable String id, Authentication authentication, Model model) {
        Evento evento = eventoService.obtenerEvento(id);
        if (evento.getOrganizador() == null
                || !authentication.getName().equalsIgnoreCase(evento.getOrganizador().getCorreo())) {
            throw new IllegalArgumentException("No tienes permisos para editar este evento");
        }

        if (!model.containsAttribute("request")) {
            model.addAttribute("request", toCrearEventoRequest(evento));
        }
        model.addAttribute("tiposEvento", TipoEvento.values());
        model.addAttribute("modoEdicion", true);
        model.addAttribute("eventoId", id);
        return "eventos/crear";
    }

    @PostMapping("/{id}/editar")
    @PreAuthorize("hasAnyRole('PROFESOR','LIDER_ESTUDIANTIL','BIENESTAR')")
    public String editarEvento(
            @PathVariable String id,
            @Valid @ModelAttribute("request") CrearEventoRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposEvento", TipoEvento.values());
            model.addAttribute("modoEdicion", true);
            model.addAttribute("eventoId", id);
            model.addAttribute("error", "Por favor corrige los errores del formulario.");
            return "eventos/crear";
        }

        try {
            Evento evento = eventoService.actualizarEvento(id, request, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Evento actualizado exitosamente");
            return "redirect:/eventos/" + evento.getId();
        } catch (Exception e) {
            model.addAttribute("tiposEvento", TipoEvento.values());
            model.addAttribute("modoEdicion", true);
            model.addAttribute("eventoId", id);
            model.addAttribute("error", e.getMessage());
            return "eventos/crear";
        }
    }

    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasAnyRole('PROFESOR','LIDER_ESTUDIANTIL','BIENESTAR')")
    public String eliminarEvento(
            @PathVariable String id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            eventoService.eliminarEvento(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Evento eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/eventos/mis-eventos";
    }

    @GetMapping("/mis-eventos")
    @PreAuthorize("hasAnyRole('PROFESOR','LIDER_ESTUDIANTIL','BIENESTAR')")
    public String misEventos(Authentication authentication, Model model) {
        List<EventoResumenResponse> eventos = eventoService.listarEventosPorOrganizador(authentication.getName())
                .stream().map(this::toResumen).collect(Collectors.toList());
        model.addAttribute("eventos", eventos);
        return "eventos/mis-eventos";
    }

    @GetMapping("/{id}/inscritos")
    @PreAuthorize("hasAnyRole('PROFESOR','LIDER_ESTUDIANTIL','BIENESTAR')")
    public String listarInscritos(
            @PathVariable String id, Authentication authentication, Model model) {

        List<InscritoResponse> inscritos = eventoService.listarInscritos(id, authentication.getName())
                .stream().map(this::toInscrito).collect(Collectors.toList());
        model.addAttribute("inscritos", inscritos);
        model.addAttribute("eventoId", id);
        return "eventos/inscritos";
    }

    @PostMapping("/{id}/inscritos/{inscripcionId}/confirmar")
    @PreAuthorize("hasAnyRole('PROFESOR','LIDER_ESTUDIANTIL','BIENESTAR')")
    public String confirmarAsistencia(
            @PathVariable String id,
            @PathVariable String inscripcionId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            eventoService.confirmarAsistencia(id, inscripcionId, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Asistencia confirmada");
            return "redirect:/eventos/" + id + "/inscritos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/eventos/" + id + "/inscritos";
        }
    }

    @GetMapping("/{id}/inscritos/export")
    @PreAuthorize("hasAnyRole('PROFESOR','LIDER_ESTUDIANTIL','BIENESTAR')")
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
        r.setId(i.getId() != null ? i.getId().toHexString() : null);
        r.setNombre(i.getNombre());
        r.setCodigoEstudiante(i.getCodigoEstudiante());
        r.setCorreo(i.getCorreo());
        r.setFechaInscripcion(i.getFechaInscripcion());
        r.setConfirmada(i.getConfirmada());
        return r;
    }

    private CrearEventoRequest toCrearEventoRequest(Evento evento) {
        CrearEventoRequest request = new CrearEventoRequest();
        request.setTitulo(evento.getTitulo());
        request.setDescripcion(evento.getDescripcion());
        request.setTipo(evento.getTipo());
        request.setFechaHoraInicio(evento.getFechaHoraInicio());
        request.setFechaHoraFin(evento.getFechaHoraFin());
        request.setUbicacion(evento.getUbicacion());
        request.setMaxAsistentes(evento.getMaxAsistentes());
        request.setDatosEspecificos(toDatosEspecificosRequest(evento.getDatosEspecificos()));
        return request;
    }

    private DatosEspecificosRequest toDatosEspecificosRequest(DatosEspecificos datosEspecificos) {
        DatosEspecificosRequest request = new DatosEspecificosRequest();
        if (datosEspecificos == null) {
            return request;
        }

        if (datosEspecificos instanceof Charla charla) {
            ConferencistaRequest conferencista = new ConferencistaRequest();
            conferencista.setNombre(charla.getConferencista().getNombre());
            conferencista.setPerfil(charla.getConferencista().getPerfil());
            conferencista.setAfiliacion(charla.getConferencista().getAfiliacion());
            request.setConferencista(conferencista);
            request.setEnlaces(charla.getEnlaces());
            request.setDescripcionExtendida(charla.getDescripcion());
            return request;
        }

        if (datosEspecificos instanceof Taller taller) {
            request.setMaterialesRequeridos(taller.getMaterialesRequeridos());
            request.setCondicionesPrevias(taller.getCondicionesPrevias());
            return request;
        }

        if (datosEspecificos instanceof TorneoDeportivo torneo) {
            request.setTipoDeporte(torneo.getTipoDeporte());
            request.setReglas(torneo.getReglas());
            request.setNumeroEquipos(torneo.getNumeroEquipos());
            request.setEstructuraTorneo(torneo.getEstructuraTorneo());
            return request;
        }

        if (datosEspecificos instanceof ActividadVoluntariado voluntariado) {
            request.setCausa(voluntariado.getCausa());
            request.setNumeroHorasRequeridas(voluntariado.getNumeroHorasRequeridas());
            request.setActividades(voluntariado.getActividades());
            request.setPuntosEncuentro(voluntariado.getPuntosEncuentro());
            request.setResponsables(voluntariado.getResponsables());
            return request;
        }

        if (datosEspecificos instanceof OtroEvento otro) {
            request.setDescripcionAdicional(otro.getDescripcionAdicional());
        }

        return request;
    }
}
