package com.stockpro.supplier.repository;

import com.stockpro.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findBySupplierId(Long supplierId);
    List<Supplier> findByCity(String city);
    List<Supplier> findByCountry(String country);

    @Query("SELECT s FROM Supplier s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Supplier> searchByName(@Param("name") String name);

    List<Supplier> findByIsActive(boolean isActive);
    Optional<Supplier> findByTaxId(String taxId);
    long countByIsActive(boolean isActive);
    void deleteBySupplierId(Long supplierId);
}
