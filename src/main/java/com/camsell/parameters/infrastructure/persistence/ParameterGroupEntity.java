package com.camsell.parameters.infrastructure.persistence;

import com.camsell.parameters.domain.model.ParameterDataType;
import com.camsell.parameters.domain.model.ParameterScope;
import com.camsell.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parameter_group")
public class ParameterGroupEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 400)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private ParameterDataType dataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 20)
    private ParameterScope scope;

    @Column(name = "multi_valued", nullable = false)
    private boolean multiValued;

    @Column(name = "filterable", nullable = false)
    private boolean filterable;

    @Column(name = "required_for_listing", nullable = false)
    private boolean requiredForListing;

    @Column(name = "show_in_specs", nullable = false)
    private boolean showInSpecs;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "active", nullable = false)
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ParameterDataType getDataType() {
        return dataType;
    }

    public void setDataType(ParameterDataType dataType) {
        this.dataType = dataType;
    }

    public ParameterScope getScope() {
        return scope;
    }

    public void setScope(ParameterScope scope) {
        this.scope = scope;
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public void setMultiValued(boolean multiValued) {
        this.multiValued = multiValued;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    public boolean isRequiredForListing() {
        return requiredForListing;
    }

    public void setRequiredForListing(boolean requiredForListing) {
        this.requiredForListing = requiredForListing;
    }

    public boolean isShowInSpecs() {
        return showInSpecs;
    }

    public void setShowInSpecs(boolean showInSpecs) {
        this.showInSpecs = showInSpecs;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
