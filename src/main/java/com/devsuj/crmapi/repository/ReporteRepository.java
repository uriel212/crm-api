package com.devsuj.crmapi.repository;

import com.devsuj.crmapi.dto.reporte.ProductoVendidoResponse;
import com.devsuj.crmapi.dto.reporte.VentasPorCategoriaResponse;
import com.devsuj.crmapi.dto.reporte.VentasPorVendedorResponse;
import com.devsuj.crmapi.model.EstadoCliente;
import com.devsuj.crmapi.model.EstadoTicket;
import com.devsuj.crmapi.model.EtapaOportunidad;
import com.devsuj.crmapi.model.PrioridadTicket;
import com.devsuj.crmapi.security.Alcance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consultas agregadas de solo lectura para reportes y KPIs: la base de datos suma y agrupa, nunca se cargan
 * entidades en memoria. Todas reciben el {@link Alcance}: ventas y oportunidades por su responsable,
 * clientes por su vendedor asignado. Rangos de fecha [desde, hasta).
 */
@Repository
@RequiredArgsConstructor
public class ReporteRepository {

    private static final String ALCANCE_VENTA = " and (:todos = true or v.usuario.id in :ids)";

    private final EntityManager em;

    /** [numeroVentas, total] del periodo. */
    public Totales totalesVentas(Alcance alcance, LocalDateTime desde, LocalDateTime hasta) {
        Object[] fila = conAlcance(em.createQuery("""
                select count(v), coalesce(sum(v.total), 0) from Venta v
                where v.fecha >= :desde and v.fecha < :hasta""" + ALCANCE_VENTA, Object[].class), alcance)
                .setParameter("desde", desde).setParameter("hasta", hasta)
                .getSingleResult();
        return new Totales(largo(fila[0]), monto(fila[1]));
    }

    /** mes (1–12) → totales; solo los meses con ventas. */
    public Map<Integer, Totales> ventasPorMes(Alcance alcance, LocalDateTime desde, LocalDateTime hasta) {
        List<Object[]> filas = conAlcance(em.createQuery("""
                select extract(month from v.fecha), count(v), sum(v.total) from Venta v
                where v.fecha >= :desde and v.fecha < :hasta""" + ALCANCE_VENTA + """
                 group by extract(month from v.fecha)""", Object[].class), alcance)
                .setParameter("desde", desde).setParameter("hasta", hasta)
                .getResultList();
        Map<Integer, Totales> porMes = new HashMap<>();
        filas.forEach(f -> porMes.put(((Number) f[0]).intValue(), new Totales(largo(f[1]), monto(f[2]))));
        return porMes;
    }

    public List<VentasPorVendedorResponse> ventasPorVendedor(Alcance alcance, LocalDateTime desde, LocalDateTime hasta) {
        return conAlcance(em.createQuery("""
                select u.id, u.nombre, count(v), sum(v.total) from Venta v join v.usuario u
                where v.fecha >= :desde and v.fecha < :hasta""" + ALCANCE_VENTA + """
                 group by u.id, u.nombre order by sum(v.total) desc""", Object[].class), alcance)
                .setParameter("desde", desde).setParameter("hasta", hasta)
                .getResultList().stream()
                .map(f -> {
                    Totales totales = new Totales(largo(f[2]), monto(f[3]));
                    return new VentasPorVendedorResponse((Integer) f[0], (String) f[1], totales.cantidad(),
                            totales.monto(), totales.promedio());
                })
                .toList();
    }

    public List<VentasPorCategoriaResponse> ventasPorCategoria(Alcance alcance, LocalDateTime desde, LocalDateTime hasta) {
        return conAlcance(em.createQuery("""
                select c.id, c.nombre, sum(d.cantidad), sum(d.subtotal)
                from DetalleVenta d join d.venta v join d.producto p join p.categoria c
                where v.fecha >= :desde and v.fecha < :hasta""" + ALCANCE_VENTA + """
                 group by c.id, c.nombre order by sum(d.subtotal) desc""", Object[].class), alcance)
                .setParameter("desde", desde).setParameter("hasta", hasta)
                .getResultList().stream()
                .map(f -> new VentasPorCategoriaResponse((Integer) f[0], (String) f[1], largo(f[2]), monto(f[3])))
                .toList();
    }

    public List<ProductoVendidoResponse> productosMasVendidos(Alcance alcance, LocalDateTime desde,
            LocalDateTime hasta, int limite) {
        return conAlcance(em.createQuery("""
                select p.id, p.nombre, sum(d.cantidad), sum(d.subtotal)
                from DetalleVenta d join d.venta v join d.producto p
                where v.fecha >= :desde and v.fecha < :hasta""" + ALCANCE_VENTA + """
                 group by p.id, p.nombre order by sum(d.cantidad) desc, sum(d.subtotal) desc""", Object[].class), alcance)
                .setParameter("desde", desde).setParameter("hasta", hasta)
                .setMaxResults(limite)
                .getResultList().stream()
                .map(f -> new ProductoVendidoResponse((Integer) f[0], (String) f[1], largo(f[2]), monto(f[3])))
                .toList();
    }

    /** etapa → [cantidad, monto estimado] de las oportunidades actuales; solo las etapas con oportunidades. */
    public Map<EtapaOportunidad, Totales> pipeline(Alcance alcance) {
        List<Object[]> filas = conAlcance(em.createQuery("""
                select o.etapa, count(o), coalesce(sum(o.montoEstimado), 0) from Oportunidad o
                where (:todos = true or o.usuario.id in :ids)
                group by o.etapa""", Object[].class), alcance)
                .getResultList();
        Map<EtapaOportunidad, Totales> porEtapa = new EnumMap<>(EtapaOportunidad.class);
        filas.forEach(f -> porEtapa.put((EtapaOportunidad) f[0], new Totales(largo(f[1]), monto(f[2]))));
        return porEtapa;
    }

    /**
     * Cierres (GANADA/PERDIDA) en el periodo según la fecha del paso de cierre en el historial, por responsable
     * de la oportunidad: usuarioId → etapa → cantidad.
     */
    public Map<Integer, Map<EtapaOportunidad, Long>> cierresPorUsuario(Alcance alcance, LocalDateTime desde,
            LocalDateTime hasta) {
        List<Object[]> filas = conAlcance(em.createQuery("""
                select o.usuario.id, h.etapaNueva, count(distinct o.id)
                from OportunidadHistorialEtapa h join h.oportunidad o
                where h.etapaNueva in :cierres and h.fecha >= :desde and h.fecha < :hasta
                  and (:todos = true or o.usuario.id in :ids)
                group by o.usuario.id, h.etapaNueva""", Object[].class), alcance)
                .setParameter("cierres", List.of(EtapaOportunidad.GANADA, EtapaOportunidad.PERDIDA))
                .setParameter("desde", desde).setParameter("hasta", hasta)
                .getResultList();
        Map<Integer, Map<EtapaOportunidad, Long>> porUsuario = new HashMap<>();
        filas.forEach(f -> porUsuario.computeIfAbsent((Integer) f[0], k -> new EnumMap<>(EtapaOportunidad.class))
                .put((EtapaOportunidad) f[1], largo(f[2])));
        return porUsuario;
    }

    public long clientesPorEstado(Alcance alcance, EstadoCliente estado) {
        return conAlcance(em.createQuery("""
                select count(c) from Cliente c left join c.vendedor cv
                where c.estado = :estado and (:todos = true or cv.id in :ids)""", Long.class), alcance)
                .setParameter("estado", estado)
                .getSingleResult();
    }

    public long clientesNuevos(Alcance alcance, LocalDateTime desde, LocalDateTime hasta) {
        return conAlcance(em.createQuery("""
                select count(c) from Cliente c left join c.vendedor cv
                where c.creadoEn >= :desde and c.creadoEn < :hasta
                  and (:todos = true or cv.id in :ids)""", Long.class), alcance)
                .setParameter("desde", desde).setParameter("hasta", hasta)
                .getSingleResult();
    }

    /** Tickets sin cerrar del alcance: atendidos por alguien del alcance o de clientes asignados al alcance. */
    public long ticketsAbiertos(Alcance alcance) {
        return conAlcance(em.createQuery("""
                select count(t) from TicketSoporte t left join t.usuario u join t.cliente c left join c.vendedor cv
                where t.estado <> :cerrado and (:todos = true or u.id in :ids or cv.id in :ids)""", Long.class), alcance)
                .setParameter("cerrado", EstadoTicket.CERRADO)
                .getSingleResult();
    }

    public long productosConStockBajo() {
        return em.createQuery("select count(p) from Producto p where p.activo = true and p.stock <= p.stockMinimo",
                Long.class).getSingleResult();
    }

    public BigDecimal sumaMetas(Alcance alcance, int anio, int mes) {
        return monto(conAlcance(em.createQuery("""
                select sum(m.montoObjetivo) from MetaVenta m
                where m.anio = :anio and m.mes = :mes and (:todos = true or m.usuario.id in :ids)""",
                BigDecimal.class), alcance)
                .setParameter("anio", anio).setParameter("mes", mes)
                .getSingleResult());
    }

    // ---------- Soporte (9.8) ----------

    private static final String ALCANCE_TICKET = """
             from TicketSoporte t left join t.usuario u join t.cliente c left join c.vendedor cv
            where (:todos = true or u.id in :ids or cv.id in :ids)""";

    public Map<EstadoTicket, Long> ticketsPorEstado(Alcance alcance) {
        Map<EstadoTicket, Long> porEstado = new EnumMap<>(EstadoTicket.class);
        conAlcance(em.createQuery("select t.estado, count(t)" + ALCANCE_TICKET + " group by t.estado", Object[].class),
                alcance).getResultList().forEach(f -> porEstado.put((EstadoTicket) f[0], largo(f[1])));
        return porEstado;
    }

    public Map<PrioridadTicket, Long> pendientesPorPrioridad(Alcance alcance) {
        Map<PrioridadTicket, Long> porPrioridad = new EnumMap<>(PrioridadTicket.class);
        conAlcance(em.createQuery("select t.prioridad, count(t)" + ALCANCE_TICKET
                + " and t.estado <> :cerrado group by t.prioridad", Object[].class), alcance)
                .setParameter("cerrado", EstadoTicket.CERRADO)
                .getResultList().forEach(f -> porPrioridad.put((PrioridadTicket) f[0], largo(f[1])));
        return porPrioridad;
    }

    public long ticketsSinAsignar(Alcance alcance) {
        return conAlcance(em.createQuery("select count(t)" + ALCANCE_TICKET
                + " and t.estado <> :cerrado and u is null", Long.class), alcance)
                .setParameter("cerrado", EstadoTicket.CERRADO)
                .getSingleResult();
    }

    /** Horas promedio de creación a cierre (SQL nativo de MySQL: TIMESTAMPDIFF); null si no hay cerrados. */
    public BigDecimal horasPromedioCierre(Alcance alcance) {
        Object valor = em.createNativeQuery("""
                select avg(timestampdiff(minute, t.creado_en, t.cerrado_en)) / 60
                from ticket_soporte t join cliente c on c.id = t.cliente_id
                where t.estado = 'CERRADO' and t.cerrado_en is not null
                  and (:todos = true or t.usuario_id in (:ids) or c.vendedor_id in (:ids))""")
                .setParameter("todos", alcance.todos()).setParameter("ids", alcance.idsParaConsulta())
                .getSingleResult();
        return valor == null ? null : monto(valor);
    }

    // ---------- Fidelización (9.8) ----------

    /** [clienteId, nombre, ganados, canjeados] por cliente con movimientos, clientes por su vendedor asignado. */
    public List<Object[]> puntosPorCliente(Alcance alcance) {
        return conAlcance(em.createQuery("""
                select c.id, c.nombre,
                       sum(case when m.tipo = com.devsuj.crmapi.model.TipoMovimientoPuntos.GANADO then m.puntos else 0 end),
                       sum(case when m.tipo = com.devsuj.crmapi.model.TipoMovimientoPuntos.CANJEADO then m.puntos else 0 end)
                from MovimientoPuntos m join m.cliente c left join c.vendedor cv
                where (:todos = true or cv.id in :ids)
                group by c.id, c.nombre""", Object[].class), alcance)
                .getResultList();
    }

    // ---------- Vista 360 del cliente (9.9) ----------

    /** [numeroCompras, totalComprado, ultimaCompra]. */
    public Object[] comprasDe(Integer clienteId) {
        return em.createQuery("""
                select count(v), coalesce(sum(v.total), 0), max(v.fecha) from Venta v where v.cliente.id = :id""",
                Object[].class).setParameter("id", clienteId).getSingleResult();
    }

    public Totales oportunidadesAbiertasDe(Integer clienteId) {
        Object[] fila = em.createQuery("""
                select count(o), coalesce(sum(o.montoEstimado), 0) from Oportunidad o
                where o.cliente.id = :id and o.etapa not in :cerradas""", Object[].class)
                .setParameter("id", clienteId)
                .setParameter("cerradas", List.of(EtapaOportunidad.GANADA, EtapaOportunidad.PERDIDA))
                .getSingleResult();
        return new Totales(largo(fila[0]), monto(fila[1]));
    }

    public LocalDateTime ultimaInteraccionDe(Integer clienteId) {
        return em.createQuery("""
                select max(i.fecha) from Interaccion i
                where i.cliente.id = :id and i.estado = com.devsuj.crmapi.model.EstadoInteraccion.REALIZADA""",
                LocalDateTime.class).setParameter("id", clienteId).getSingleResult();
    }

    public long actividadesProgramadasDe(Integer clienteId) {
        return contar("""
                select count(i) from Interaccion i
                where i.cliente.id = :id and i.estado = com.devsuj.crmapi.model.EstadoInteraccion.PROGRAMADA""", clienteId);
    }

    public long contratosVigentesDe(Integer clienteId) {
        return contar("""
                select count(c) from Contrato c
                where c.cliente.id = :id and c.estado = com.devsuj.crmapi.model.EstadoContrato.VIGENTE""", clienteId);
    }

    public long ticketsAbiertosDe(Integer clienteId) {
        return contar("""
                select count(t) from TicketSoporte t
                where t.cliente.id = :id and t.estado <> com.devsuj.crmapi.model.EstadoTicket.CERRADO""", clienteId);
    }

    public long campanasDe(Integer clienteId) {
        return contar("select count(cc) from CampanaCliente cc where cc.cliente.id = :id", clienteId);
    }

    private long contar(String jpql, Integer clienteId) {
        return em.createQuery(jpql, Long.class).setParameter("id", clienteId).getSingleResult();
    }

    /** Cantidad de registros y suma de importes. */
    public record Totales(long cantidad, BigDecimal monto) {

        public static final Totales CERO = new Totales(0, BigDecimal.ZERO.setScale(2));

        /** monto / cantidad; 0 si no hay registros. */
        public BigDecimal promedio() {
            return cantidad == 0
                    ? BigDecimal.ZERO.setScale(2)
                    : monto.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
        }
    }

    private static <T> TypedQuery<T> conAlcance(TypedQuery<T> query, Alcance alcance) {
        return query.setParameter("todos", alcance.todos()).setParameter("ids", alcance.idsParaConsulta());
    }

    private static long largo(Object valor) {
        return valor == null ? 0 : ((Number) valor).longValue();
    }

    private static BigDecimal monto(Object valor) {
        BigDecimal monto = switch (valor) {
            case null -> BigDecimal.ZERO;
            case BigDecimal decimal -> decimal;
            default -> new BigDecimal(valor.toString());
        };
        return monto.setScale(2, RoundingMode.HALF_UP);
    }
}
