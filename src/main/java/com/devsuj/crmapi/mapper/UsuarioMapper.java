package com.devsuj.crmapi.mapper;

import com.devsuj.crmapi.dto.usuario.UsuarioResponse;
import com.devsuj.crmapi.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        Usuario gerente = usuario.getGerente();
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol(),
                gerente == null ? null : gerente.getId(), gerente == null ? null : gerente.getNombre(),
                usuario.isActivo(), usuario.getCreadoEn());
    }
}
