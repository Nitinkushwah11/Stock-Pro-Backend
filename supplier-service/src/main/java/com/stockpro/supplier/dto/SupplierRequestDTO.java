package com.stockpro.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequestDTO {

    @NotBlank
    private String name;

    @Email
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
}
