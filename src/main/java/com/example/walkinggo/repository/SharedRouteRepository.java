package com.example.walkinggo.repository;

import com.example.walkinggo.entity.SharedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SharedRouteRepository extends JpaRepository<SharedRoute, Long> {
    Optional<SharedRoute> findByShareId(UUID shareId);

    boolean existsByWalkLogId(Long walkLogId);

    Optional<SharedRoute> findByWalkLogId(Long walkLogId);
}