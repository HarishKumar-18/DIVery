package com.dlvery.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "audit_logs")
@SuppressWarnings("unused")
public class AuditLog {
    @Id
    private String id;
    private String action;
    private String entity;
    private String entityId;
    private String userId;
    private String timestamp;
}