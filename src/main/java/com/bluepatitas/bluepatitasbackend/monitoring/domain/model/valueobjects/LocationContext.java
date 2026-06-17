package com.bluepatitas.bluepatitasbackend.monitoring.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * LocationContext
 * <p>
 * Value Object that represents a precise geographic location using
 * latitude and longitude coordinates. Used within PerimeterAlert
 * to track the current position of a subject during a breach event.
 * </p>
 */
@Embeddable
public record LocationContext(
        BigDecimal latitude,
        BigDecimal longitude
) {

    /**
     * Calculates the approximate distance in meters to another location
     * using the Haversine formula.
     *
     * @param other the other location to measure distance to
     * @return distance in meters (approximate)
     */
    public double distanceTo(LocationContext other) {
        final int EARTH_RADIUS_METERS = 6_371_000;
        double lat1Rad = Math.toRadians(this.latitude.doubleValue());
        double lat2Rad = Math.toRadians(other.latitude().doubleValue());
        double deltaLat = Math.toRadians(other.latitude().doubleValue() - this.latitude.doubleValue());
        double deltaLon = Math.toRadians(other.longitude().doubleValue() - this.longitude.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Checks if this location is within the specified radius (meters) of another location.
     *
     * @param center       the central reference location
     * @param radiusMeters the perimeter radius in meters
     * @return true if inside the perimeter, false otherwise
     */
    public boolean isWithinPerimeter(LocationContext center, double radiusMeters) {
        return distanceTo(center) <= radiusMeters;
    }

    @Override
    public String toString() {
        return "LocationContext{lat=" + latitude + ", lon=" + longitude + "}";
    }
}
