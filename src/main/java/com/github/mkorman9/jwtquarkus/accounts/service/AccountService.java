package com.github.mkorman9.jwtquarkus.accounts.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.mkorman9.jwtquarkus.accounts.dto.AuditEvent;
import com.github.mkorman9.jwtquarkus.oauth.dto.GithubUserInfo;
import io.vertx.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AccountService {
    private final Map<UUID, Boolean> ACCOUNTS = new ConcurrentHashMap<>();
    private final Map<Long, UUID> GITHUB_CONNECTIONS = new ConcurrentHashMap<>();

    @Inject
    EventBus eventBus;

    public UUID registerAccount() {
        var account = UuidCreator.getTimeOrderedEpoch();
        ACCOUNTS.put(account, true);

        eventBus.send("audit", new AuditEvent("REGISTER_ACCOUNT", account.toString()));

        return account;
    }

    public void connectGithubAccount(GithubUserInfo userInfo, UUID id) {
        eventBus.send("audit", new AuditEvent("CONNECT_GITHUB_ACCOUNT", id.toString()));

        GITHUB_CONNECTIONS.put(userInfo.id(), id);
    }

    public UUID getByGithubId(long githubId) {
        return GITHUB_CONNECTIONS.get(githubId);
    }
}
