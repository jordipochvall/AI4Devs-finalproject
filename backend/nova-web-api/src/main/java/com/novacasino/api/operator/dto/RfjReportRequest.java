package com.novacasino.api.operator.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request to generate the RFJ regulatory report for a calendar month (HU-21). */
public record RfjReportRequest(
        @NotNull @Min(2000) @Max(9999) Integer year,
        @NotNull @Min(1) @Max(12) Integer month) {
}
