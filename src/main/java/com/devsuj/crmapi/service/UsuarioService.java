package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.usuario.CambioEstadoUsuarioRequest;
import com.devsuj.crmapi.dto.usuario.CambioPasswordRequest;
import com.devsuj.crmapi.dto.usuario.UsuarioActualizarRequest;
import com.devsuj.crmapi.dto.usuario.UsuarioCrearRequest;
import com.devsuj.crmapi.dto.usuario.UsuarioResponse;
import com.devsuj.crmapi.exception.AccesoDenegadoException;
import com.devsuj.crmapi.exception.DatosInvalidosException;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.UsuarioMapper;
import com.devsuj.crmapi.model.Rol;
import com.devsuj.crmapi.model.Usuario;
import com.devsuj.crmapi.repository.ClienteRepository;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.repository.UsuarioRepository;
import com.devsuj.crmapi.security.ControlAcceso;
import com.devsuj.crmapi.security.SesionActual;
import com.devsuj.crmapi.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final SesionActual sesion;
    private final ControlAcceso controlAcceso;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PaginaResponse<UsuarioResponse> listar(Rol rol, Boolean activo, String q, Pageable pageable) {
        Specification<Usuario> filtro = Specification.allOf(
                Filtros.igual(rol, "rol"), Filtros.igual(activo, "activo"), Filtros.texto(q, "nombre", "email"));
        return PaginaResponse.de(usuarioRepository.findAll(filtro, pageable), mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Integer id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerActual() {
        return obtener(sesion.obtener().id());
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw emailDuplicado(request.email());
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setRol(request.rol());
        usuario.setGerente(resolverGerente(request.rol(), request.gerenteId()));
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        return mapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioActualizarRequest request) {
        Usuario usuario = buscar(id);
        if (usuarioRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw emailDuplicado(request.email());
        }
        if (usuario.tieneRol(Rol.GERENTE) && request.rol() != Rol.GERENTE) {
            verificarSinEquipo(usuario, "cambiarle el rol");
        }
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setRol(request.rol());
        usuario.setGerente(resolverGerente(request.rol(), request.gerenteId()));
        return mapper.toResponse(usuario);
    }

    /**
     * Al desactivar a alguien con clientes asignados se responde 409 con el conteo, salvo que se confirme
     * {@code reasignarClientes}: entonces sus clientes pasan a su gerente (P4).
     */
    @Transactional
    public UsuarioResponse cambiarEstado(Integer id, CambioEstadoUsuarioRequest request) {
        Usuario usuario = buscar(id);
        if (!request.activo() && usuario.isActivo()) {
            desactivar(usuario, request.confirmaReasignacion());
        }
        usuario.setActivo(request.activo());
        return mapper.toResponse(usuario);
    }

    @Transactional
    public void cambiarPassword(Integer id, CambioPasswordRequest request) {
        UsuarioActual actual = sesion.obtener();
        boolean propio = actual.id().equals(id);
        if (!propio && !actual.esAdmin()) {
            throw new AccesoDenegadoException("Solo puedes cambiar tu propia contraseña.");
        }
        Usuario usuario = buscar(id);
        if (propio && !actual.esAdmin() && (request.passwordActual() == null
                || !passwordEncoder.matches(request.passwordActual(), usuario.getPasswordHash()))) {
            throw new DatosInvalidosException("La contraseña actual no es correcta.", "passwordActual");
        }
        usuario.setPasswordHash(passwordEncoder.encode(request.passwordNueva()));
    }

    /** Para otros services: el usuario existe (404). */
    @Transactional(readOnly = true)
    public Usuario buscar(Integer id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("un usuario", id));
    }

    /** Para otros services: el usuario existe y está activo (409 si está desactivado). */
    @Transactional(readOnly = true)
    public Usuario buscarActivo(Integer id) {
        Usuario usuario = buscar(id);
        if (!usuario.isActivo()) {
            throw new ReglaNegocioException("El usuario " + usuario.getNombre() + " está desactivado.");
        }
        return usuario;
    }

    /**
     * Responsable de un registro nuevo (oportunidad, venta): por defecto quien hace la petición;
     * GERENTE/ADMIN pueden indicar a alguien de su alcance. Debe estar activo.
     */
    @Transactional(readOnly = true)
    public Usuario responsableParaRegistro(Integer usuarioIdSolicitado) {
        UsuarioActual actual = sesion.obtener();
        if (usuarioIdSolicitado == null || usuarioIdSolicitado.equals(actual.id())) {
            return buscarActivo(actual.id());
        }
        if (!controlAcceso.alcance().incluye(usuarioIdSolicitado)) {
            throw new AccesoDenegadoException("Solo puedes registrar a nombre de vendedores de tu equipo.");
        }
        return buscarActivo(usuarioIdSolicitado);
    }

    private void desactivar(Usuario usuario, boolean reasignarClientes) {
        if (usuario.getId().equals(sesion.obtener().id())) {
            throw new ReglaNegocioException("No puedes desactivar tu propio usuario.");
        }
        if (usuario.tieneRol(Rol.GERENTE)) {
            verificarSinEquipo(usuario, "desactivarlo");
        }
        long clientes = clienteRepository.countByVendedorId(usuario.getId());
        if (clientes == 0) {
            return;
        }
        Usuario gerente = usuario.getGerente();
        String destino = gerente == null ? "quedarán sin vendedor" : "pasarán a su gerente " + gerente.getNombre();
        if (!reasignarClientes) {
            throw new ReglaNegocioException(usuario.getNombre() + " tiene " + clientes + " cliente(s) asignado(s), que "
                    + destino + ". Confirma con reasignarClientes = true.",
                    Map.of("clientesAsignados", clientes));
        }
        clienteRepository.reasignarTodos(usuario.getId(), gerente, LocalDateTime.now(clock));
    }

    private void verificarSinEquipo(Usuario gerente, String accion) {
        long equipo = usuarioRepository.countByGerenteIdAndActivoTrue(gerente.getId());
        if (equipo > 0) {
            throw new ReglaNegocioException(gerente.getNombre() + " tiene " + equipo
                    + " vendedor(es) activo(s) en su equipo. Pásalos a otro gerente antes de " + accion + ".",
                    Map.of("vendedoresEnEquipo", equipo));
        }
    }

    private Usuario resolverGerente(Rol rol, Integer gerenteId) {
        if (gerenteId == null) {
            return null;
        }
        if (rol != Rol.VENDEDOR) {
            throw new DatosInvalidosException("Solo un VENDEDOR pertenece al equipo de un gerente.");
        }
        Usuario gerente = buscarActivo(gerenteId);
        if (!gerente.tieneRol(Rol.GERENTE)) {
            throw new ReglaNegocioException(gerente.getNombre() + " no tiene rol GERENTE.");
        }
        return gerente;
    }

    private static ReglaNegocioException emailDuplicado(String email) {
        String mensaje = "Ya existe un usuario con el email " + email + ".";
        return new ReglaNegocioException(mensaje, Map.of("errores", Map.of("email", mensaje)));
    }
}
