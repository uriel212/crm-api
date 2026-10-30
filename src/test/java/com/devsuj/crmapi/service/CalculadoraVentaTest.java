package com.devsuj.crmapi.service;

import com.devsuj.crmapi.model.AlcancePromocion;
import com.devsuj.crmapi.model.Producto;
import com.devsuj.crmapi.model.Promocion;
import com.devsuj.crmapi.model.TipoPromocion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraVentaTest {

    private final CalculadoraVenta calculadora =
            new CalculadoraVenta(List.of(new DescuentoPorcentaje(), new DescuentoMontoFijo()));

    private final Producto licencia = producto(12, "2899.00");
    private final Producto mouse = producto(7, "1899.00");

    @Test
    void sinPromocionesElTotalEsCantidadPorPrecio() {
        CalculoVenta calculo = calculadora.calcular(List.of(pedido(mouse, 3)), List.of());

        assertThat(calculo.subtotal()).isEqualByComparingTo("5697.00");
        assertThat(calculo.descuento()).isEqualByComparingTo("0");
        assertThat(calculo.total()).isEqualByComparingTo("5697.00");
        assertThat(calculo.promocion()).isNull();
        assertThat(calculo.renglones().getFirst().promocion()).isNull();
    }

    @Test
    void montoFijoDeProductoEsPorUnidadYLaDelTotalSeCalculaSobreElSubtotalYaDescontado() {
        Promocion porLicencia = promocion(3, TipoPromocion.MONTO_FIJO, AlcancePromocion.PRODUCTO, "300", null, licencia);
        Promocion sobreTotal = promocion(4, TipoPromocion.MONTO_FIJO, AlcancePromocion.VENTA, "500", "10000");

        CalculoVenta calculo = calculadora.calcular(List.of(pedido(licencia, 5)), List.of(porLicencia, sobreTotal));

        CalculoVenta.Renglon renglon = calculo.renglones().getFirst();
        assertThat(renglon.descuento()).isEqualByComparingTo("1500.00");
        assertThat(renglon.subtotal()).isEqualByComparingTo("12995.00");
        assertThat(calculo.subtotal()).isEqualByComparingTo("12995.00");
        assertThat(calculo.promocion()).isSameAs(sobreTotal);
        assertThat(calculo.total()).isEqualByComparingTo("12495.00");
    }

    @Test
    void elMontoMinimoSeAlcanzaConMayorOIgual() {
        Promocion sobreTotal = promocion(4, TipoPromocion.MONTO_FIJO, AlcancePromocion.VENTA, "500", "5697.00");

        assertThat(calculadora.calcular(List.of(pedido(mouse, 3)), List.of(sobreTotal)).promocion()).isSameAs(sobreTotal);
        assertThat(calculadora.calcular(List.of(pedido(mouse, 2)), List.of(sobreTotal)).promocion()).isNull();
    }

    @Test
    void ganaElMayorDescuentoYEnEmpateLaDeIdMenor() {
        Promocion diezPorCiento = promocion(8, TipoPromocion.PORCENTAJE, AlcancePromocion.PRODUCTO, "10", null, mouse);
        Promocion doscientos = promocion(9, TipoPromocion.MONTO_FIJO, AlcancePromocion.PRODUCTO, "200", null, mouse);
        Promocion empate = promocion(5, TipoPromocion.MONTO_FIJO, AlcancePromocion.PRODUCTO, "200", null, mouse);

        CalculoVenta calculo = calculadora.calcular(List.of(pedido(mouse, 1)), List.of(diezPorCiento, doscientos, empate));

        assertThat(calculo.renglones().getFirst().promocion()).isSameAs(empate);
        assertThat(calculo.renglones().getFirst().descuento()).isEqualByComparingTo("200.00");
    }

    @Test
    void elPorcentajeSeRedondeaACentavosYElDescuentoNuncaSuperaElImporte() {
        Promocion quince = promocion(1, TipoPromocion.PORCENTAJE, AlcancePromocion.PRODUCTO, "15", null, mouse);
        assertThat(calculadora.calcular(List.of(pedido(mouse, 1)), List.of(quince)).renglones().getFirst().descuento())
                .isEqualByComparingTo("284.85");

        Promocion enorme = promocion(2, TipoPromocion.MONTO_FIJO, AlcancePromocion.PRODUCTO, "5000", null, mouse);
        CalculoVenta calculo = calculadora.calcular(List.of(pedido(mouse, 2)), List.of(enorme));
        assertThat(calculo.renglones().getFirst().subtotal()).isEqualByComparingTo("0");
        assertThat(calculo.total()).isEqualByComparingTo("0");
    }

    @Test
    void unaPromocionDeProductoNoAplicaAOtrosProductos() {
        Promocion porLicencia = promocion(3, TipoPromocion.MONTO_FIJO, AlcancePromocion.PRODUCTO, "300", null, licencia);

        CalculoVenta calculo = calculadora.calcular(List.of(pedido(mouse, 1)), List.of(porLicencia));

        assertThat(calculo.renglones().getFirst().promocion()).isNull();
        assertThat(calculo.total()).isEqualByComparingTo("1899.00");
    }

    private static CalculoVenta.Pedido pedido(Producto producto, int cantidad) {
        return new CalculoVenta.Pedido(producto, cantidad);
    }

    private static Producto producto(int id, String precio) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre("Producto " + id);
        producto.setPrecio(new BigDecimal(precio));
        return producto;
    }

    private static Promocion promocion(int id, TipoPromocion tipo, AlcancePromocion alcance, String valor,
            String montoMinimo, Producto... productos) {
        Promocion promocion = new Promocion();
        promocion.setId(id);
        promocion.setNombre("Promoción " + id);
        promocion.setTipo(tipo);
        promocion.setAlcance(alcance);
        promocion.setValor(new BigDecimal(valor));
        promocion.setMontoMinimo(montoMinimo == null ? null : new BigDecimal(montoMinimo));
        promocion.getProductos().addAll(List.of(productos));
        return promocion;
    }
}
