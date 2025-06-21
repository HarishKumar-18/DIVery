//package com.dlvery.service;
//
//import com.dlvery.model.Delivery;
//import com.dlvery.repository.DeliveryRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class DeliveryService {
//    @Autowired
//    private DeliveryRepository repository;
//
//    public Delivery addDelivery(Delivery delivery) {
//        return repository.save(delivery);
//    }
//
//    public List<Delivery> getDeliveriesByAgent(String agentId) {
//        return repository.findAll().stream()
//                .filter(d -> d.getAgentId().equals(agentId))
//                .toList();
//    }
//
//    public Delivery updateDelivery(String id, Delivery delivery) {
//        delivery.setId(id);
//        return repository.save(delivery);
//    }
//}
package com.dlvery.service;

import com.dlvery.model.AuditLog;
import com.dlvery.model.Delivery;
import com.dlvery.repository.AuditLogRepository;
import com.dlvery.repository.DeliveryRepository;
import com.dlvery.exception.BadRequestException;
import com.dlvery.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class DeliveryService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);
    private static final List<String> VALID_STATUSES = Arrays.asList("PENDING", "IN_TRANSIT", "DELIVERED", "DOOR_LOCK", "DAMAGED");
    @Autowired
    private DeliveryRepository deliveryRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;

    public Delivery addDelivery(Delivery delivery, String userId) {
        if (!VALID_STATUSES.contains(delivery.getStatus())) {
            throw new BadRequestException("Invalid status: " + delivery.getStatus());
        }
        Delivery saved = deliveryRepository.save(delivery);
        logAudit("ADD", "Delivery", saved.getId(), userId);
        logger.info("Delivery assigned: ID={}, Agent={}", saved.getId(), saved.getAgentId());
        return saved;
    }

    public List<Delivery> getDeliveriesByAgent(String agentId) {
        return deliveryRepository.findByAgentId(agentId);
    }

    public Delivery updateDelivery(String id, Delivery delivery, String userId) {
        Delivery existing = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + id));
        if (!VALID_STATUSES.contains(delivery.getStatus())) {
            throw new BadRequestException("Invalid status: " + delivery.getStatus());
        }
        delivery.setId(id);
        Delivery updated = deliveryRepository.save(delivery);
        logAudit("UPDATE", "Delivery", id, userId);
        logger.info("Delivery updated: ID={}, Status={}", id, delivery.getStatus());
        return updated;
    }

    private void logAudit(String action, String entity, String entityId, String userId) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setUserId(userId);
        log.setTimestamp(LocalDateTime.now().toString());
        auditLogRepository.save(log);
    }
}