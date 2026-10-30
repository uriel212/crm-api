package com.devsuj.crmapi.dto.marketing;

import com.devsuj.crmapi.model.AlcancePromocion;
import com.devsuj.crmapi.model.TipoPromocion;
import com.devsuj.crmapi.validation.ConRangoFechas;
import com.devsuj.crmapi.validation.RangoFechasValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@RangoFechasValido
@Schema(description = """
        Promoción. PRODUCTO: descuento por renglón para los productoIds (MONTO_FIJO = quetzales por unidad).
        VENTA: descuento sobre el total si el subtotal alcanza montoMinimo (MONTO_FIJO = quetzales una vez).""")
public record PromocionRequest(
        @Schema(description = "Campaña que la anuncia (opcional): solo aplica mientras esté ACTIVA", example = "3")
        Integer campanaId,
        @Schema(example = "Buen Fin: 10% en laptops") @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120, message = "El nombre admite máximo 120 caracteres.") String nombre,
        @Schema(example = "PORCENTAJE") @NotNull(message = "El tipo es obligatorio.") TipoPromocion tipo,
        @Schema(example = "PRODUCTO") @NotNull(message = "El alcance es obligatorio.") AlcancePromocion alcance,
        @Schema(example = "10.00") @NotNull(message = "El valor es obligatorio.")
        @Positive(message = "El valor debe ser mayor que cero.")
        @Digits(integer = 8, fraction = 2, message = "Máximo 8 enteros y 2 decimales.") BigDecimal valor,
        @Schema(description = "Solo para alcance VENTA (null = sin mínimo)", example = "10000.00")
        @PositiveOrZero(message = "El monto mínimo no puede ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "Máximo 10 enteros y 2 decimales.") BigDecimal montoMinimo,
        @Schema(example = "2026-11-13") @NotNull(message = "La fecha de inicio es obligatoria.") LocalDate fechaInicio,
        @Schema(example = "2026-11-16") @NotNull(message = "La fecha de fin es obligatoria.") LocalDate fechaFin,
        @Schema(description = "Obligatorio para alcance PRODUCTO; vacío para VENTA", example = "[1, 2]")
        @Size(max = 200, message = "Máximo 200 productos.") Set<Integer> productoIds)
        implements ConRangoFechas {
}
