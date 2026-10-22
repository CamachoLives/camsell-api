package com.camsell.catalog.infrastructure.persistence;

import com.camsell.catalog.domain.model.ListingStatus;
import com.camsell.shared.infrastructure.persistence.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listing")
public class ListingEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference", nullable = false, unique = true, length = 20)
    private String reference;

    @Column(name = "slug", nullable = false, unique = true, length = 220)
    private String slug;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "seller_name", nullable = false, length = 160)
    private String sellerName;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "brand_name", nullable = false, length = 80)
    private String brandName;

    @Column(name = "model_id", nullable = false)
    private Long modelId;

    @Column(name = "model_name", nullable = false, length = 80)
    private String modelName;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "model_year", nullable = false)
    private int modelYear;

    @Column(name = "mileage_km", nullable = false)
    private int mileageKm;

    @Column(name = "price_amount", nullable = false, precision = 16, scale = 2)
    private BigDecimal priceAmount;

    @Column(name = "price_currency", nullable = false, length = 3)
    private String priceCurrency;

    @Column(name = "negotiable", nullable = false)
    private boolean negotiable;

    @Column(name = "description", nullable = false, length = 6000)
    private String description;

    @Column(name = "city_code", length = 60)
    private String cityCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ListingStatus status;

    @Column(name = "featured", nullable = false)
    private boolean featured;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "published_at")
    private Instant publishedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    @OrderBy("position ASC")
    private List<ListingImageEntity> images = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    @OrderBy("groupCode ASC")
    private List<ListingAttributeEntity> attributes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getModelYear() {
        return modelYear;
    }

    public void setModelYear(int modelYear) {
        this.modelYear = modelYear;
    }

    public int getMileageKm() {
        return mileageKm;
    }

    public void setMileageKm(int mileageKm) {
        this.mileageKm = mileageKm;
    }

    public BigDecimal getPriceAmount() {
        return priceAmount;
    }

    public void setPriceAmount(BigDecimal priceAmount) {
        this.priceAmount = priceAmount;
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }

    public void setPriceCurrency(String priceCurrency) {
        this.priceCurrency = priceCurrency;
    }

    public boolean isNegotiable() {
        return negotiable;
    }

    public void setNegotiable(boolean negotiable) {
        this.negotiable = negotiable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<ListingImageEntity> getImages() {
        return images;
    }

    public List<ListingAttributeEntity> getAttributes() {
        return attributes;
    }
}
