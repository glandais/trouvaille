package io.github.glandais.trouvaille.auth;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.smallrye.jwt.runtime.auth.JsonWebTokenCredential;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.Set;
import org.jboss.logging.Logger;

/**
 * Mécanisme d'authentification HTTP personnalisé qui extrait les tokens JWT
 * depuis les cookies et les headers Authorization.
 */
@ApplicationScoped
@Priority(200) // Priorité plus basse que le mécanisme JWT par défaut
public class CookieJwtHttpAuthenticationMechanism implements HttpAuthenticationMechanism {

  private static final Logger LOG = Logger.getLogger(CookieJwtHttpAuthenticationMechanism.class);
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  @Override
  public Uni<SecurityIdentity> authenticate(
      RoutingContext context, IdentityProviderManager identityProviderManager) {
    String token = extractToken(context);

    if (token == null) {
      LOG.debug("Aucun token JWT trouvé dans la requête");
      return Uni.createFrom().nullItem();
    }

    LOG.debug("Token JWT trouvé, authentification en cours...");

    TokenAuthenticationRequest authRequest =
        new TokenAuthenticationRequest(new JsonWebTokenCredential(token));
    return identityProviderManager
        .authenticate(authRequest)
        .onFailure()
        .recoverWithUni(
            throwable -> {
              LOG.warn("Token JWT invalide, suppression du cookie", throwable);
              removeCookie(context);
              return Uni.createFrom().nullItem();
            });
  }

  @Override
  public Uni<ChallengeData> getChallenge(RoutingContext context) {
    return Uni.createFrom().item(new ChallengeData(401, "WWW-Authenticate", "Bearer"));
  }

  @Override
  public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
    return Collections.singleton(TokenAuthenticationRequest.class);
  }

  @Override
  public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context) {
    return Uni.createFrom()
        .item(
            new HttpCredentialTransport(
                HttpCredentialTransport.Type.AUTHORIZATION, AUTHORIZATION_HEADER));
  }

  /**
   * Extrait le token depuis le header Authorization ou le cookie
   */
  private String extractToken(RoutingContext context) {
    // 1. Essayer d'abord le header Authorization (standard)
    String authHeader = context.request().getHeader(AUTHORIZATION_HEADER);
    if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
      String token = authHeader.substring(BEARER_PREFIX.length()).trim();
      LOG.debug("Token trouvé dans le header Authorization");
      return token;
    }

    // 2. Ensuite essayer le cookie auth_token
    Cookie authCookie = context.request().getCookie(AuthCookieUtils.COOKIE_NAME);
    if (authCookie != null && authCookie.getValue() != null && !authCookie.getValue().isEmpty()) {
      LOG.debug("Token trouvé dans le cookie auth_token");
      return authCookie.getValue();
    }

    return null;
  }

  /**
   * Supprime le cookie d'authentification en cas de token invalide
   */
  private void removeCookie(RoutingContext context) {
    Cookie authCookie = context.request().getCookie(AuthCookieUtils.COOKIE_NAME);
    if (authCookie != null) {
      LOG.debug("Suppression du cookie auth_token invalide");
      context.response().addCookie(AuthCookieUtils.createExpiredVertxCookie());
    }
  }
}
