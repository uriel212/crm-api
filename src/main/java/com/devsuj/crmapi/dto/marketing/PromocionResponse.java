package com.devsuj.crmapi.dto.marketing;

import com.devsuj.crmapi.model.AlcancePromocion;
import com.devsuj.crmapi.model.TipoPromocion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Promoción con sus productos")
public record PromocionResponse(
        @Schema(example = "3") Integer id,
        @Schema(example = "2") Integer campanaId,
        @Schema(example = "Buen Fin 2026") String campanaNombre,
        @Schema(example = "Q300 menos por licencia de Microsoft 365") String nombre,
        @Schema(example = "MONTO_FIJO") TipoPromocion tipo,
        @Schema(example = "PRODUCTO") AlcancePromocion alcance,
        @Schema(example = "300.00") BigDecimal valor,
        @Schema(example = "10000.00") BigDecimal montoMinimo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        @Schema(example = "true") boolean activa,
        @Schema(description = "Activa, dentro de sus fechas hoy y con su campaña ACTIVA (si tiene)", example = "true")
        boolean vigente,
        List<ProductoDePromocion> productos) {

    public record ProductoDePromocion(@Schema(example = "12") Integer id,
            @Schema(example = "Microsoft 365 Empresa Estándar") String nombre) {
    }
}
