package com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates;

import com.bluepatitas.bluepatitasbackend.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Shelter aggregate root.
 */
@Entity
@Table(name = "monitoring_shelters")
@Getter
@Setter
@NoArgsConstructor
public class Shelter extends AbstractDomainAggregateRoot<Shelter> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "administrator", nullable = false, length = 150)
    private String administrator;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    public Shelter(UUID id, String name, String city, String address, String administrator, String phone, String email) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.administrator = administrator;
        this.phone = phone;
        this.email = email;
    }
}
