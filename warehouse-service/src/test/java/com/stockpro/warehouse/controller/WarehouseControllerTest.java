package com.stockpro.warehouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockpro.warehouse.dto.StockLevelResponseDTO;
import com.stockpro.warehouse.dto.StockTransferRequestDTO;
import com.stockpro.warehouse.dto.StockUpdateRequestDTO;
import com.stockpro.warehouse.dto.WarehouseRequestDTO;
import com.stockpro.warehouse.dto.WarehouseResponseDTO;
import com.stockpro.warehouse.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WarehouseController.class)
@AutoConfigureMockMvc(addFilters = false)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WarehouseService warehouseService;

    @Test
    void createWarehouseReturnsCreated() throws Exception {
        WarehouseRequestDTO request = WarehouseRequestDTO.builder()
                .name("Main Warehouse")
                .location("Delhi")
                .address("Plot 45")
                .managerId(101)
                .capacity(1000)
                .build();

        when(warehouseService.createWarehouse(any(WarehouseRequestDTO.class)))
                .thenReturn(WarehouseResponseDTO.builder().warehouseId(1).name("Main Warehouse").build());

        mockMvc.perform(post("/warehouse")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(warehouseService).createWarehouse(any(WarehouseRequestDTO.class));
    }

    @Test
    void getWarehouseByIdReturnsOk() throws Exception {
        when(warehouseService.getWarehouseById(1))
                .thenReturn(WarehouseResponseDTO.builder().warehouseId(1).name("Main Warehouse").build());

        mockMvc.perform(get("/warehouse/1"))
                .andExpect(status().isOk());

        verify(warehouseService).getWarehouseById(1);
    }

    @Test
    void getAllWarehousesReturnsOk() throws Exception {
        when(warehouseService.getAllActiveWarehouses())
                .thenReturn(List.of(WarehouseResponseDTO.builder().warehouseId(1).name("Main Warehouse").build()));

        mockMvc.perform(get("/warehouse"))
                .andExpect(status().isOk());

        verify(warehouseService).getAllActiveWarehouses();
    }

    @Test
    void updateWarehouseReturnsOk() throws Exception {
        WarehouseRequestDTO request = WarehouseRequestDTO.builder()
                .name("Updated Warehouse")
                .location("Mumbai")
                .address("Plot 90")
                .managerId(102)
                .capacity(2000)
                .build();

        when(warehouseService.updateWarehouse(eq(1), any(WarehouseRequestDTO.class)))
                .thenReturn(WarehouseResponseDTO.builder().warehouseId(1).name("Updated Warehouse").build());

        mockMvc.perform(put("/warehouse/1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(warehouseService).updateWarehouse(eq(1), any(WarehouseRequestDTO.class));
    }

    @Test
    void deleteWarehouseReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/warehouse/1"))
                .andExpect(status().isNoContent());

        verify(warehouseService).deleteWarehouse(1);
    }

    @Test
    void getStockLevelReturnsOk() throws Exception {
        when(warehouseService.getStockLevel(10L, 20L))
                .thenReturn(StockLevelResponseDTO.builder().warehouseId(10L).productId(20L).quantity(50).build());

        mockMvc.perform(get("/warehouse/10/stock/20"))
                .andExpect(status().isOk());

        verify(warehouseService).getStockLevel(10L, 20L);
    }

    @Test
    void getStockByWarehouseReturnsOk() throws Exception {
        when(warehouseService.getStockByWarehouse(10L))
                .thenReturn(List.of(StockLevelResponseDTO.builder().warehouseId(10L).productId(20L).build()));

        mockMvc.perform(get("/warehouse/10/stock"))
                .andExpect(status().isOk());

        verify(warehouseService).getStockByWarehouse(10L);
    }

    @Test
    void getStockByProductReturnsOk() throws Exception {
        when(warehouseService.getStockByProduct(20L))
                .thenReturn(List.of(StockLevelResponseDTO.builder().warehouseId(10L).productId(20L).build()));

        mockMvc.perform(get("/warehouse/stock/product/20"))
                .andExpect(status().isOk());

        verify(warehouseService).getStockByProduct(20L);
    }

    @Test
    void addStockReturnsOk() throws Exception {
        StockUpdateRequestDTO request = StockUpdateRequestDTO.builder()
                .warehouseId(10L)
                .productId(20L)
                .quantity(5)
                .build();

        when(warehouseService.addStock(any(StockUpdateRequestDTO.class)))
                .thenReturn(StockLevelResponseDTO.builder().warehouseId(10L).productId(20L).quantity(15).build());

        mockMvc.perform(post("/warehouse/stock/add")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(warehouseService).addStock(any(StockUpdateRequestDTO.class));
    }

    @Test
    void deductStockReturnsOk() throws Exception {
        StockUpdateRequestDTO request = StockUpdateRequestDTO.builder()
                .warehouseId(10L)
                .productId(20L)
                .quantity(3)
                .build();

        when(warehouseService.deductStock(any(StockUpdateRequestDTO.class)))
                .thenReturn(StockLevelResponseDTO.builder().warehouseId(10L).productId(20L).quantity(7).build());

        mockMvc.perform(post("/warehouse/stock/deduct")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(warehouseService).deductStock(any(StockUpdateRequestDTO.class));
    }

    @Test
    void reserveStockReturnsOk() throws Exception {
        StockUpdateRequestDTO request = StockUpdateRequestDTO.builder()
                .warehouseId(10L)
                .productId(20L)
                .quantity(2)
                .build();

        when(warehouseService.reserveStock(any(StockUpdateRequestDTO.class)))
                .thenReturn(StockLevelResponseDTO.builder().warehouseId(10L).productId(20L).reservedQuantity(2).build());

        mockMvc.perform(post("/warehouse/stock/reserve")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(warehouseService).reserveStock(any(StockUpdateRequestDTO.class));
    }

    @Test
    void releaseReservationReturnsOk() throws Exception {
        StockUpdateRequestDTO request = StockUpdateRequestDTO.builder()
                .warehouseId(10L)
                .productId(20L)
                .quantity(2)
                .build();

        when(warehouseService.releaseReservation(any(StockUpdateRequestDTO.class)))
                .thenReturn(StockLevelResponseDTO.builder().warehouseId(10L).productId(20L).reservedQuantity(0).build());

        mockMvc.perform(post("/warehouse/stock/release")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(warehouseService).releaseReservation(any(StockUpdateRequestDTO.class));
    }

    @Test
    void transferStockReturnsNoContent() throws Exception {
        StockTransferRequestDTO request = StockTransferRequestDTO.builder()
                .fromWarehouseId(1L)
                .toWarehouseId(2L)
                .productId(20L)
                .quantity(5)
                .build();

        mockMvc.perform(post("/warehouse/stock/transfer")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(warehouseService).transferStock(any(StockTransferRequestDTO.class));
    }

    @Test
    void getLowStockItemsReturnsOk() throws Exception {
        when(warehouseService.getLowStockItems(10))
                .thenReturn(List.of(StockLevelResponseDTO.builder().warehouseId(10L).productId(20L).quantity(4).build()));

        mockMvc.perform(get("/warehouse/stock/low-stock"))
                .andExpect(status().isOk());

        verify(warehouseService).getLowStockItems(10);
    }
}
