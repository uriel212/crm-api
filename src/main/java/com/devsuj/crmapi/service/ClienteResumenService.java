package com.devsuj.crmapi.service;

import com.devsuj.crmapi.dto.reporte.ClienteResumenResponse;
import com.devsuj.crmapi.mapper.ClienteMapper;
import com.devsuj.crmapi.mapper.VentaMapper;
import com.devsuj.crmapi.model.Cliente;
import com.devsuj.crmapi.repository.Filtros;
import com.devsuj.crmapi.repository.MovimientoPuntosRepository;
import com.devsuj.crmapi.repository.ReporteRepository;
import com.devsuj.crmapi.repository.ReporteRepository.Totales;
import com.devsuj.crmapi.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Vista 360 de un cliente (9.9): todo lo que el CRM sabe de él en una sola respuesta, con consultas agregadas.
 * S30: la ve quien gestiona al cliente (su vendedor, el gerente de este o ADMIN).
 */
@Service
@RequiredArgsConstructor
public class ClienteResumenService {

    private static final int ULTIMAS_VENTAS = 5;

    private final ClienteService clienteService;
    private final ReporteRepository reportes;
    private final VentaRepository ventaRepository;
    private final MovimientoPuntosRepository puntosRepository;
    private final ClienteMapper clienteMapper;
    private final VentaMapper ventaMapper;

    @Transactional(readOnly = true)
    public ClienteResumenResponse resumen(Integer clienteId) {
        Cliente cliente = clienteService.buscarGestionable(clienteId);
        Object[] compras = reportes.comprasDe(clienteId);
        Totales ventas = new Totales(((Number) compras[0]).longValue(),
                new BigDecimal(compras[1].toString()).setScale(2, RoundingMode.HALF_UP));
        Totales pipeline = reportes.oportunidadesAbiertasDe(clienteId);
        var ultimas = ventaRepository.findAll(Filtros.igual(clienteId, "cliente", "id"),
                PageRequest.of(0, ULTIMAS_VENTAS, Sort.by(Sort.Direction.DESC, "fecha", "id")));
        return new ClienteResumenResponse(clienteMapper.toResponse(cliente), ventas.cantidad(), ventas.monto(),
                ventas.promedio(), (LocalDateTime) compras[2], ultimas.map(ventaMapper::toResumen).getContent(),
                pipeline.cantidad(), pipeline.monto(), reportes.ultimaInteraccionDe(clienteId),
                reportes.actividadesProgramadasDe(clienteId), puntosRepository.totalesDe(clienteId).saldo(),
                reportes.contratosVigentesDe(clienteId), reportes.ticketsAbiertosDe(clienteId),
                reportes.campanasDe(clienteId));
    }
}
