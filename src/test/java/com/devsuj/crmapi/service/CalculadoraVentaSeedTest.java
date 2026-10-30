package com.devsuj.crmapi.service;

import com.devsuj.crmapi.model.AlcancePromocion;
import com.devsuj.crmapi.model.Producto;
import com.devsuj.crmapi.model.Promocion;
import com.devsuj.crmapi.model.TipoPromocion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** La calculadora reproduce los importes que el seed calculó con SQL (ventas 18 y 24). */
class CalculadoraVentaSeedTest {

    private final CalculadoraVenta calculadora =
            new CalculadoraVenta(List.of(new DescuentoPorcentaje(), new DescuentoMontoFijo()));

    @Test
    void venta18ConQuincePorCientoEnAccesorios() {
        Producto teclado = producto(6, "2199.00");
        Producto mouse = producto(7, "1899.00");
        Promocion regresoAClases = promocion(1, TipoPromocion.PORCENTAJE, "15", teclado, mouse);

        CalculoVenta calculo = calculadora.calcular(
                List.of(new CalculoVenta.Pedido(teclado, 10), new CalculoVenta.Pedido(mouse, 10)), List.of(regresoAClases));

        assertThat(calculo.renglones().get(0).subtotal()).isEqualByComparingTo("18691.50");
        assertThat(calculo.renglones().get(1).subtotal()).isEqualByComparingTo("16141.50");
        assertThat(calculo.total()).isEqualByComparingTo("34833.00");
    }

    @Test
    void venta24ConQ300PorLicencia() {
        Producto eset = producto(13, "899.00");
        Producto microsoft = producto(12, "2899.00");
        Promocion licencias = promocion(3, TipoPromocion.MONTO_FIJO, "300", microsoft);

        CalculoVenta calculo = calculadora.calcular(
                List.of(new CalculoVenta.Pedido(eset, 20), new CalculoVenta.Pedido(microsoft, 5)), List.of(licencias));

        assertThat(calculo.renglones().get(1).descuento()).isEqualByComparingTo("1500.00");
        assertThat(calculo.total()).isEqualByComparingTo("30975.00");
    }

    private static Producto producto(int id, String precio) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre("Producto " + id);
        producto.setPrecio(new BigDecimal(precio));
        return producto;
    }

    private static Promocion promocion(int id, TipoPromocion tipo, String valor, Producto... productos) {
        Promocion promocion = new Promocion();
        promocion.setId(id);
        promocion.setNombre("Promoción " + id);
        promocion.setTipo(tipo);
        promocion.setAlcance(AlcancePromocion.PRODUCTO);
        promocion.setValor(new BigDecimal(valor));
        promocion.getProductos().addAll(List.of(productos));
        return promocion;
    }
}
