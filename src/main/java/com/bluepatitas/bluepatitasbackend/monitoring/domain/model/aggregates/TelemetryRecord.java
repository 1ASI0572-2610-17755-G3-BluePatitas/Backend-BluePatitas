package com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.valueobjects.EnvironmentalMetrics;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TelemetryRecord
 * <p>
 * Aggregate Root that represents a single telemetry reading captured by
 * environmental sensors and visual monitoring devices associated to a target
 * (e.g., a pet or zone). Enforces invariants related to temperature,
 * humidity, and visual anomaly detection.
 * </p>
 */
@Entity
@Table(name = "monitoring_telemetry_records")
@Getter
@NoArgsConstructor
public class TelemetryRecord {

    /** Unique identifier for this telemetry record. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** The identifier of the monitored target (e.g., pet ID or zone ID). */
    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    /** Captured ambient temperature in degrees Celsius. */
    @Column(name = "ambient_temperature", precision = 5, scale = 2)
    private BigDecimal ambientTemperature;

    /** Captured ambient humidity percentage. */
    @Column(name = "ambient_humidity", precision = 5, scale = 2)
    private BigDecimal ambientHumidity;

    /** Raw or encoded visual data captured by the monitoring camera. */
    @Column(name = "visual_data", columnDefinition = "TEXT")
    private String visualData;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    /** Timestamp when this telemetry record was captured. */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    /**
     * Full constructor for creating a TelemetryRecord aggregate.
     *
     * @param id                  unique identifier
     * @param targetId            the monitored target's identifier
     * @param ambientTemperature  current temperature reading
     * @param ambientHumidity     current humidity reading
     * @param visualData          raw visual data payload
     * @param latitude            current GPS latitude
     * @param longitude           current GPS longitude
     * @param recordedAt          timestamp of the reading
     */
    public TelemetryRecord(UUID id, UUID targetId, BigDecimal ambientTemperature,
                           BigDecimal ambientHumidity, String visualData,
                           BigDecimal latitude, BigDecimal longitude,
                           LocalDateTime recordedAt) {
        this.id = id;
        this.targetId = targetId;
        this.ambientTemperature = ambientTemperature;
        this.ambientHumidity = ambientHumidity;
        this.visualData = visualData;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordedAt = recordedAt;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Domain Behaviour
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validates whether the current environmental readings are within the
     * acceptable thresholds defined by the provided {@link EnvironmentalMetrics}.
     *
     * @param metrics the threshold configuration to validate against
     * @return true if both temperature and humidity are within bounds
     * @throws IllegalArgumentException if metrics is null
     */
    public boolean validateEnvironmentalThresholds(EnvironmentalMetrics metrics) {
        if (metrics == null) {
            throw new IllegalArgumentException("EnvironmentalMetrics must not be null");
        }
        boolean tempOk = metrics.isTemperatureWithinBounds(this.ambientTemperature);
        boolean humidityOk = metrics.isHumidityWithinBounds(this.ambientHumidity);
        return tempOk && humidityOk;
    }

    /**
     * Determines whether the captured visual data contains potential anomalies.
     * An anomaly is detected when the visual data payload is non-null and
     * contains an "ANOMALY" flag marker (produced by the camera processing pipeline).
     *
     * @return true if the visual data indicates an anomaly, false otherwise
     */
    public boolean hasVisualAnomalies() {
        return this.visualData != null && this.visualData.toUpperCase().contains("ANOMALY");
    }
}
