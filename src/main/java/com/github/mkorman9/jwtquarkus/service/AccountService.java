package com.github.mkorman9.jwtquarkus.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AccountService {
    private final Map<UUID, Boolean> ACCOUNTS = new ConcurrentHashMap<>();
    private final Map<Long, UUID> GITHUB_CONNECTIONS = new ConcurrentHashMap<>();

    public UUID registerAccount() {
        var account = UUID.randomUUID();
        ACCOUNTS.put(account, true);
        return account;
    }

    public boolean doesAccountExist(UUID id) {
        return ACCOUNTS.containsKey(id);
    }

    public void connectAccount(long githubId, UUID id) {
        GITHUB_CONNECTIONS.put(githubId, id);
    }

    public UUID getByGithubId(long githubId) {
        return GITHUB_CONNECTIONS.get(githubId);
    }
}
