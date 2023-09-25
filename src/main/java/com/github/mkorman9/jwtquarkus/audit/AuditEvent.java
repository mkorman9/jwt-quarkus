package com.github.mkorman9.jwtquarkus.audit;

public record AuditEvent(
    String action,
    String subject
) {
    public static final String CHANNEL_ADDRESS = "audit";
}
