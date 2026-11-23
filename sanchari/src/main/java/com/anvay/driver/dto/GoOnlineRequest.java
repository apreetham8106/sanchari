package com.anvay.driver.dto;

import jakarta.validation.constraints.NotNull;

public record GoOnlineRequest(@NotNull Boolean online, Double lat, Double lng) {
}
