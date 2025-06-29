package com.hobbySphere.dto;

public class ThemeResponseDTO {
    private Long id;
    private String name;
    private String values;
    private String valuesMobile;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public ThemeResponseDTO() {}

    public ThemeResponseDTO(Long id, String name, String values, String valuesMobile, Boolean isActive, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.values = values;
        this.valuesMobile = valuesMobile;
        this.isActive = isActive != null ? isActive : false;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Map from Theme entity
    public ThemeResponseDTO(com.hobbySphere.entities.Theme theme) {
        this.id = theme.getId();
        this.name = theme.getName();
        this.values = theme.getValues();
        this.valuesMobile = theme.getValuesMobile();
        this.isActive = theme.getIsActive() != null ? theme.getIsActive() : false;
        this.createdAt = theme.getCreated_at() != null ? theme.getCreated_at().toString() : null;
        this.updatedAt = theme.getUpdated_at() != null ? theme.getUpdated_at().toString() : null;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValues() { return values; }
    public void setValues(String values) { this.values = values; }

    public String getValuesMobile() { return valuesMobile; }
    public void setValuesMobile(String valuesMobile) { this.valuesMobile = valuesMobile; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}