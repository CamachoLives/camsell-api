package com.camsell.parameters.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VehicleModelJpaRepository extends JpaRepository<VehicleModelEntity, Long> {

    List<VehicleModelEntity> findByBrandIdOrderByNameAsc(Long brandId);

    List<VehicleModelEntity> findByBrandIdAndActiveTrueOrderByNameAsc(Long brandId);

    List<VehicleModelEntity> findByBrandIdInOrderByNameAsc(Collection<Long> brandIds);

    List<VehicleModelEntity> findByBrandIdInAndActiveTrueOrderByNameAsc(Collection<Long> brandIds);

    boolean existsByBrandIdAndNameIgnoreCase(Long brandId, String name);

    void deleteByBrandId(Long brandId);
}
