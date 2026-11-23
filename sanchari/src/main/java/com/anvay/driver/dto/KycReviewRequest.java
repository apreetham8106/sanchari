package com.anvay.driver.dto;

import jakarta.validation.constraints.NotNull;

public record KycReviewRequest(@NotNull Boolean approve, String remarks) {
}
