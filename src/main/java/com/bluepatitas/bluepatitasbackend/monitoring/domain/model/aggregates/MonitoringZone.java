package com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates;

import com.bluepatitas.bluepatitasbackend.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * MonitoringZone aggregate root.
 */
@Entity
@Table(name = "monitoring_zones")
@Getter
@Setter
@NoArgsConstructor
public class MonitoringZone extends AbstractDomainAggregateRoot<MonitoringZone> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "temperature_c")
    private Double temperatureC;

    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "animal_count", nullable = false)
    private Integer animalCount;

    @Column(name = "camera_enabled", nullable = false)
    private Boolean cameraEnabled;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    public MonitoringZone(UUID id, UUID targetId, String name, Double temperatureC, Double humidity, String status, Integer animalCount, Boolean cameraEnabled, String imageUrl) {
        this.id = id;
        this.targetId = targetId;
        this.name = name;
        this.temperatureC = temperatureC;
        this.humidity = humidity;
        this.status = status;
        this.animalCount = animalCount;
        this.cameraEnabled = cameraEnabled;
        this.imageUrl = imageUrl;
    }
}
