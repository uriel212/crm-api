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

/** Posible negocio que avanza por etapas del pipeline. */
@Entity
@Table(name = "oportunidad")
@Getter
@Setter
@NoArgsConstructor
public class Oportunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Oportunidad PERDIDA de la que esta es un nuevo intento (las perdidas no se reabren). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oportunidad_anterior_id")
    private Oportunidad oportunidadAnterior;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(name = "monto_estimado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoEstimado = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtapaOportunidad etapa = EtapaOportunidad.PROSPECTO;

    @Column(name = "fecha_cierre_estimada")
    private LocalDate fechaCierreEstimada;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
}
