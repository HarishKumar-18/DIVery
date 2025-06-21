//package com.dlvery.service;
//
//import com.dlvery.model.Inventory;
//import com.dlvery.repository.InventoryRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class InventoryService {
//    @Autowired
//    private InventoryRepository repository;
//
//    public Inventory addInventory(Inventory inventory) {
//        return repository.save(inventory);
//    }
//
//    public List<Inventory> getAllInventory() {
//        return repository.findAll();
//    }
//
//    public Inventory updateInventory(String id, Inventory inventory) {
//        inventory.setId(id);
//        return repository.save(inventory);
//    }
//}
package com.dlvery.service;

import com.dlvery.model.Inventory;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface InventoryService {
    Inventory addInventory(Inventory item);
    List<Inventory> getAllInventory();
    Inventory updateInventory(String id, Inventory item);
    void deleteInventory(String id);
    void uploadInventoryFile(MultipartFile file);
    void assignForDelivery(String sku, int quantity, String agentId, String customerName, String address);
    List<Object> trackDeliveryBySku(String sku);
    List<Object> trackDeliveryByAgent(String agentId);
    List<Object> reportDeliveredGoods(String startDate, String endDate);
    List<Object> reportDamagedGoods();
    List<Object> reportPendingByAgent(String agentId);
}