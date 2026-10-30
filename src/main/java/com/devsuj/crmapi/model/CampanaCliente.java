package com.devsuj.crmapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Destinatario de una campaña y su resultado (tabla con llave compuesta). */
@Entity
@Table(name = "campana_cliente")
@Getter
@Setter
@NoArgsConstructor
public class CampanaCliente {

    @EmbeddedId
    private CampanaClienteId id;

    @MapsId("campanaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campana_id")
    private Campana campana;

    @MapsId("clienteId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoCampana resultado = ResultadoCampana.ENVIADO;

    public CampanaCliente(Campana campana, Cliente cliente) {
        this.id = new CampanaClienteId(campana.getId(), cliente.getId());
        this.campana = campana;
        this.cliente = cliente;
    }
}
