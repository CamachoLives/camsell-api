package com.camsell.parameters.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParameterOptionJpaRepository extends JpaRepository<ParameterOptionEntity, Long> {

    List<ParameterOptionEntity> findAllByOrderByGroupCodeAscSortOrderAscLabelAsc();

    List<ParameterOptionEntity> findAllByActiveTrueOrderByGroupCodeAscSortOrderAscLabelAsc();

    List<ParameterOptionEntity> findByGroupCodeOrderBySortOrderAscLabelAsc(String groupCode);

    List<ParameterOptionEntity> findByGroupCodeAndActiveTrueOrderBySortOrderAscLabelAsc(String groupCode);

    Optional<ParameterOptionEntity> findByGroupCodeAndCode(String groupCode, String code);

    void deleteByGroupCode(String groupCode);
}
