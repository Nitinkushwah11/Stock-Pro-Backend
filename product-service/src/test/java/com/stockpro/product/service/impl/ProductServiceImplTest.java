package com.stockpro.product.service.impl;

import com.stockpro.product.client.AlertClient;
import com.stockpro.product.dto.ProductRequestDTO;
import com.stockpro.product.dto.ProductResponseDTO;
import com.stockpro.product.entity.Product;
import com.stockpro.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AlertClient alertClient;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProductSavesAndReturnsResponse() {
        ProductRequestDTO request = ProductRequestDTO.builder()
                .sku("SKU-1")
                .name("Keyboard")
                .category("Electronics")
                .brand("Acme")
                .unitOfMeasure("PCS")
                .costPrice(500.0)
                .sellingPrice(750.0)
                .reorderLevel(5)
                .maxStockLevel(100)
                .leadTimeDays(3)
                .barcode("BAR-1")
                .build();

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setProductId(1L);
            return product;
        });

        ProductResponseDTO response = productService.createProduct(request);

        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getSku()).isEqualTo("SKU-1");
        verify(productRepository).save(any(Product.class));
        verify(alertClient, org.mockito.Mockito.times(2)).sendAlert(any(AlertClient.AlertRequest.class));
    }

    @Test
    void getByIdReturnsMappedProduct() {
        Product product = product(7L);

        when(productRepository.findById(7L)).thenReturn(Optional.of(product));

        ProductResponseDTO response = productService.getById(7L);

        assertThat(response.getProductId()).isEqualTo(7L);
        assertThat(response.getName()).isEqualTo("Mouse");
    }

    @Test
    void queryMethodsMapRepositoryResults() {
        Product product = product(7L);
        when(productRepository.findBySku("SKU-7")).thenReturn(Optional.of(product));
        when(productRepository.findByBarcode("BAR-7")).thenReturn(Optional.of(product));
        when(productRepository.findByCategory("Electronics")).thenReturn(List.of(product));
        when(productRepository.findByBrand("Acme")).thenReturn(List.of(product));
        when(productRepository.findByNameContainingIgnoreCase("Mouse")).thenReturn(List.of(product));
        when(productRepository.findAll()).thenReturn(List.of(product));

        assertThat(productService.getBySku("SKU-7").getProductId()).isEqualTo(7L);
        assertThat(productService.getByBarcode("BAR-7").getProductId()).isEqualTo(7L);
        assertThat(productService.getByCategory("Electronics")).hasSize(1);
        assertThat(productService.getByBrand("Acme")).hasSize(1);
        assertThat(productService.searchProducts("Mouse")).hasSize(1);
        assertThat(productService.getAllProducts()).hasSize(1);
        assertThat(productService.getLowStockProducts()).isEmpty();
    }

    @Test
    void updateDeactivateAndDeleteUseExistingProduct() {
        Product product = product(7L);
        ProductRequestDTO request = ProductRequestDTO.builder()
                .sku("SKU-8")
                .name("Updated Mouse")
                .category("Accessories")
                .brand("Acme")
                .unitOfMeasure("PCS")
                .costPrice(300.0)
                .sellingPrice(450.0)
                .reorderLevel(4)
                .maxStockLevel(40)
                .leadTimeDays(2)
                .barcode("BAR-8")
                .build();

        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        assertThat(productService.updateProduct(7L, request).getName()).isEqualTo("Updated Mouse");
        productService.deactivateProduct(7L);
        assertThat(product.isActive()).isFalse();
        productService.deleteProduct(7L);

        verify(productRepository).delete(product);
        verify(alertClient, never()).sendAlert(AlertClient.AlertRequest.builder().build());
    }

    private Product product(Long id) {
        return Product.builder()
                .productId(id)
                .sku("SKU-7")
                .name("Mouse")
                .description("Wireless")
                .category("Electronics")
                .brand("Acme")
                .unitOfMeasure("PCS")
                .costPrice(250.0)
                .sellingPrice(350.0)
                .reorderLevel(3)
                .maxStockLevel(30)
                .leadTimeDays(1)
                .imageUrl("image.png")
                .barcode("BAR-7")
                .isActive(true)
                .build();
    }
}
