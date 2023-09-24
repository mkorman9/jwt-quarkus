package com.github.mkorman9.jwtquarkus.accounts.dto;

public record AuditEvent(
    String action,
    String subject
) {
}
