package com.stockpro.product.repository;

import com.stockpro.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
    boolean existsBySku(String sku);
    boolean existsByBarcode(String barcode);
    List<Product> findByCategory(String category);
    List<Product> findByBrand(String brand);
    List<Product> findByIsActive(boolean isActive);
    List<Product> findByNameContainingIgnoreCase(String name);
    int countByCategory(String category);
}
