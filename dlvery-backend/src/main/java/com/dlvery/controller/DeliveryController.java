//package com.dlvery.controller;
//
//import com.dlvery.model.Delivery;
//import com.dlvery.service.DeliveryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/delivery")
//public class DeliveryController {
//    @Autowired
//    private DeliveryService service;
//
//    @PostMapping
//    public Delivery addDelivery(@RequestBody Delivery delivery) {
//        return service.addDelivery(delivery);
//    }
//
//    @GetMapping("/agent/{agentId}")
//    public List<Delivery> getDeliveriesByAgent(@PathVariable String agentId) {
//        return service.getDeliveriesByAgent(agentId);
//    }
//
//    @PutMapping("/{id}")
//    public Delivery updateDelivery(@PathVariable String id, @RequestBody Delivery delivery) {
//        return service.updateDelivery(id, delivery);
//    }
//}
package com.dlvery.controller;

import com.dlvery.model.Delivery;
import com.dlvery.service.DeliveryService;
import com.dlvery.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {
    @Autowired
    private DeliveryService service;

    @PostMapping
    public ResponseEntity<?> addDelivery(@Valid @RequestBody Delivery delivery, Authentication auth) {
        try {
            return ResponseEntity.ok(service.addDelivery(delivery, auth.getName()));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/agent")
    public ResponseEntity<List<Delivery>> getDeliveriesByAgent(Authentication auth) {
        return ResponseEntity.ok(service.getDeliveriesByAgent(auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDelivery(@PathVariable String id, @Valid @RequestBody Delivery delivery, Authentication auth) {
        try {
            return ResponseEntity.ok(service.updateDelivery(id, delivery, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}