package com.devsuj.crmapi.mapper;

import com.devsuj.crmapi.dto.cliente.ClienteRequest;
import com.devsuj.crmapi.dto.cliente.ClienteResponse;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public ClienteResponse toResponse(Cliente cliente) {
        Usuario vendedor = cliente.getVendedor();
        return new ClienteResponse(cliente.getId(), cliente.getNombre(), cliente.getEmpresa(), cliente.getEmail(),
                cliente.getTelefono(), cliente.getDireccion(), cliente.getEstado(),
                vendedor == null ? null : vendedor.getId(), vendedor == null ? null : vendedor.getNombre(),
                cliente.getCreadoEn(), cliente.getActualizadoEn());
    }

    /** Copia los datos de contacto (el responsable y el estado tienen reglas propias en el service). */
    public void copiarContacto(ClienteRequest request, Cliente cliente) {
        cliente.setNombre(request.nombre());
        cliente.setEmpresa(request.empresa());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setDireccion(request.direccion());
    }
}
