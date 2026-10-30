package com.devsuj.crmapi.service;

import com.devsuj.crmapi.model.AlcancePromocion;
import com.devsuj.crmapi.model.Producto;
import com.devsuj.crmapi.model.Promocion;
import com.devsuj.crmapi.model.TipoPromocion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Calcula los importes de una venta (SRP, sin base de datos: se prueba con JUnit puro).
 * <ol>
 *   <li>Por renglón: precio = precio actual del producto (queda congelado en la venta); se aplica la mejor
 *       promoción de alcance PRODUCTO que incluya al producto; subtotal = cantidad × precio − descuento.</li>
 *   <li>Sobre el total: subtotal = suma de renglones ya descontados (S13); se aplica la mejor promoción de
 *       alcance VENTA cuyo monto mínimo se alcance con subtotal ≥ monto_minimo (S14); total = subtotal − descuento.</li>
 * </ol>
 * "Mejor" = mayor descuento; en empate, la de id menor (P17). Montos en {@link BigDecimal}, escala 2, HALF_UP.
 */
@Component
public class CalculadoraVenta {

    private static final Comparator<Map.Entry<Promocion, BigDecimal>> MEJOR = Map.Entry
            .<Promocion, BigDecimal>comparingByValue()
            .thenComparing(e -> -e.getKey().getId());

    private final Map<TipoPromocion, EstrategiaDescuento> estrategias = new EnumMap<>(TipoPromocion.class);

    public CalculadoraVenta(List<EstrategiaDescuento> estrategias) {
        estrategias.forEach(e -> this.estrategias.put(e.tipo(), e));
    }

    /** @param vigentes promociones vigentes hoy (de ambos alcances) */
    public CalculoVenta calcular(List<CalculoVenta.Pedido> pedidos, List<Promocion> vigentes) {
        List<Promocion> deProducto = filtrar(vigentes, AlcancePromocion.PRODUCTO);
        List<CalculoVenta.Renglon> renglones = pedidos.stream()
                .map(p -> calcularRenglon(p.producto(), p.cantidad(), deProducto))
                .toList();
        BigDecimal subtotal = renglones.stream().map(CalculoVenta.Renglon::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

        Optional<Map.Entry<Promocion, BigDecimal>> sobreTotal = mejor(
                filtrar(vigentes, AlcancePromocion.VENTA).stream().filter(p -> alcanzaMinimo(p, subtotal)).toList(),
                p -> descuento(p, subtotal, 1));
        Promocion promocion = sobreTotal.map(Map.Entry::getKey).orElse(null);
        BigDecimal descuento = sobreTotal.map(Map.Entry::getValue).orElse(cero());
        return new CalculoVenta(renglones, subtotal, descuento, promocion, subtotal.subtract(descuento));
    }

    private CalculoVenta.Renglon calcularRenglon(Producto producto, int cantidad, List<Promocion> deProducto) {
        BigDecimal precio = producto.getPrecio().setScale(2, RoundingMode.HALF_UP);
        BigDecimal importe = precio.multiply(BigDecimal.valueOf(cantidad));
        Optional<Map.Entry<Promocion, BigDecimal>> aplicada = mejor(
                deProducto.stream().filter(p -> incluye(p, producto)).toList(),
                p -> descuento(p, importe, cantidad));
        Promocion promocion = aplicada.map(Map.Entry::getKey).orElse(null);
        BigDecimal descuento = aplicada.map(Map.Entry::getValue).orElse(cero());
        return new CalculoVenta.Renglon(producto, cantidad, precio, promocion, descuento, importe.subtract(descuento));
    }

    private BigDecimal descuento(Promocion promocion, BigDecimal importe, int unidades) {
        EstrategiaDescuento estrategia = estrategias.get(promocion.getTipo());
        if (estrategia == null) {
            throw new IllegalStateException("No hay estrategia de descuento para " + promocion.getTipo());
        }
        return estrategia.calcular(importe, unidades, promocion.getValor());
    }

    /** Solo cuentan las que descuentan algo. */
    private static Optional<Map.Entry<Promocion, BigDecimal>> mejor(List<Promocion> candidatas,
            Function<Promocion, BigDecimal> calculo) {
        return candidatas.stream()
                .map(p -> Map.entry(p, calculo.apply(p)))
                .filter(e -> e.getValue().signum() > 0)
                .max(MEJOR);
    }

    private static List<Promocion> filtrar(List<Promocion> promociones, AlcancePromocion alcance) {
        return promociones.stream().filter(p -> p.getAlcance() == alcance).toList();
    }

    private static boolean incluye(Promocion promocion, Producto producto) {
        return promocion.getProductos().stream().anyMatch(p -> p.getId().equals(producto.getId()));
    }

    private static boolean alcanzaMinimo(Promocion promocion, BigDecimal subtotal) {
        return promocion.getMontoMinimo() == null || subtotal.compareTo(promocion.getMontoMinimo()) >= 0;
    }

    private static BigDecimal cero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
    }
}
