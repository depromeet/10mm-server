package com.depromeet.global.security;

import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

public class CustomAccessTokenResponseClient
        implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final DefaultAuthorizationCodeTokenResponseClient delegate =
            new DefaultAuthorizationCodeTokenResponseClient();

    public CustomAccessTokenResponseClient(
            CustomRequestEntityConverterV2 customRequestEntityConverterV2) {
        delegate.setRequestEntityConverter(customRequestEntityConverterV2);
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(
            OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        return delegate.getTokenResponse(authorizationGrantRequest);
    }
}
