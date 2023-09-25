package com.github.mkorman9.jwtquarkus.audit;

import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AuditEventsListener {
    @ConsumeEvent(value = AuditEvent.CHANNEL_ADDRESS)
    // @Blocking to use worker thread
    // @RunOnVirtualThread to use virtual thread
    public void onAuditEvent(AuditEvent auditEvent) {
        log.info("[AUDIT] {} {}", auditEvent.action(), auditEvent.subject());
    }
}
