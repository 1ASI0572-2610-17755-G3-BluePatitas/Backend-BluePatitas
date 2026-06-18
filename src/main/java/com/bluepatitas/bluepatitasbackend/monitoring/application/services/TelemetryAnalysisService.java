package com.bluepatitas.bluepatitasbackend.monitoring.application.services;

import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.ProcessTelemetryCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.application.queries.AnalyzeVisualDataQuery;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.TelemetryRecord;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.MonitoringZone;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.MonitoringZoneRepository;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.TelemetryRepository;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.valueobjects.EnvironmentalMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * TelemetryAnalysisService
 * <p>
 * Application service responsible for processing incoming telemetry data
 * from environmental sensors and managing continuous camera feed processing.
 * Coordinates domain logic for threshold validation and visual anomaly detection.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryAnalysisService {

    private final TelemetryRepository telemetryRepository;
    private final MonitoringZoneRepository monitoringZoneRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Command Handlers (Write Side)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Processes an incoming telemetry reading by persisting it and evaluating
     * whether environmental thresholds are being violated.
     *
     * @param command the CQRS command carrying telemetry payload
     * @return the persisted TelemetryRecord
     */
    @Transactional
    public TelemetryRecord processTelemetry(ProcessTelemetryCommand command) {
        log.info("Processing telemetry for targetId={}", command.targetId());

        TelemetryRecord record = new TelemetryRecord(
                UUID.randomUUID(),
                command.targetId(),
                command.ambientTemperature(),
                command.ambientHumidity(),
                command.visualData(),
                command.latitude(),
                command.longitude(),
                LocalDateTime.now()
        );

        // Validate environmental thresholds using default metrics
        EnvironmentalMetrics defaultMetrics = EnvironmentalMetrics.defaults();
        boolean thresholdsOk = record.validateEnvironmentalThresholds(defaultMetrics);

        if (!thresholdsOk) {
            log.warn("ALERT: Environmental thresholds exceeded for targetId={} — temp={}, humidity={}",
                    command.targetId(), command.ambientTemperature(), command.ambientHumidity());
        }

        // Evaluate visual anomalies
        if (record.hasVisualAnomalies()) {
            log.warn("ALERT: Visual anomaly detected for targetId={}", command.targetId());
        }

        // Update MonitoringZone real-time telemetry and status
        if (command.targetId() != null) {
            monitoringZoneRepository.findByTargetId(command.targetId()).ifPresent(zone -> {
                if (command.ambientTemperature() != null) {
                    zone.setTemperatureC(command.ambientTemperature().doubleValue());
                }
                if (command.ambientHumidity() != null) {
                    zone.setHumidity(command.ambientHumidity().doubleValue());
                }

                // Check limits
                boolean tempOk = true;
                boolean humidityOk = true;

                if (command.ambientTemperature() != null) {
                    double temp = command.ambientTemperature().doubleValue();
                    if (zone.getMinTemperatureC() != null && temp < zone.getMinTemperatureC()) {
                        tempOk = false;
                    }
                    if (zone.getMaxTemperatureC() != null && temp > zone.getMaxTemperatureC()) {
                        tempOk = false;
                    }
                }
                if (command.ambientHumidity() != null) {
                    double humidity = command.ambientHumidity().doubleValue();
                    if (humidity >= 70.0) {
                        humidityOk = false;
                    }
                }

                boolean statusOk = tempOk && humidityOk && !record.hasVisualAnomalies();

                if (!statusOk) {
                    zone.setStatus("Warning");
                } else {
                    zone.setStatus("Active");
                }
                monitoringZoneRepository.save(zone);
                log.info("Updated MonitoringZone name='{}' targetId={} with temp={} hum={} status={}",
                        zone.getName(), zone.getTargetId(), zone.getTemperatureC(), zone.getHumidity(), zone.getStatus());
            });
        }

        return telemetryRepository.save(record);
    }

    /**
     * Manages continuous camera stream processing for a given target.
     * Intended to be called by a scheduled task or event-driven pipeline
     * to evaluate the most recent visual data frames.
     *
     * @param targetId the identifier of the target whose camera feed is being processed
     */
    @Transactional(readOnly = true)
    public void processCameraStream(UUID targetId) {
        log.info("Processing continuous camera stream for targetId={}", targetId);

        telemetryRepository.findLatestByTargetId(targetId).ifPresentOrElse(
                record -> {
                    if (record.hasVisualAnomalies()) {
                        log.warn("Camera stream anomaly detected for targetId={} at recordedAt={}",
                                targetId, record.getRecordedAt());
                    } else {
                        log.debug("Camera stream nominal for targetId={}", targetId);
                    }
                },
                () -> log.debug("No telemetry records found for targetId={}", targetId)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Query Handlers (Read Side)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Analyzes visual data from a target's recent telemetry records and
     * returns those that contain detected anomalies.
     *
     * @param query the CQRS query specifying the target and result limit
     * @return a filtered list of TelemetryRecords containing visual anomalies
     */
    @Transactional(readOnly = true)
    public List<TelemetryRecord> analyzeVisualData(AnalyzeVisualDataQuery query) {
        log.info("Analyzing visual data for targetId={}, limit={}", query.targetId(), query.limit());

        List<TelemetryRecord> records = telemetryRepository
                .findAllByTargetIdOrderByRecordedAtAsc(query.targetId());

        return records.stream()
                .filter(TelemetryRecord::hasVisualAnomalies)
                .limit(query.limit())
                .toList();
    }

    /**
     * Retrieves all telemetry records for a given target, ordered chronologically.
     *
     * @param targetId the identifier of the monitored target
     * @return a list of telemetry records
     */
    @Transactional(readOnly = true)
    public List<TelemetryRecord> getTelemetryByTarget(UUID targetId) {
        log.info("Fetching all telemetry records for targetId={}", targetId);
        return telemetryRepository.findAllByTargetIdOrderByRecordedAtAsc(targetId);
    }
}
