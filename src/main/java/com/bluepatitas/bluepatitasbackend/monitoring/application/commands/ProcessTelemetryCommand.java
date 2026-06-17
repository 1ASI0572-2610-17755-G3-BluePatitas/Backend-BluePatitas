package com.bluepatitas.bluepatitasbackend.monitoring.application.commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ProcessTelemetryCommand
 * <p>
 * CQRS Write-side command that instructs the application to ingest and
 * process a new telemetry reading from an environmental sensor device.
 * </p>
 *
 * @param targetId           the identifier of the monitored target
 * @param ambientTemperature the captured temperature reading in Celsius
 * @param ambientHumidity    the captured humidity reading as a percentage
 * @param visualData         the raw or encoded visual payload from the camera
 */
public record ProcessTelemetryCommand(
        UUID targetId,
        BigDecimal ambientTemperature,
        BigDecimal ambientHumidity,
        String visualData
) {
}
