package com.camsell.parameters.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandJpaRepository extends JpaRepository<BrandEntity, Long> {

    Optional<BrandEntity> findBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);

    List<BrandEntity> findAllByOrderByNameAsc();

    List<BrandEntity> findAllByActiveTrueOrderByNameAsc();
}
