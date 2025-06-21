package com.dlvery.repository;

import com.dlvery.model.Delivery;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DeliveryRepository extends MongoRepository<Delivery, String> {
    List<Delivery> findBySku(String sku);
    List<Delivery> findByStatusAndDeliveryDateBetween(String status, String startDate, String endDate);
    List<Delivery> findByStatus(String status);
    List<Delivery> findByAgentIdAndStatus(String agentId, String status);

    List<Delivery> findByAgentId(String agentId);
}