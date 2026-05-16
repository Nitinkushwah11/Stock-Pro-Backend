package com.stockpro.product.service;

import com.stockpro.product.dto.ProductRequestDTO;
import com.stockpro.product.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO requestDTO);
    ProductResponseDTO getById(Long id);
    ProductResponseDTO getBySku(String sku);
    ProductResponseDTO getByBarcode(String barcode);
    List<ProductResponseDTO> getByCategory(String category);
    List<ProductResponseDTO> getByBrand(String brand);
    List<ProductResponseDTO> searchProducts(String name);
    List<ProductResponseDTO> getAllProducts();
    ProductResponseDTO updateProduct(Long id, ProductRequestDTO requestDTO);
    void deactivateProduct(Long id);
    void deleteProduct(Long id);
    List<ProductResponseDTO> getLowStockProducts();
}