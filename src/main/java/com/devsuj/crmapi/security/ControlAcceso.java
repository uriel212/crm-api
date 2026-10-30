package com.devsuj.crmapi.security;

import com.devsuj.crmapi.exception.AccesoDenegadoException;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.Usuario;
import com.devsuj.crmapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Reglas de visibilidad (P3) en un solo lugar:
 * <ul>
 *   <li>VENDEDOR: ve y modifica solo lo suyo (y lo de los clientes que tiene asignados).</li>
 *   <li>GERENTE: lo suyo y lo de los vendedores de su equipo.</li>
 *   <li>ADMIN: todo.</li>
 * </ul>
 * Los clientes los ven todos, pero solo los gestiona el vendedor asignado, su gerente o un ADMIN.
 */
@Component
@RequiredArgsConstructor
public class ControlAcceso {

    private final SesionActual sesion;
    private final UsuarioRepository usuarioRepository;

    public UsuarioActual usuario() {
        return sesion.obtener();
    }

    public Alcance alcance() {
        UsuarioActual actual = sesion.obtener();
        if (actual.esAdmin()) {
            return Alcance.total();
        }
        Set<Integer> ids = new HashSet<>();
        ids.add(actual.id());
        if (actual.esGerente()) {
            ids.addAll(usuarioRepository.findIdsDelEquipo(actual.id()));
        }
        return new Alcance(false, Set.copyOf(ids));
    }

    /** Valida el filtro {@code usuarioId} de un listado: no se puede pedir lo de alguien fuera del alcance. */
    public void verificarFiltroUsuario(Alcance alcance, Integer usuarioId) {
        if (usuarioId != null && !alcance.incluye(usuarioId)) {
            throw new AccesoDenegadoException("No puedes consultar los registros de ese usuario.");
        }
    }

    /** Modificar el cliente o registrarle oportunidades, ventas, interacciones, contratos o canjes. */
    public void verificarGestionCliente(Cliente cliente) {
        UsuarioActual actual = sesion.obtener();
        if (actual.esAdmin()) {
            return;
        }
        Usuario vendedor = cliente.getVendedor();
        boolean permitido = vendedor == null ? actual.esGerente() : alcance().incluye(vendedor.getId());
        if (!permitido) {
            throw new AccesoDenegadoException(
                    "Solo el vendedor asignado, su gerente o un administrador pueden gestionar este cliente.");
        }
    }

    /** Ver un registro (oportunidad, venta, interacción...): es del alcance o su cliente está asignado al alcance. */
    public void verificarLectura(Usuario dueno, Cliente cliente, String recurso) {
        Alcance alcance = alcance();
        boolean clienteVisible = cliente.getVendedor() != null && alcance.incluye(cliente.getVendedor().getId());
        if (!alcance.incluye(dueno == null ? null : dueno.getId()) && !clienteVisible) {
            throw new AccesoDenegadoException("No tienes acceso a " + recurso + ".");
        }
    }

    /** Modificar un registro propio: solo su dueño, el gerente del dueño o un ADMIN. */
    public void verificarModificacion(Usuario dueno, String recurso) {
        if (!alcance().incluye(dueno.getId())) {
            throw new AccesoDenegadoException("Solo el responsable, su gerente o un administrador pueden modificar "
                    + recurso + ".");
        }
    }
}
