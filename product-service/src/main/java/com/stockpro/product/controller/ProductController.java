package com.stockpro.product.controller;

import com.stockpro.product.dto.ProductRequestDTO;
import com.stockpro.product.dto.ProductResponseDTO;
import com.stockpro.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product Catalog API", description = "Endpoints for managing products [cite: 256]")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO dto) {
        return new ResponseEntity<>(productService.createProduct(dto), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all products")
    public ResponseEntity<List<ProductResponseDTO>> getAll() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get a product by SKU")
    public ResponseEntity<ProductResponseDTO> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getBySku(sku));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by Category")
    public ResponseEntity<List<ProductResponseDTO>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getByCategory(category));
    }

    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get products by Brand")
    public ResponseEntity<List<ProductResponseDTO>> getByBrand(@PathVariable String brand) {
        return ResponseEntity.ok(productService.getByBrand(brand));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get product by Barcode lookup for mobile scan-based workflows [cite: 258]")
    public ResponseEntity<ProductResponseDTO> getByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(productService.getByBarcode(barcode));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name")
    public ResponseEntity<List<ProductResponseDTO>> search(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchProducts(name));
    }

    @GetMapping("/lowStock")
    @Operation(summary = "Get low stock products query used by the dashboard widget [cite: 258]")
    public ResponseEntity<List<ProductResponseDTO>> getLowStock() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ProductResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Soft delete / Deactivate a product")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hard delete a product")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}