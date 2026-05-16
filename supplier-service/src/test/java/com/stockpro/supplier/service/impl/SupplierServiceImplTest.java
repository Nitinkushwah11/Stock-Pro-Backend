package com.stockpro.supplier.service.impl;

import com.stockpro.supplier.dto.SupplierRequestDTO;
import com.stockpro.supplier.dto.SupplierResponseDTO;
import com.stockpro.supplier.entity.Supplier;
import com.stockpro.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Test
    void createSupplierSavesAndReturnsResponse() {
        SupplierRequestDTO request = new SupplierRequestDTO();
        request.setName("Best Supplies");
        request.setEmail("supplier@example.com");
        request.setCity("Delhi");
        request.setCountry("India");
        request.setRating(4.5);

        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier supplier = invocation.getArgument(0);
            supplier.setSupplierId(11L);
            return supplier;
        });

        SupplierResponseDTO response = supplierService.createSupplier(request);

        assertThat(response.getSupplierId()).isEqualTo(11L);
        assertThat(response.getName()).isEqualTo("Best Supplies");
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void getAllSuppliersMapsRepositoryResults() {
        Supplier supplier = supplier();
        when(supplierRepository.findAll()).thenReturn(List.of(supplier));

        List<SupplierResponseDTO> suppliers = supplierService.getAllSuppliers();

        assertThat(suppliers).hasSize(1);
        assertThat(suppliers.get(0).getName()).isEqualTo("Vendor");
    }

    @Test
    void queryAndUpdateMethodsMapRepositoryResults() {
        Supplier supplier = supplier();
        SupplierRequestDTO request = new SupplierRequestDTO();
        request.setName("Updated Vendor");
        request.setEmail("updated@example.com");
        request.setCity("Mumbai");
        request.setCountry("India");
        request.setRating(4.8);

        when(supplierRepository.findByIsActive(true)).thenReturn(List.of(supplier));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(supplier)).thenReturn(supplier);

        when(supplierRepository.searchByName("Vendor")).thenReturn(List.of(supplier));

        assertThat(supplierService.getActiveSuppliers()).hasSize(1);
        assertThat(supplierService.getById(1L).getSupplierId()).isEqualTo(1L);
        assertThat(supplierService.updateSupplier(1L, request).getName()).isEqualTo("Updated Vendor");
        assertThat(supplierService.searchSuppliers("Vendor")).hasSize(1);

        supplierService.deactivateSupplier(1L);
        assertThat(supplier.isActive()).isFalse();
    }

    @Test
    void deleteSupplierDeletesExistingSupplier() {
        when(supplierRepository.existsById(1L)).thenReturn(true);

        supplierService.deleteSupplier(1L);

        verify(supplierRepository).deleteById(1L);
    }

    private Supplier supplier() {
        return Supplier.builder()
                .supplierId(1L)
                .name("Vendor")
                .email("vendor@example.com")
                .phone("9999999999")
                .contactPerson("Asha")
                .address("Main Road")
                .city("Delhi")
                .country("India")
                .taxId("GST-1")
                .paymentTerms("NET30")
                .leadTimeDays(3)
                .rating(4.0)
                .isActive(true)
                .build();
    }
}
