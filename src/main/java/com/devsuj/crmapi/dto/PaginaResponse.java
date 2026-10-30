package com.devsuj.crmapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Página de resultados con un contrato JSON estable para el frontend (en lugar de serializar {@code Page}). */
@Schema(description = "Página de resultados")
public record PaginaResponse<T>(
        List<T> contenido,
        @Schema(description = "Número de página (empieza en 0)", example = "0") int pagina,
        @Schema(description = "Elementos por página", example = "20") int tamano,
        @Schema(example = "57") long totalElementos,
        @Schema(example = "3") int totalPaginas) {

    public static <E, T> PaginaResponse<T> de(Page<E> pagina, Function<E, T> conversion) {
        return new PaginaResponse<>(pagina.getContent().stream().map(conversion).toList(),
                pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }
}
