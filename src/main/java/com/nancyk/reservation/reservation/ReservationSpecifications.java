package com.nancyk.reservation.reservation;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Locale;

public final class ReservationSpecifications {

    private ReservationSpecifications() {
    }

    public static Specification<Reservation> hasResourceId(Long resourceId) {
        return (root, query, criteriaBuilder) ->
                resourceId == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(
                        root.get("resource").get("id"),
                        resourceId
                );
    }

    public static Specification<Reservation> hasStatus(
            ReservationStatus status
    ) {
        return (root, query, criteriaBuilder) ->
                status == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Reservation> reservedByContains(
            String reservedBy
    ) {
        return (root, query, criteriaBuilder) -> {
            if (reservedBy == null || reservedBy.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("reservedBy")),
                    "%" + reservedBy.toLowerCase(Locale.ROOT) + "%"
            );
        };
    }

    public static Specification<Reservation> overlapsTimeRange(
            Instant from,
            Instant to
    ) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) {
                return criteriaBuilder.conjunction();
            }

            if (from == null) {
                return criteriaBuilder.lessThan(root.get("startsAt"), to);
            }

            if (to == null) {
                return criteriaBuilder.greaterThan(root.get("endsAt"), from);
            }

            return criteriaBuilder.and(
                    criteriaBuilder.lessThan(root.get("startsAt"), to),
                    criteriaBuilder.greaterThan(root.get("endsAt"), from)
            );
        };
    }
}