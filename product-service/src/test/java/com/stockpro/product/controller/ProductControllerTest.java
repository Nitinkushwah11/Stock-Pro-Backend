package com.stockpro.product.controller;

import com.stockpro.product.dto.ProductRequestDTO;
import com.stockpro.product.dto.ProductResponseDTO;
import com.stockpro.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    void delegatesAllProductEndpoints() {
        ProductRequestDTO request = ProductRequestDTO.builder().name("Keyboard").category("IT").brand("Acme")
                .unitOfMeasure("PCS").costPrice(100.0).sellingPrice(150.0).barcode("BAR-1").build();
        ProductResponseDTO response = ProductResponseDTO.builder().productId(1L).name("Keyboard").sku("SKU-1").build();

        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(response);
        when(productService.getAllProducts()).thenReturn(List.of(response));
        when(productService.getById(1L)).thenReturn(response);
        when(productService.getBySku("SKU-1")).thenReturn(response);
        when(productService.getByCategory("IT")).thenReturn(List.of(response));
        when(productService.getByBrand("Acme")).thenReturn(List.of(response));
        when(productService.getByBarcode("BAR-1")).thenReturn(response);
        when(productService.searchProducts("Key")).thenReturn(List.of(response));
        when(productService.getLowStockProducts()).thenReturn(List.of(response));
        when(productService.updateProduct(1L, request)).thenReturn(response);

        assertThat(productController.create(request).getStatusCode().value()).isEqualTo(201);
        assertThat(productController.getAll().getBody()).hasSize(1);
        assertThat(productController.getById(1L).getBody()).isSameAs(response);
        assertThat(productController.getBySku("SKU-1").getBody()).isSameAs(response);
        assertThat(productController.getByCategory("IT").getBody()).hasSize(1);
        assertThat(productController.getByBrand("Acme").getBody()).hasSize(1);
        assertThat(productController.getByBarcode("BAR-1").getBody()).isSameAs(response);
        assertThat(productController.search("Key").getBody()).hasSize(1);
        assertThat(productController.getLowStock().getBody()).hasSize(1);
        assertThat(productController.update(1L, request).getBody()).isSameAs(response);
        assertThat(productController.deactivate(1L).getStatusCode().value()).isEqualTo(204);
        assertThat(productController.delete(1L).getStatusCode().value()).isEqualTo(204);

        verify(productService).deactivateProduct(1L);
        verify(productService).deleteProduct(1L);
    }
}
