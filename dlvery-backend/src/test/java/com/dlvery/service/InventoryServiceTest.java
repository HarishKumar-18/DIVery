
package com.dlvery.service;

import com.dlvery.model.Delivery;
import com.dlvery.model.Inventory;
import com.dlvery.repository.AuditLogRepository;
import com.dlvery.repository.DeliveryRepository;
import com.dlvery.repository.InventoryRepository;
import com.dlvery.exception.BadRequestException;
import com.dlvery.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private AuditLogRepository auditLogRepository; // Kept for potential future use
    @Mock
    private MultipartFile multipartFile;
    @InjectMocks
    private InventoryServiceImpl inventoryService;
    private Inventory inventory;
    private Delivery delivery;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        inventory.setId("1");
        inventory.setSku("SKU001");
        inventory.setName("Laptop");
        inventory.setCategory("Electronics");
        inventory.setDamaged(false);
        inventory.setPerishable(false);
        inventory.setExpiryDate("N/A");
        inventory.setQuantity(100);
        inventory.setLowStockThreshold(10);

        delivery = new Delivery();
        delivery.setId("d1");
        delivery.setSku("SKU001");
        delivery.setAgentId("agent1");
        delivery.setStatus("PENDING");
        delivery.setCustomerName("John Doe");
        delivery.setAddress("123 Main St");
    }

    @Test
    void addInventorySuccess() {
        when(inventoryRepository.findBySku("SKU001")).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        Inventory result = inventoryService.addInventory(inventory);
        assertEquals("SKU001", result.getSku());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void addInventoryDuplicateSku() {
        when(inventoryRepository.findBySku("SKU001")).thenReturn(Optional.of(inventory));
        assertThrows(BadRequestException.class, () -> inventoryService.addInventory(inventory));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void getAllInventory() {
        when(inventoryRepository.findAll()).thenReturn(Arrays.asList(inventory));
        List<Inventory> result = inventoryService.getAllInventory();
        assertEquals(1, result.size());
        assertEquals("SKU001", result.get(0).getSku());
    }

    @Test
    void updateInventorySuccess() {
        when(inventoryRepository.findById("1")).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        Inventory result = inventoryService.updateInventory("1", inventory);
        assertEquals("SKU001", result.getSku());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void updateInventoryNotFound() {
        when(inventoryRepository.findById("1")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> inventoryService.updateInventory("1", inventory));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void deleteInventorySuccess() {
        when(inventoryRepository.findById("1")).thenReturn(Optional.of(inventory));
        when(deliveryRepository.findBySku("SKU001")).thenReturn(Collections.emptyList());
        inventoryService.deleteInventory("1");
        verify(inventoryRepository).deleteById("1");
    }

    @Test
    void deleteInventoryWithActiveDeliveries() {
        lenient().when(inventoryRepository.findById("1")).thenReturn(Optional.of(inventory));
        when(deliveryRepository.findBySku("SKU001")).thenReturn(Arrays.asList(delivery));
        assertThrows(BadRequestException.class, () -> inventoryService.deleteInventory("1"));
        verify(inventoryRepository, never()).deleteById(any());
    }

    @Test
    void uploadInventoryFileSuccess() throws IOException {
        String csvContent = "sku,name,category,damaged,perishable,expiryDate,quantity,lowStockThreshold\n" +
                "SKU002,Mouse,Electronics,false,false,N/A,50,5";
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(csvContent.getBytes()));
        when(inventoryRepository.findBySku("SKU002")).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        inventoryService.uploadInventoryFile(multipartFile);
        verify(inventoryRepository).save(any());
    }

    @Test
    void assignForDeliverySuccess() {
        when(inventoryRepository.findBySku("SKU001")).thenReturn(Optional.of(inventory));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);
        inventoryService.assignForDelivery("SKU001", 10, "agent1", "John Doe", "123 Main St");
        verify(inventoryRepository).save(inventory);
        verify(deliveryRepository).save(any(Delivery.class));
        assertEquals(90, inventory.getQuantity());
    }

    @Test
    void assignForDeliveryNotFound() {
        when(inventoryRepository.findBySku("SKU001")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () ->
                inventoryService.assignForDelivery("SKU001", 10, "agent1", "John Doe", "123 Main St"));
        verify(deliveryRepository, never()).save(any());
    }

    @Test
    void trackDeliveryBySku() {
        when(deliveryRepository.findBySku("SKU001")).thenReturn(Arrays.asList(delivery));
        List<Object> result = inventoryService.trackDeliveryBySku("SKU001");
        assertEquals(1, result.size());
        verify(deliveryRepository).findBySku("SKU001");
    }

    @Test
    void trackDeliveryByAgent() {
        when(deliveryRepository.findByAgentId("agent1")).thenReturn(Arrays.asList(delivery));
        List<Object> result = inventoryService.trackDeliveryByAgent("agent1");
        assertEquals(1, result.size());
        verify(deliveryRepository).findByAgentId("agent1");
    }

    @Test
    void reportDeliveredGoods() {
        when(deliveryRepository.findByStatusAndDeliveryDateBetween("DELIVERED", "2025-06-01", "2025-06-30"))
                .thenReturn(Arrays.asList(delivery));
        List<Object> result = inventoryService.reportDeliveredGoods("2025-06-01", "2025-06-30");
        assertEquals(1, result.size());
        verify(deliveryRepository).findByStatusAndDeliveryDateBetween("DELIVERED", "2025-06-01", "2025-06-30");
    }

    @Test
    void reportDamagedGoods() {
        when(deliveryRepository.findByStatus("DAMAGED")).thenReturn(Arrays.asList(delivery));
        List<Object> result = inventoryService.reportDamagedGoods();
        assertEquals(1, result.size());
        verify(deliveryRepository).findByStatus("DAMAGED");
    }

    @Test
    void reportPendingByAgent() {
        when(deliveryRepository.findByAgentIdAndStatus("agent1", "PENDING")).thenReturn(Arrays.asList(delivery));
        List<Object> result = inventoryService.reportPendingByAgent("agent1");
        assertEquals(1, result.size());
        verify(deliveryRepository).findByAgentIdAndStatus("agent1", "PENDING");
    }
}
