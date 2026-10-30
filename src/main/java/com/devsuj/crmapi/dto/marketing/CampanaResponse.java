package com.devsuj.crmapi.dto.marketing;

import com.devsuj.crmapi.model.CanalCampana;
import com.devsuj.crmapi.model.EstadoCampana;
import com.devsuj.crmapi.model.SegmentoCampana;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Campaña de marketing")
public record CampanaResponse(
        @Schema(example = "2") Integer id,
        @Schema(example = "Regreso a clases 2026") String nombre,
        @Schema(example = "CORREO") CanalCampana canal,
        @Schema(example = "15% de descuento en accesorios") String asunto,
        String mensaje,
        @Schema(example = "TODOS") SegmentoCampana segmento,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        @Schema(example = "5000.00") BigDecimal presupuesto,
        @Schema(example = "FINALIZADA") EstadoCampana estado,
        @Schema(example = "9") long totalDestinatarios) {
}
