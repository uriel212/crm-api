package com.devsuj.crmapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Oferta con vigencia. Alcance PRODUCTO (por renglón) o VENTA (sobre el total, con monto mínimo). */
@Entity
@Table(name = "promocion")
@Getter
@Setter
@NoArgsConstructor
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campana_id")
    private Campana campana;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPromocion tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlcancePromocion alcance = AlcancePromocion.PRODUCTO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "monto_minimo", precision = 12, scale = 2)
    private BigDecimal montoMinimo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private boolean activa = true;

    @ManyToMany
    @BatchSize(size = 50)
    @JoinTable(name = "promocion_producto",
            joinColumns = @JoinColumn(name = "promocion_id"),
            inverseJoinColumns = @JoinColumn(name = "producto_id"))
    private Set<Producto> productos = new HashSet<>();

    /** Vigente = activa, dentro de sus fechas y, si la anuncia una campaña, con la campaña ACTIVA (P18). */
    public boolean estaVigente(LocalDate fecha) {
        boolean enFechas = !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
        boolean campanaOk = campana == null || campana.getEstado() == EstadoCampana.ACTIVA;
        return activa && enFechas && campanaOk;
    }
}
