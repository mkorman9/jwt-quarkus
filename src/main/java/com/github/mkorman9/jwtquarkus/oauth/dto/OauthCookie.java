package com.github.mkorman9.jwtquarkus.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OauthCookie {
    private String cookie;

    private String cookieHash;
}
