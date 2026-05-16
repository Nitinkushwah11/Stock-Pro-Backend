package com.stockpro.supplier.service;

import com.stockpro.supplier.dto.SupplierRequestDTO;
import com.stockpro.supplier.dto.SupplierResponseDTO;

import java.util.List;

public interface SupplierService {
    SupplierResponseDTO createSupplier(SupplierRequestDTO dto);
    SupplierResponseDTO getById(Long id);
    List<SupplierResponseDTO> getAllSuppliers();
    List<SupplierResponseDTO> getActiveSuppliers();
    List<SupplierResponseDTO> searchSuppliers(String name);
    SupplierResponseDTO updateSupplier(Long id, SupplierRequestDTO dto);
    void deactivateSupplier(Long id);
    void deleteSupplier(Long id);
    List<SupplierResponseDTO> getByCity(String city);
    List<SupplierResponseDTO> getByCountry(String country);
    SupplierResponseDTO updateRating(Long id, Double rating);
}
