package com.acervodigital.collection.copy;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateCollectionCopyRequest(
        @NotNull UUID catalogItemId,
        @NotNull CopyCondition condition,
        UUID locationId,
        @NotNull LocalDate acquiredOn,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal purchasePrice,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal currentValue,
        @Size(max = 2000) String notes
) {
}