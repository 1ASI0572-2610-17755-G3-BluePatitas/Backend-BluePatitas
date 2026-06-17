package com.bluepatitas.bluepatitasbackend.monitoring.application.queries;

import java.util.UUID;

/**
 * AnalyzeVisualDataQuery
 * <p>
 * CQRS Read-side query that requests a visual data analysis report
 * for a specific target's most recent telemetry records. Useful for
 * dashboards and real-time monitoring views.
 * </p>
 *
 * @param targetId the identifier of the target whose visual data should be analyzed
 * @param limit    maximum number of recent telemetry records to include in the analysis
 */
public record AnalyzeVisualDataQuery(
        UUID targetId,
        int limit
) {
}
