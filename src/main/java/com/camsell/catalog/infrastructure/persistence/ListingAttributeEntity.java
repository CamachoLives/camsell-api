package com.camsell.catalog.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Valor de un parametro aplicado a una publicacion. */
@Entity
@Table(name = "listing_attribute")
public class ListingAttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Copia de solo lectura de la FK que gestiona la coleccion del agregado.
     * Permite filtrar por atributos con subconsultas en las Specifications.
     */
    @Column(name = "listing_id", insertable = false, updatable = false)
    private Long listingId;

    @Column(name = "group_code", nullable = false, length = 40)
    private String groupCode;

    @Column(name = "option_code", length = 60)
    private String optionCode;

    @Column(name = "text_value", length = 500)
    private String textValue;

    @Column(name = "number_value", precision = 16, scale = 2)
    private BigDecimal numberValue;

    @Column(name = "bool_value")
    private Boolean boolValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getListingId() {
        return listingId;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getOptionCode() {
        return optionCode;
    }

    public void setOptionCode(String optionCode) {
        this.optionCode = optionCode;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public BigDecimal getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(BigDecimal numberValue) {
        this.numberValue = numberValue;
    }

    public Boolean getBoolValue() {
        return boolValue;
    }

    public void setBoolValue(Boolean boolValue) {
        this.boolValue = boolValue;
    }
}
