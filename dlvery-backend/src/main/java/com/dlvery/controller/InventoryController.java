
package com.dlvery.controller;

import com.dlvery.model.Inventory;
import com.dlvery.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@PreAuthorize("hasAnyRole('INVENTORY', 'ADMIN')")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<Inventory> addInventory(@RequestBody Inventory item) {
        return ResponseEntity.ok(inventoryService.addInventory(item));
    }

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable String id, @RequestBody Inventory item) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable String id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadInventoryFile(@RequestParam("file") MultipartFile file) {
        inventoryService.uploadInventoryFile(file);
        return ResponseEntity.ok("Inventory uploaded successfully");
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignForDelivery(
            @RequestParam String sku,
            @RequestParam int quantity,
            @RequestParam String agentId,
            @RequestParam String customerName,
            @RequestParam String address) {
        inventoryService.assignForDelivery(sku, quantity, agentId, customerName, address);
        return ResponseEntity.ok("Assigned for delivery");
    }

    @GetMapping("/track/sku/{sku}")
    public ResponseEntity<List<Object>> trackDeliveryBySku(@PathVariable String sku) {
        return ResponseEntity.ok(inventoryService.trackDeliveryBySku(sku));
    }

    @GetMapping("/track/agent/{agentId}")
    public ResponseEntity<List<Object>> trackDeliveryByAgent(@PathVariable String agentId) {
        return ResponseEntity.ok(inventoryService.trackDeliveryByAgent(agentId));
    }

    @GetMapping("/report/delivered")
    public ResponseEntity<List<Object>> reportDeliveredGoods(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(inventoryService.reportDeliveredGoods(startDate, endDate));
    }

    @GetMapping("/report/damaged")
    public ResponseEntity<List<Object>> reportDamagedGoods() {
        return ResponseEntity.ok(inventoryService.reportDamagedGoods());
    }

    @GetMapping("/report/pending/{agentId}")
    public ResponseEntity<List<Object>> reportPendingByAgent(@PathVariable String agentId) {
        return ResponseEntity.ok(inventoryService.reportPendingByAgent(agentId));
    }
}