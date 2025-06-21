package com.dlvery.controller;

import com.dlvery.model.Delivery;
import com.dlvery.model.Inventory;
import com.dlvery.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
    }

    @Test
    void testGetAllInventory() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setId("1");
        inventory.setSku("SKU001");
        inventory.setName("Laptop");
        inventory.setQuantity(100);
        List<Inventory> inventoryList = Collections.singletonList(inventory);

        when(inventoryService.getAllInventory()).thenReturn(inventoryList);

        mockMvc.perform(get("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU001"))
                .andExpect(jsonPath("$[0].name").value("Laptop"));

        verify(inventoryService, times(1)).getAllInventory();
    }

    @Test
    void testAddInventory() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setId("1");
        inventory.setSku("SKU001");
        inventory.setName("Laptop");
        inventory.setQuantity(100);

        when(inventoryService.addInventory(any(Inventory.class))).thenReturn(inventory);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU001\",\"name\":\"Laptop\",\"category\":\"Electronics\",\"damaged\":false,\"perishable\":false,\"expiryDate\":\"N/A\",\"quantity\":100,\"lowStockThreshold\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU001"))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(inventoryService, times(1)).addInventory(any(Inventory.class));
    }

    @Test
    void testUpdateInventory() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setId("1");
        inventory.setSku("SKU001");
        inventory.setName("Laptop Updated");
        inventory.setQuantity(50);

        when(inventoryService.updateInventory(eq("1"), any(Inventory.class))).thenReturn(inventory);

        mockMvc.perform(put("/api/inventory/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU001\",\"name\":\"Laptop Updated\",\"category\":\"Electronics\",\"damaged\":false,\"perishable\":false,\"expiryDate\":\"N/A\",\"quantity\":50,\"lowStockThreshold\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop Updated"));

        verify(inventoryService, times(1)).updateInventory(eq("1"), any(Inventory.class));
    }

    @Test
    void testDeleteInventory() throws Exception {
        doNothing().when(inventoryService).deleteInventory("1");

        mockMvc.perform(delete("/api/inventory/1"))
                .andExpect(status().isOk());

        verify(inventoryService, times(1)).deleteInventory("1");
    }

    @Test
    void testUploadInventoryFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "inventory.csv",
                MediaType.TEXT_PLAIN_VALUE, "sku,name,category,damaged,perishable,expiryDate,quantity,lowStockThreshold\nSKU002,Mouse,Electronics,false,false,N/A,50,5".getBytes());

        doNothing().when(inventoryService).uploadInventoryFile(any(MultipartFile.class));

        mockMvc.perform(multipart("/api/inventory/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Inventory uploaded successfully"));

        verify(inventoryService, times(1)).uploadInventoryFile(any(MultipartFile.class));
    }

    @Test
    void testAssignForDelivery() throws Exception {
        doNothing().when(inventoryService).assignForDelivery(anyString(), anyInt(), anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/inventory/assign")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("sku", "SKU001")
                        .param("quantity", "10")
                        .param("agentId", "agent1")
                        .param("customerName", "John Doe")
                        .param("address", "123 Main St"))
                .andExpect(status().isOk())
                .andExpect(content().string("Assigned for delivery"));

        verify(inventoryService, times(1)).assignForDelivery(anyString(), anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    void testTrackDeliveryBySku() throws Exception {
        Delivery delivery = new Delivery();
        delivery.setId("1");
        delivery.setSku("SKU001");
        delivery.setStatus("PENDING");
        List<Object> deliveries = Collections.singletonList(delivery);

        when(inventoryService.trackDeliveryBySku("SKU001")).thenReturn(deliveries);

        mockMvc.perform(get("/api/inventory/track/sku/SKU001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU001"));

        verify(inventoryService, times(1)).trackDeliveryBySku("SKU001");
    }

    @Test
    void testTrackDeliveryByAgent() throws Exception {
        Delivery delivery = new Delivery();
        delivery.setId("1");
        delivery.setSku("SKU001");
        delivery.setAgentId("agent1");
        List<Object> deliveries = Collections.singletonList(delivery);

        when(inventoryService.trackDeliveryByAgent("agent1")).thenReturn(deliveries);

        mockMvc.perform(get("/api/inventory/track/agent/agent1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].agentId").value("agent1"));

        verify(inventoryService, times(1)).trackDeliveryByAgent("agent1");
    }

    @Test
    void testReportDeliveredGoods() throws Exception {
        Delivery delivery = new Delivery();
        delivery.setId("1");
        delivery.setSku("SKU001");
        delivery.setStatus("DELIVERED");
        List<Object> deliveries = Collections.singletonList(delivery);

        when(inventoryService.reportDeliveredGoods(anyString(), anyString())).thenReturn(deliveries);

        mockMvc.perform(get("/api/inventory/report/delivered")
                        .param("startDate", "2025-06-01")
                        .param("endDate", "2025-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU001"));

        verify(inventoryService, times(1)).reportDeliveredGoods(anyString(), anyString());
    }

    @Test
    void testReportDamagedGoods() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setId("1");
        inventory.setSku("SKU001");
        inventory.setDamaged(true);
        List<Object> inventoryList = Collections.singletonList(inventory);

        when(inventoryService.reportDamagedGoods()).thenReturn(inventoryList);

        mockMvc.perform(get("/api/inventory/report/damaged")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU001"));

        verify(inventoryService, times(1)).reportDamagedGoods();
    }

    @Test
    void testReportPendingByAgent() throws Exception {
        Delivery delivery = new Delivery();
        delivery.setId("1");
        delivery.setSku("SKU001");
        delivery.setStatus("PENDING");
        List<Object> deliveries = Collections.singletonList(delivery);

        when(inventoryService.reportPendingByAgent("agent1")).thenReturn(deliveries);

        mockMvc.perform(get("/api/inventory/report/pending/agent1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU001"));

        verify(inventoryService, times(1)).reportPendingByAgent("agent1");
    }
}