package com.github.mkorman9.jwtquarkus.oauth;

import lombok.Builder;

import java.net.URI;

@Builder
public record OauthTicket(
    URI url,
    OauthState state
) {
}
