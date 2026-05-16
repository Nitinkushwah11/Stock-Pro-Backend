package com.stockpro.supplier.controller;

import com.stockpro.supplier.dto.SupplierRequestDTO;
import com.stockpro.supplier.dto.SupplierResponseDTO;
import com.stockpro.supplier.service.SupplierService;
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
class SupplierResourceTest {

    @Mock
    private SupplierService supplierService;

    @InjectMocks
    private SupplierResource supplierResource;

    @Test
    void delegatesAllSupplierEndpoints() {
        SupplierRequestDTO request = new SupplierRequestDTO();
        request.setName("Best Supplies");
        SupplierResponseDTO response = new SupplierResponseDTO();
        response.setSupplierId(1L);
        response.setName("Best Supplies");

        when(supplierService.createSupplier(any(SupplierRequestDTO.class))).thenReturn(response);
        when(supplierService.getAllSuppliers()).thenReturn(List.of(response));
        when(supplierService.getActiveSuppliers()).thenReturn(List.of(response));
        when(supplierService.getById(1L)).thenReturn(response);
        when(supplierService.searchSuppliers("Best")).thenReturn(List.of(response));
        when(supplierService.getByCity("Delhi")).thenReturn(List.of(response));
        when(supplierService.getByCountry("India")).thenReturn(List.of(response));
        when(supplierService.updateSupplier(1L, request)).thenReturn(response);
        when(supplierService.updateRating(1L, 4.5)).thenReturn(response);

        assertThat(supplierResource.create(request).getStatusCode().value()).isEqualTo(201);
        assertThat(supplierResource.getAll().getBody()).hasSize(1);
        assertThat(supplierResource.getActive().getBody()).hasSize(1);
        assertThat(supplierResource.getById(1L).getBody()).isSameAs(response);
        assertThat(supplierResource.search("Best").getBody()).hasSize(1);
        assertThat(supplierResource.getByCity("Delhi").getBody()).hasSize(1);
        assertThat(supplierResource.getByCountry("India").getBody()).hasSize(1);
        assertThat(supplierResource.update(1L, request).getBody()).isSameAs(response);
        assertThat(supplierResource.deactivate(1L).getStatusCode().value()).isEqualTo(204);
        assertThat(supplierResource.updateRating(1L, 4.5).getBody()).isSameAs(response);
        assertThat(supplierResource.delete(1L).getStatusCode().value()).isEqualTo(204);

        verify(supplierService).deactivateSupplier(1L);
        verify(supplierService).deleteSupplier(1L);
    }
}
