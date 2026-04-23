package com.cs489.project.adsdentalapp.dto.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleAppointmentRequest {

    @NotNull(message = "New appointment date is required")
    private LocalDate newAppointmentDate;

    @NotNull(message = "New appointment time is required")
    private LocalTime newAppointmentTime;

    private String reason;
}
