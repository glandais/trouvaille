package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.client.OAuth2Client;
import io.github.glandais.trouvaille.client.dto.TokenResponse;
import io.github.glandais.trouvaille.client.dto.User;
import io.github.glandais.trouvaille.config.OAuthConfig;
import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.openapi.beans.OAuthTokenRequest;
import io.github.glandais.trouvaille.openapi.beans.OAuthTokenResponse;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class AuthService {

    @Inject
    UserService userService;

    @Inject
    OAuthConfig oauthConfig;

    @Inject
    @RestClient
    OAuth2Client mattermostClient;

    public OAuthTokenResponse exchangeOAuthToken(OAuthTokenRequest request) {
        log.info("Exchanging OAuth code for token");

        try {
            // Exchange code for access token
            TokenResponse tokenResponse = mattermostClient.exchangeToken(
                    "authorization_code",
                    oauthConfig.clientId(),
                    oauthConfig.clientSecret(),
                    request.getRedirectUri(),
                    request.getCode()
            );

            // Get user info
            User user = mattermostClient.getCurrentUser(
                    "Bearer " + tokenResponse.getAccessToken()
            );

            UserEntity userEntity = userService.getUserEntity(user.getId(), user.getUsername(), user.getNickname());

            // Create JWT token for our application
            String jwtToken = Jwt.issuer("trouvaille")
                    .upn(user.getUsername())
                    .groups(Set.of("user"))
                    .claim("sub", userEntity.getId())
                    .claim("externalId", userEntity.getExternalId())
                    .claim("username", userEntity.getUsername())
                    .claim("nickname", userEntity.getNickname())
                    .expiresIn(Duration.ofHours(24))
                    .sign();

            OAuthTokenResponse response = new OAuthTokenResponse();
            response.setAccessToken(jwtToken);

            log.info("OAuth token exchange successful for user: {}", user.getUsername());
            return response;

        } catch (Exception e) {
            log.error("Failed to exchange OAuth token", e);
            throw new RuntimeException("OAuth token exchange failed", e);
        }
    }

}