package com.stockpro.supplier.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierResponseDTO {
    private Long supplierId;
    private String name;
    private String email;
    private String phone;
    private String contactPerson;
    private String address;
    private String city;
    private String country;
    private String taxId;
    private String paymentTerms;
    private Integer leadTimeDays;
    private Double rating;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
