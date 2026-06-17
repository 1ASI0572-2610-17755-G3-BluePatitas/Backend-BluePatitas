package com.bluepatitas.bluepatitasbackend.monitoring.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.PerimeterAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * JpaAlertRepository
 * <p>
 * Spring Data JPA derived-query repository for {@link PerimeterAlert} entities.
 * This interface is an infrastructure detail and must NOT be used directly
 * outside of {@link RelationalAlertRepository}.
 * </p>
 */
public interface JpaAlertRepository extends JpaRepository<PerimeterAlert, UUID> {

    /**
     * Fetches all alerts for a given target where location tracking is active.
     *
     * @param targetId the monitored target's UUID
     * @return list of active tracking alerts
     */
    List<PerimeterAlert> findAllByTargetIdAndTrackingActiveTrue(UUID targetId);

    /**
     * Checks whether any alert exists for a given target.
     *
     * @param targetId the monitored target's UUID
     * @return true if at least one alert exists
     */
    boolean existsByTargetId(UUID targetId);
}
