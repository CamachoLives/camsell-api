package com.camsell.parameters.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParameterGroupJpaRepository extends JpaRepository<ParameterGroupEntity, Long> {

    Optional<ParameterGroupEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<ParameterGroupEntity> findAllByOrderBySortOrderAscNameAsc();

    List<ParameterGroupEntity> findAllByActiveTrueOrderBySortOrderAscNameAsc();

    void deleteByCode(String code);
}
