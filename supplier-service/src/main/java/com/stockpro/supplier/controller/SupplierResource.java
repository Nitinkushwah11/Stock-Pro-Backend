package com.stockpro.supplier.controller;

import com.stockpro.supplier.dto.SupplierRequestDTO;
import com.stockpro.supplier.dto.SupplierResponseDTO;
import com.stockpro.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier API", description = "Supplier management endpoints")
public class SupplierResource {

    private final SupplierService supplierService;

    @PostMapping
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<SupplierResponseDTO> create(@Valid @RequestBody SupplierRequestDTO dto) {
        return new ResponseEntity<>(supplierService.createSupplier(dto), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all suppliers")
    public ResponseEntity<List<SupplierResponseDTO>> getAll() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active suppliers")
    public ResponseEntity<List<SupplierResponseDTO>> getActive() {
        return ResponseEntity.ok(supplierService.getActiveSuppliers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<SupplierResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search suppliers by name")
    public ResponseEntity<List<SupplierResponseDTO>> search(@RequestParam String name) {
        return ResponseEntity.ok(supplierService.searchSuppliers(name));
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Get suppliers by city")
    public ResponseEntity<List<SupplierResponseDTO>> getByCity(@PathVariable String city) {
        return ResponseEntity.ok(supplierService.getByCity(city));
    }

    @GetMapping("/country/{country}")
    @Operation(summary = "Get suppliers by country")
    public ResponseEntity<List<SupplierResponseDTO>> getByCountry(@PathVariable String country) {
        return ResponseEntity.ok(supplierService.getByCountry(country));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a supplier")
    public ResponseEntity<SupplierResponseDTO> update(@PathVariable Long id, @Valid @RequestBody SupplierRequestDTO dto) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, dto));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a supplier")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        supplierService.deactivateSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/rating")
    @Operation(summary = "Update supplier rating")
    public ResponseEntity<SupplierResponseDTO> updateRating(@PathVariable Long id, @RequestParam Double rating) {
        return ResponseEntity.ok(supplierService.updateRating(id, rating));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a supplier")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
