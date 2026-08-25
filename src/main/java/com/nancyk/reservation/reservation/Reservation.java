package com.nancyk.reservation.reservation;

import com.nancyk.reservation.resource.Resource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "reservations")
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "reserved_by", nullable = false, length = 150)
    private String reservedBy;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Reservation() {
    }

    public Reservation(
            Resource resource,
            String reservedBy,
            Instant startsAt,
            Instant endsAt
    ) {
        this.resource = resource;
        this.reservedBy = reservedBy;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = ReservationStatus.CONFIRMED;
    }

    public Long getId() {
        return id;
    }

    public Resource getResource() {
        return resource;
    }

    public String getReservedBy() {
        return reservedBy;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean isCancelled() {
        return status == ReservationStatus.CANCELLED;
    }
}