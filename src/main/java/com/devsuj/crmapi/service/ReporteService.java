package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.reporte.ConversionResponse;
import com.devsuj.crmapi.dto.reporte.DashboardResponse;
import com.devsuj.crmapi.dto.reporte.FidelizacionReporteResponse;
import com.devsuj.crmapi.dto.reporte.SoporteResponse;
import com.devsuj.crmapi.dto.marketing.MetricasCampanaResponse;
import com.devsuj.crmapi.dto.reporte.PipelineEtapaResponse;
import com.devsuj.crmapi.dto.reporte.ProductoVendidoResponse;
import com.devsuj.crmapi.dto.reporte.RendimientoVendedorResponse;
import com.devsuj.crmapi.dto.reporte.VentasPorCategoriaResponse;
import com.devsuj.crmapi.dto.reporte.VentasPorMesResponse;
import com.devsuj.crmapi.dto.reporte.VentasPorVendedorResponse;
import com.devsuj.crmapi.mapper.Porcentajes;
import com.devsuj.crmapi.model.EstadoCliente;
import com.devsuj.crmapi.model.EstadoTicket;
import com.devsuj.crmapi.model.EtapaOportunidad;
import com.devsuj.crmapi.model.PrioridadTicket;
import com.devsuj.crmapi.model.MetaVenta;
import com.devsuj.crmapi.model.Rol;
import com.devsuj.crmapi.model.Usuario;
import com.devsuj.crmapi.repository.MetaVentaRepository;
import com.devsuj.crmapi.repository.ReporteRepository;
import com.devsuj.crmapi.repository.ReporteRepository.Totales;
import com.devsuj.crmapi.repository.UsuarioRepository;
import com.devsuj.crmapi.repository.VentaRepository;
import com.devsuj.crmapi.repository.VentasDelPeriodo;
import com.devsuj.crmapi.security.Alcance;
import com.devsuj.crmapi.security.ControlAcceso;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Reportes y KPIs de solo lectura, siempre filtrados por el alcance de quien consulta (VENDEDOR lo suyo,
 * GERENTE su equipo, ADMIN todo). Ventas y oportunidades cuentan por su responsable; clientes por su vendedor.
 * Periodos por defecto: el tablero usa el mes en curso; los análisis, del 1 de enero del año en curso a hoy.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private static final Set<EtapaOportunidad> CERRADAS = Set.of(EtapaOportunidad.GANADA, EtapaOportunidad.PERDIDA);

    private final ReporteRepository reportes;
    private final VentaRepository ventaRepository;
    private final MetaVentaRepository metaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CampanaService campanaService;
    private final ControlAcceso controlAcceso;
    private final Clock clock;

    public DashboardResponse dashboard(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = hoy();
        Periodo periodo = Periodo.mesEnCurso(desde, hasta, hoy);
        Alcance alcance = controlAcceso.alcance();
        Totales ventas = reportes.totalesVentas(alcance, periodo.inicio(), periodo.fin());

        YearMonth mes = YearMonth.from(hoy);
        BigDecimal metaMes = reportes.sumaMetas(alcance, mes.getYear(), mes.getMonthValue());
        BigDecimal vendidoMes = reportes.totalesVentas(alcance, inicioDe(mes), inicioDe(mes.plusMonths(1))).monto();

        Map<EtapaOportunidad, Totales> pipeline = reportes.pipeline(alcance);
        Totales abiertas = abiertas(pipeline);
        Map<EtapaOportunidad, Long> cierres = sumarCierres(reportes.cierresPorUsuario(alcance, periodo.inicio(), periodo.fin()));
        long ganadas = cierres.getOrDefault(EtapaOportunidad.GANADA, 0L);
        long perdidas = cierres.getOrDefault(EtapaOportunidad.PERDIDA, 0L);

        return new DashboardResponse(periodo.desde(), periodo.hasta(), ventas.monto(), ventas.cantidad(),
                ventas.promedio(), metaMes, vendidoMes, Porcentajes.de(vendidoMes, metaMes),
                reportes.clientesPorEstado(alcance, EstadoCliente.ACTIVO),
                reportes.clientesPorEstado(alcance, EstadoCliente.PROSPECTO),
                reportes.clientesNuevos(alcance, periodo.inicio(), periodo.fin()),
                abiertas.cantidad(), abiertas.monto(), ganadas, perdidas, Porcentajes.de(ganadas, ganadas + perdidas),
                reportes.ticketsAbiertos(alcance), reportes.productosConStockBajo());
    }

    /** Los 12 meses del año, con 0 en los meses sin ventas (listo para graficar). */
    public List<VentasPorMesResponse> ventasPorMes(Integer anio) {
        int anioConsulta = anio != null ? anio : hoy().getYear();
        Map<Integer, Totales> porMes = reportes.ventasPorMes(controlAcceso.alcance(),
                LocalDate.of(anioConsulta, 1, 1).atStartOfDay(), LocalDate.of(anioConsulta + 1, 1, 1).atStartOfDay());
        return IntStream.rangeClosed(1, 12)
                .mapToObj(m -> {
                    Totales t = porMes.getOrDefault(m, Totales.CERO);
                    return new VentasPorMesResponse(anioConsulta, m, t.cantidad(), t.monto());
                })
                .toList();
    }

    public List<VentasPorVendedorResponse> ventasPorVendedor(LocalDate desde, LocalDate hasta) {
        Periodo periodo = anioEnCurso(desde, hasta);
        return reportes.ventasPorVendedor(controlAcceso.alcance(), periodo.inicio(), periodo.fin());
    }

    public List<VentasPorCategoriaResponse> ventasPorCategoria(LocalDate desde, LocalDate hasta) {
        Periodo periodo = anioEnCurso(desde, hasta);
        return reportes.ventasPorCategoria(controlAcceso.alcance(), periodo.inicio(), periodo.fin());
    }

    public List<ProductoVendidoResponse> productosMasVendidos(LocalDate desde, LocalDate hasta, int limite) {
        Periodo periodo = anioEnCurso(desde, hasta);
        return reportes.productosMasVendidos(controlAcceso.alcance(), periodo.inicio(), periodo.fin(), limite);
    }

    /** Las 6 etapas en orden del pipeline, con 0 en las vacías. */
    public List<PipelineEtapaResponse> pipeline() {
        Map<EtapaOportunidad, Totales> porEtapa = reportes.pipeline(controlAcceso.alcance());
        return Arrays.stream(EtapaOportunidad.values())
                .map(e -> {
                    Totales t = porEtapa.getOrDefault(e, Totales.CERO);
                    return new PipelineEtapaResponse(e, t.cantidad(), t.monto());
                })
                .toList();
    }

    public ConversionResponse conversion(LocalDate desde, LocalDate hasta) {
        Periodo periodo = anioEnCurso(desde, hasta);
        Alcance alcance = controlAcceso.alcance();
        Map<EtapaOportunidad, Long> cierres = sumarCierres(reportes.cierresPorUsuario(alcance, periodo.inicio(), periodo.fin()));
        long ganadas = cierres.getOrDefault(EtapaOportunidad.GANADA, 0L);
        long perdidas = cierres.getOrDefault(EtapaOportunidad.PERDIDA, 0L);
        return new ConversionResponse(periodo.desde(), periodo.hasta(), ganadas, perdidas,
                Porcentajes.de(ganadas, ganadas + perdidas), abiertas(reportes.pipeline(alcance)).cantidad());
    }

    /**
     * Un renglón por VENDEDOR del alcance (activos, o inactivos que vendieron ese mes), ordenado por lo vendido.
     * Mes por defecto: el mes en curso.
     */
    public List<RendimientoVendedorResponse> rendimientoVendedores(Integer anio, Integer mes) {
        YearMonth periodo = YearMonth.of(anio != null ? anio : hoy().getYear(), mes != null ? mes : hoy().getMonthValue());
        Alcance alcance = controlAcceso.alcance();
        List<Usuario> vendedores = usuarioRepository.findByRolOrderByNombre(Rol.VENDEDOR).stream()
                .filter(u -> alcance.incluye(u.getId()))
                .toList();
        if (vendedores.isEmpty()) {
            return List.of();
        }
        Set<Integer> ids = vendedores.stream().map(Usuario::getId).collect(Collectors.toSet());
        Map<Integer, VentasDelPeriodo> ventas = ventaRepository
                .sumarPorVendedorYMes(ids, inicioDe(periodo), inicioDe(periodo.plusMonths(1))).stream()
                .collect(Collectors.toMap(VentasDelPeriodo::usuarioId, Function.identity()));
        Map<Integer, BigDecimal> metas = metaRepository.findByAnioAndMes(periodo.getYear(), periodo.getMonthValue())
                .stream()
                .collect(Collectors.toMap(m -> m.getUsuario().getId(), MetaVenta::getMontoObjetivo));
        Map<Integer, Map<EtapaOportunidad, Long>> cierres = reportes.cierresPorUsuario(alcance, inicioDe(periodo),
                inicioDe(periodo.plusMonths(1)));

        return vendedores.stream()
                .filter(u -> u.isActivo() || ventas.containsKey(u.getId()))
                .map(u -> rendimiento(u, ventas.get(u.getId()), metas.get(u.getId()),
                        cierres.getOrDefault(u.getId(), Map.of())))
                .sorted((a, b) -> b.totalVendido().compareTo(a.totalVendido()))
                .toList();
    }

    /** Efectividad de todas las campañas (las campañas no tienen dueño: no se filtran por alcance). */
    public List<MetricasCampanaResponse> efectividadCampanas() {
        return campanaService.metricasDeTodas();
    }

    public SoporteResponse soporte() {
        Alcance alcance = controlAcceso.alcance();
        Map<EstadoTicket, Long> porEstado = reportes.ticketsPorEstado(alcance);
        Map<PrioridadTicket, Long> pendientes = reportes.pendientesPorPrioridad(alcance);
        return new SoporteResponse(porEstado.getOrDefault(EstadoTicket.ABIERTO, 0L),
                porEstado.getOrDefault(EstadoTicket.EN_PROCESO, 0L), porEstado.getOrDefault(EstadoTicket.CERRADO, 0L),
                reportes.ticketsSinAsignar(alcance), reportes.horasPromedioCierre(alcance),
                Arrays.stream(PrioridadTicket.values())
                        .map(p -> new SoporteResponse.PorPrioridad(p, pendientes.getOrDefault(p, 0L)))
                        .toList());
    }

    public FidelizacionReporteResponse fidelizacion() {
        List<Object[]> filas = reportes.puntosPorCliente(controlAcceso.alcance());
        long emitidos = filas.stream().mapToLong(f -> ((Number) f[2]).longValue()).sum();
        long canjeados = filas.stream().mapToLong(f -> ((Number) f[3]).longValue()).sum();
        List<FidelizacionReporteResponse.ClienteConSaldo> conSaldo = filas.stream()
                .map(f -> new FidelizacionReporteResponse.ClienteConSaldo((Integer) f[0], (String) f[1],
                        ((Number) f[2]).longValue() - ((Number) f[3]).longValue()))
                .filter(c -> c.saldo() > 0)
                .sorted(Comparator.comparingLong(FidelizacionReporteResponse.ClienteConSaldo::saldo).reversed()
                        .thenComparing(FidelizacionReporteResponse.ClienteConSaldo::clienteId))
                .toList();
        return new FidelizacionReporteResponse(emitidos, canjeados, emitidos - canjeados, conSaldo.size(),
                conSaldo.stream().limit(5).toList());
    }

    private static RendimientoVendedorResponse rendimiento(Usuario vendedor, VentasDelPeriodo ventas, BigDecimal meta,
            Map<EtapaOportunidad, Long> cierres) {
        BigDecimal vendido = ventas == null ? Totales.CERO.monto() : ventas.total();
        long numero = ventas == null ? 0 : ventas.numeroVentas();
        long ganadas = cierres.getOrDefault(EtapaOportunidad.GANADA, 0L);
        long perdidas = cierres.getOrDefault(EtapaOportunidad.PERDIDA, 0L);
        return new RendimientoVendedorResponse(vendedor.getId(), vendedor.getNombre(), numero, vendido, meta,
                meta == null ? null : Porcentajes.de(vendido, meta), ganadas, perdidas,
                Porcentajes.de(ganadas, ganadas + perdidas));
    }

    private static Totales abiertas(Map<EtapaOportunidad, Totales> pipeline) {
        return pipeline.entrySet().stream()
                .filter(e -> !CERRADAS.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .reduce(Totales.CERO, (a, b) -> new Totales(a.cantidad() + b.cantidad(), a.monto().add(b.monto())));
    }

    private static Map<EtapaOportunidad, Long> sumarCierres(Map<Integer, Map<EtapaOportunidad, Long>> porUsuario) {
        return porUsuario.values().stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));
    }

    private Periodo anioEnCurso(LocalDate desde, LocalDate hasta) {
        LocalDate hoy = hoy();
        return Periodo.de(desde, hasta, hoy.withDayOfYear(1), hoy);
    }

    private static LocalDateTime inicioDe(YearMonth mes) {
        return mes.atDay(1).atStartOfDay();
    }

    private LocalDate hoy() {
        return LocalDate.now(clock);
    }
}
