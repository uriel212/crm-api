package com.devsuj.crmapi.dto.catalogo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Datos de un producto. El stock no se edita aquí: cambia con movimientos de inventario.")
public record ProductoRequest(
        @Schema(example = "3") @NotNull(message = "La categoría es obligatoria.") Integer categoriaId,
        @Schema(example = "Teclado mecánico Keychron K2") @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 120) String nombre,
        @Schema(example = "Inalámbrico, switches marrones") @Size(max = 5000) String descripcion,
        @Schema(description = "Precio en quetzales", example = "1299.00") @NotNull(message = "El precio es obligatorio.")
        @PositiveOrZero(message = "El precio no puede ser negativo.")
        @Digits(integer = 8, fraction = 2, message = "Máximo 8 enteros y 2 decimales.") BigDecimal precio,
        @Schema(description = "Precio en puntos de fidelización; null = no canjeable", example = "1300")
        @Positive(message = "El precio en puntos debe ser mayor que 0.") Integer precioPuntos,
        @Schema(description = "Stock alcanza este número o menos → alerta de resurtir", example = "5")
        @NotNull(message = "El stock mínimo es obligatorio.") @PositiveOrZero Integer stockMinimo,
        @Schema(description = "Solo al crear: existencias iniciales (queda como ENTRADA en el kardex)", example = "20")
        @PositiveOrZero Integer stockInicial) {
}
