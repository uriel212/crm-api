package com.devsuj.crmapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

/** Llave compuesta de {@link CampanaCliente}. Un record es inmutable y ya trae equals/hashCode. */
@Embeddable
public record CampanaClienteId(
        @Column(name = "campana_id") Integer campanaId,
        @Column(name = "cliente_id") Integer clienteId) implements Serializable {
}
