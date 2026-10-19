package com.library.lending.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BookUpdateRequest(
        @NotBlank String title,
        @NotBlank String author,
        @Min(1) int totalCopies) {
}
