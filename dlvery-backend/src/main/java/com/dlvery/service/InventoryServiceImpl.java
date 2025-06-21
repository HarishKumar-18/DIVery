
package com.dlvery.service;

import com.dlvery.model.Delivery;
import com.dlvery.model.Inventory;
import com.dlvery.repository.DeliveryRepository;
import com.dlvery.repository.InventoryRepository;
import com.dlvery.exception.BadRequestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Override
    public Inventory addInventory(Inventory item) {
        if (inventoryRepository.findBySku(item.getSku()).isPresent()) {
            throw new BadRequestException("SKU already exists");
        }
        return inventoryRepository.save(item);
    }

    @Override
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Override
    public Inventory updateInventory(String id, Inventory item) {
        Optional<Inventory> existingItem = inventoryRepository.findById(id);
        if (!existingItem.isPresent()) {
            throw new BadRequestException("Inventory item not found");
        }
        Inventory updatedItem = existingItem.get();
        updatedItem.setSku(item.getSku());
        updatedItem.setName(item.getName());
        updatedItem.setCategory(item.getCategory());
        updatedItem.setDamaged(item.isDamaged());
        updatedItem.setPerishable(item.isPerishable());
        updatedItem.setExpiryDate(item.getExpiryDate());
        updatedItem.setQuantity(item.getQuantity());
        updatedItem.setLowStockThreshold(item.getLowStockThreshold());
        return inventoryRepository.save(updatedItem);
    }

    @Override
    public void deleteInventory(String id) {
        Optional<Inventory> existingItem = inventoryRepository.findById(id);
        if (!existingItem.isPresent()) {
            throw new BadRequestException("Inventory item not found");
        }
        Inventory inventory = existingItem.get();
        List<Delivery> deliveries = deliveryRepository.findBySku(inventory.getSku());
        if (!deliveries.isEmpty()) {
            throw new BadRequestException("Cannot delete inventory with active deliveries");
        }
        inventoryRepository.deleteById(id);
    }

    @Override
    public void uploadInventoryFile(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : csvParser) {
                Inventory item = new Inventory();
                item.setSku(record.get("sku"));
                item.setName(record.get("name"));
                item.setCategory(record.get("category"));
                item.setDamaged(Boolean.parseBoolean(record.get("damaged")));
                item.setPerishable(Boolean.parseBoolean(record.get("perishable")));
                item.setExpiryDate(record.get("expiryDate"));
                item.setQuantity(Integer.parseInt(record.get("quantity")));
                item.setLowStockThreshold(Integer.parseInt(record.get("lowStockThreshold")));
                if (!inventoryRepository.findBySku(item.getSku()).isPresent()) {
                    inventoryRepository.save(item);
                }
            }
        } catch (Exception e) {
            throw new BadRequestException("Failed to upload inventory file: " + e.getMessage());
        }
    }

    @Override
    public void assignForDelivery(String sku, int quantity, String agentId, String customerName, String address) {
        Optional<Inventory> optionalInventory = inventoryRepository.findBySku(sku);
        if (!optionalInventory.isPresent()) {
            throw new BadRequestException("Inventory item not found for SKU: " + sku);
        }
        Inventory inventory = optionalInventory.get();
        if (inventory.getQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock for SKU: " + sku);
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);

        Delivery delivery = new Delivery();
        delivery.setSku(sku);
        delivery.setQuantity(quantity);
        delivery.setAgentId(agentId);
        delivery.setCustomerName(customerName);
        delivery.setAddress(address);
        delivery.setStatus("PENDING");
        delivery.setDeliveryDate(LocalDate.now().toString());
        delivery.setCreatedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);
    }

    @Override
    public List<Object> trackDeliveryBySku(String sku) {
        return new ArrayList<>(deliveryRepository.findBySku(sku));
    }

    @Override
    public List<Object> trackDeliveryByAgent(String agentId) {
        return new ArrayList<>(deliveryRepository.findByAgentId(agentId));
    }

    @Override
    public List<Object> reportDeliveredGoods(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            return new ArrayList<>(deliveryRepository.findByStatusAndDeliveryDateBetween("DELIVERED", start.toString(), end.toString()));
        } catch (Exception e) {
            throw new BadRequestException("Invalid date format");
        }
    }

    @Override
    public List<Object> reportDamagedGoods() {
        return new ArrayList<>(deliveryRepository.findByStatus("DAMAGED"));
    }

    @Override
    public List<Object> reportPendingByAgent(String agentId) {
        return new ArrayList<>(deliveryRepository.findByAgentIdAndStatus(agentId, "PENDING"));
    }
}