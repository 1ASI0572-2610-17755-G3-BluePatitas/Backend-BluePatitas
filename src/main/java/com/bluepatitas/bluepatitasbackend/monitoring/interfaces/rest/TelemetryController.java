package com.bluepatitas.bluepatitasbackend.monitoring.interfaces.rest;

import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.ProcessTelemetryCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.application.queries.AnalyzeVisualDataQuery;
import com.bluepatitas.bluepatitasbackend.monitoring.application.services.TelemetryAnalysisService;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.TelemetryRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * TelemetryController
 * <p>
 * REST interface for telemetry ingestion and retrieval operations within
 * the Monitoring Bounded Context. Delegates to the application layer
 * following the DDD interfaces → application → domain dependency direction.
 * </p>
 */
@RestController
@RequestMapping("/api/monitoring/telemetry")
@RequiredArgsConstructor
@Tag(name = "Monitoring – Telemetry", description = "Endpoints for environmental and visual telemetry management")
public class TelemetryController {

    private final TelemetryAnalysisService telemetryAnalysisService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/monitoring/telemetry
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ingests a new telemetry reading from an environmental sensor or camera device.
     * Triggers threshold validation and visual anomaly detection.
     *
     * @param request the telemetry data payload
     * @return the created TelemetryRecord with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Ingest telemetry reading",
               description = "Processes an environmental and visual telemetry reading for a monitored target.")
    public ResponseEntity<TelemetryRecord> processTelemetry(
            @Valid @RequestBody TelemetryRequest request) {

        ProcessTelemetryCommand command = new ProcessTelemetryCommand(
                request.targetId(),
                request.ambientTemperature(),
                request.ambientHumidity(),
                request.visualData()
        );

        TelemetryRecord saved = telemetryAnalysisService.processTelemetry(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/monitoring/telemetry/{targetId}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves the full telemetry history for a monitored target.
     *
     * @param targetId the UUID of the target to retrieve telemetry for
     * @return a list of TelemetryRecord entries ordered by timestamp
     */
    @GetMapping("/{targetId}")
    @Operation(summary = "Get telemetry by target",
               description = "Returns all telemetry records for a specific monitored target, ordered chronologically.")
    public ResponseEntity<List<TelemetryRecord>> getTelemetryByTarget(
            @PathVariable UUID targetId) {

        List<TelemetryRecord> records = telemetryAnalysisService.getTelemetryByTarget(targetId);
        return ResponseEntity.ok(records);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner Request DTO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Request body for telemetry ingestion.
     */
    public record TelemetryRequest(
            UUID targetId,
            BigDecimal ambientTemperature,
            BigDecimal ambientHumidity,
            String visualData
    ) {}
}
