package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.PaginaResponse;
import com.devsuj.crmapi.dto.cliente.ClienteRequest;
import com.devsuj.crmapi.dto.cliente.ClienteResponse;
import com.devsuj.crmapi.dto.cliente.ReasignacionLoteRequest;
import com.devsuj.crmapi.dto.cliente.ReasignacionResponse;
import com.devsuj.crmapi.exception.AccesoDenegadoException;
import com.devsuj.crmapi.exception.RecursoNoEncontradoException;
import com.devsuj.crmapi.exception.ReglaNegocioException;
import com.devsuj.crmapi.mapper.ClienteMapper;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.EstadoCliente;
import com.devsuj.crmapi.model.Rol;
import com.devsuj.crmapi.model.Usuario;
import com.devsuj.crmapi.repository.ClienteRepository;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.repository.VentaRepository;
import com.devsuj.crmapi.security.ControlAcceso;
import com.devsuj.crmapi.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clientes: todos los usuarios los ven (evita duplicados y muestra quién es el responsable), pero solo el
 * vendedor asignado, su gerente o un ADMIN los gestionan (P3). Nunca se borran: pasan a INACTIVO (P5).
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;
    private final UsuarioService usuarioService;
    private final ClienteMapper mapper;
    private final ControlAcceso controlAcceso;

    @Transactional(readOnly = true)
    public PaginaResponse<ClienteResponse> listar(EstadoCliente estado, Integer vendedorId, String q, Pageable pageable) {
        Specification<Cliente> filtro = Specification.allOf(
                Filtros.igual(estado, "estado"),
                Filtros.igual(vendedorId, "vendedor", "id"),
                Filtros.texto(q, "nombre", "empresa", "email"));
        return PaginaResponse.de(clienteRepository.findAll(filtro, pageable), mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Integer id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByEmail(request.email())) {
            throw emailDuplicado(request.email());
        }
        Cliente cliente = new Cliente();
        mapper.copiarContacto(request, cliente);
        cliente.setEstado(EstadoCliente.PROSPECTO);
        cliente.setVendedor(responsableAlCrear(request.vendedorId()));
        return mapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {
        Cliente cliente = buscarGestionable(id);
        if (clienteRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw emailDuplicado(request.email());
        }
        mapper.copiarContacto(request, cliente);
        Integer actual = cliente.getVendedor() == null ? null : cliente.getVendedor().getId();
        if (request.vendedorId() != null && !request.vendedorId().equals(actual)) {
            verificarPuedeReasignar();
            cliente.setVendedor(responsableValido(request.vendedorId()));
        }
        return mapper.toResponse(cliente);
    }

    @Transactional
    public ClienteResponse cambiarEstado(Integer id, EstadoCliente estado) {
        Cliente cliente = buscarGestionable(id);
        if (estado == EstadoCliente.PROSPECTO && cliente.getEstado() != EstadoCliente.PROSPECTO
                && ventaRepository.existsByClienteId(id)) {
            throw new ReglaNegocioException("El cliente ya tiene compras: no puede volver a PROSPECTO.");
        }
        cliente.setEstado(estado);
        return mapper.toResponse(cliente);
    }

    @Transactional
    public ClienteResponse asignarVendedor(Integer id, Integer vendedorId) {
        Cliente cliente = buscarGestionable(id);
        cliente.setVendedor(vendedorId == null ? null : responsableValido(vendedorId));
        return mapper.toResponse(cliente);
    }

    @Transactional
    public ReasignacionResponse reasignarLote(ReasignacionLoteRequest request) {
        Set<Integer> ids = new HashSet<>(request.clienteIds());
        List<Cliente> clientes = clienteRepository.findByIdIn(ids);
        if (clientes.size() != ids.size()) {
            Set<Integer> encontrados = new HashSet<>(clientes.stream().map(Cliente::getId).toList());
            Integer faltante = ids.stream().filter(i -> !encontrados.contains(i)).findFirst().orElseThrow();
            throw new RecursoNoEncontradoException("un cliente", faltante);
        }
        Usuario nuevo = responsableValido(request.vendedorId());
        clientes.forEach(controlAcceso::verificarGestionCliente);
        clientes.forEach(c -> c.setVendedor(nuevo));
        return new ReasignacionResponse(clientes.size(), nuevo.getId(), nuevo.getNombre());
    }

    /** Para otros módulos: el cliente existe (404). */
    @Transactional(readOnly = true)
    public Cliente buscar(Integer id) {
        return clienteRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("un cliente", id));
    }

    /** Para otros módulos: existe y el usuario actual puede gestionarlo (403). */
    @Transactional(readOnly = true)
    public Cliente buscarGestionable(Integer id) {
        Cliente cliente = buscar(id);
        controlAcceso.verificarGestionCliente(cliente);
        return cliente;
    }

    /** Para otros módulos: gestionable y no INACTIVO (P10: no se vende ni se canjea a un cliente inactivo). */
    @Transactional(readOnly = true)
    public Cliente buscarOperable(Integer id, String accion) {
        Cliente cliente = buscarGestionable(id);
        if (cliente.getEstado() == EstadoCliente.INACTIVO) {
            throw new ReglaNegocioException("El cliente " + cliente.getNombre() + " está INACTIVO: no se puede "
                    + accion + ". Reactívalo primero.");
        }
        return cliente;
    }

    /** Para canjes: bloquea la fila del cliente hasta el fin de la transacción (evita dos canjes a la vez) y valida que sea operable. */
    @Transactional
    public Cliente bloquearOperable(Integer id, String accion) {
        clienteRepository.bloquearPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("un cliente", id));
        return buscarOperable(id, accion);
    }

    private Usuario responsableAlCrear(Integer vendedorId) {
        UsuarioActual actual = controlAcceso.usuario();
        if (actual.esVendedor()) {
            if (vendedorId != null && !vendedorId.equals(actual.id())) {
                throw new AccesoDenegadoException("Un vendedor solo puede registrar clientes a su nombre.");
            }
            return usuarioService.buscarActivo(actual.id());
        }
        return vendedorId == null ? null : responsableValido(vendedorId);
    }

    /** Responsable válido: existe, activo, VENDEDOR o GERENTE y dentro del alcance de quien asigna. */
    private Usuario responsableValido(Integer usuarioId) {
        Usuario usuario = usuarioService.buscarActivo(usuarioId);
        if (usuario.tieneRol(Rol.ADMIN)) {
            throw new ReglaNegocioException("Un cliente solo se asigna a un VENDEDOR o a un GERENTE.");
        }
        if (!controlAcceso.alcance().incluye(usuario.getId())) {
            throw new AccesoDenegadoException("Solo puedes asignar clientes a vendedores de tu equipo.");
        }
        return usuario;
    }

    private void verificarPuedeReasignar() {
        if (controlAcceso.usuario().esVendedor()) {
            throw new AccesoDenegadoException("Solo un gerente o un administrador pueden reasignar clientes.");
        }
    }

    private static ReglaNegocioException emailDuplicado(String email) {
        String mensaje = "Ya existe un cliente con el email " + email + ".";
        return new ReglaNegocioException(mensaje, Map.of("errores", Map.of("email", mensaje)));
    }
}
