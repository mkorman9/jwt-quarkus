package com.github.mkorman9.jwtquarkus.accounts.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.mkorman9.jwtquarkus.oauth.dto.GithubUserInfo;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@Slf4j
public class AccountService {
    private final Map<UUID, Boolean> ACCOUNTS = new ConcurrentHashMap<>();
    private final Map<Long, UUID> GITHUB_CONNECTIONS = new ConcurrentHashMap<>();

    public UUID registerAccount() {
        var account = UuidCreator.getTimeOrderedEpoch();
        ACCOUNTS.put(account, true);

        log.info("Registered new account {}", account);

        return account;
    }

    public void connectGithubAccount(GithubUserInfo userInfo, UUID id) {
        log.info("User {} connected GitHub account {} ({})", id, userInfo.name(), userInfo.email());

        GITHUB_CONNECTIONS.put(userInfo.id(), id);
    }

    public UUID getByGithubId(long githubId) {
        return GITHUB_CONNECTIONS.get(githubId);
    }
}
