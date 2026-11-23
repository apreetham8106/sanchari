package com.sanchari.driver.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** dateOfBirth is only forwarded to Digio for the licence check; it is not stored. */
public record StartKycRequest(@NotBlank @Size(max = 30) String licenseNumber,
                              @NotNull @Future LocalDate licenseExpiry,
                              @NotNull @Past LocalDate dateOfBirth) {
}
