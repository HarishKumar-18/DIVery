package com.dlvery.util;

import com.dlvery.model.Inventory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CsvUtil {
    public static List<Inventory> parseInventoryCsv(MultipartFile file) throws Exception {
        List<Inventory> inventories = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withHeader("sku", "name", "category", "damaged", "perishable", "expiryDate", "quantity", "lowStockThreshold")
                    .withIgnoreHeaderCase()
                    .withTrim());
            for (CSVRecord record : csvParser) {
                Inventory inventory = new Inventory();
                inventory.setId(UUID.randomUUID().toString());
                inventory.setSku(record.get("sku"));
                inventory.setName(record.get("name"));
                inventory.setCategory(record.get("category"));
                inventory.setDamaged(Boolean.parseBoolean(record.get("damaged")));
                inventory.setPerishable(Boolean.parseBoolean(record.get("perishable")));
                inventory.setExpiryDate(record.get("expiryDate"));
                inventory.setQuantity(Integer.parseInt(record.get("quantity")));
                inventory.setLowStockThreshold(Integer.parseInt(record.get("lowStockThreshold")));
                inventories.add(inventory);
            }
            return inventories;
        } catch (Exception e) {
            throw new Exception("Failed to parse CSV: " + e.getMessage());
        }
    }
}