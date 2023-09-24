package com.github.mkorman9.jwtquarkus.accounts.dto;

public record AuditEvent(
    String action,
    String subject
) {
    public static final String CHANNEL_ADDRESS = "audit";
}
