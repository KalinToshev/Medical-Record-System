package nbu.informatics.medicalRecordSystem.model.dto.report;

import java.math.BigDecimal;

public record NameAmountProjection(
        String name,
        BigDecimal amount
) {
}
