package com.bluepatitas.bluepatitasbackend.iam.domain.model.entities;

import com.bluepatitas.bluepatitasbackend.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "device_tokens")
@NoArgsConstructor
public class DeviceToken extends AbstractDomainAggregateRoot<DeviceToken> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "device_type")
    private String deviceType;

    public DeviceToken(Long userId, String token, String deviceType) {
        this.userId = userId;
        this.token = token;
        this.deviceType = deviceType;
    }
}
