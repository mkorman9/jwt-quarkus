package com.github.mkorman9.jwtquarkus.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URI;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OauthAuthorization {
    private URI url;

    private OauthState state;
}
