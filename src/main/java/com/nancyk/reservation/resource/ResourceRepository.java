package com.nancyk.reservation.resource;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ResourceRepository extends
        JpaRepository<Resource, Long>,
        JpaSpecificationExecutor<Resource> {

    // In PostgreSQL this behaves like SELECT ... FOR UPDATE, locking the
    // resource row until the surrounding transaction completes.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT resource FROM Resource resource WHERE resource.id = :id")
    Optional<Resource> findByIdForUpdate(Long id);
}