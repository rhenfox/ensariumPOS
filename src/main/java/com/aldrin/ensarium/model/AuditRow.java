package com.aldrin.ensarium.model;

import java.sql.Timestamp;

public record AuditRow(int id, String actorUsername, String actorFullName, String actionCode, String details, Timestamp createdAt) {}
