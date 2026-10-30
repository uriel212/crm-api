package com.devsuj.crmapi.security;

import com.devsuj.crmapi.model.Rol;

/** Usuario que hace la petición, leído del token JWT. */
public record UsuarioActual(Integer id, String email, Rol rol) {

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    public boolean esGerente() {
        return rol == Rol.GERENTE;
    }

    public boolean esVendedor() {
        return rol == Rol.VENDEDOR;
    }
}
