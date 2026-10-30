package com.devsuj.crmapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Un paso del pipeline: la oportunidad pasó de {@code etapaAnterior} (null = creación) a {@code etapaNueva}. */
@Entity
@Table(name = "oportunidad_historial_etapa")
@Getter
@Setter
@NoArgsConstructor
public class OportunidadHistorialEtapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oportunidad_id", nullable = false)
    private Oportunidad oportunidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_anterior")
    private EtapaOportunidad etapaAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa_nueva", nullable = false)
    private EtapaOportunidad etapaNueva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private String comentario;
}
