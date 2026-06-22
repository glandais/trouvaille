package io.github.glandais.trouvaille.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.smallrye.jwt.runtime.auth.JsonWebTokenCredential;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookieJwtHttpAuthenticationMechanismTest {

  @Mock private RoutingContext routingContext;

  @Mock private IdentityProviderManager identityProviderManager;

  @Mock private HttpServerRequest request;

  @Mock private HttpServerResponse response;

  @Mock private Cookie authCookie;

  @Mock private SecurityIdentity securityIdentity;

  private CookieJwtHttpAuthenticationMechanism mechanism;

  @BeforeEach
  void setUp() {
    mechanism = new CookieJwtHttpAuthenticationMechanism();
    lenient().when(routingContext.request()).thenReturn(request);
    lenient().when(routingContext.response()).thenReturn(response);
  }

  @Test
  void testAuthenticate_WithValidAuthorizationHeader() {
    // Given
    String token = "valid.jwt.token";
    String authHeader = "Bearer " + token;
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(identityProviderManager.authenticate(any(TokenAuthenticationRequest.class)))
        .thenReturn(Uni.createFrom().item(securityIdentity));

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    SecurityIdentity identity = result.await().indefinitely();
    assertEquals(securityIdentity, identity);

    // Verify the correct token was used
    ArgumentCaptor<TokenAuthenticationRequest> captor =
        ArgumentCaptor.forClass(TokenAuthenticationRequest.class);
    verify(identityProviderManager).authenticate(captor.capture());
    JsonWebTokenCredential credential = (JsonWebTokenCredential) captor.getValue().getToken();
    assertEquals(token, credential.getToken());
  }

  @Test
  void testAuthenticate_WithValidCookie() {
    // Given
    String token = "cookie.jwt.token";
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getCookie(AuthCookieUtils.COOKIE_NAME)).thenReturn(authCookie);
    when(authCookie.getValue()).thenReturn(token);
    when(identityProviderManager.authenticate(any(TokenAuthenticationRequest.class)))
        .thenReturn(Uni.createFrom().item(securityIdentity));

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    SecurityIdentity identity = result.await().indefinitely();
    assertEquals(securityIdentity, identity);

    // Verify the correct token was used
    ArgumentCaptor<TokenAuthenticationRequest> captor =
        ArgumentCaptor.forClass(TokenAuthenticationRequest.class);
    verify(identityProviderManager).authenticate(captor.capture());
    JsonWebTokenCredential credential = (JsonWebTokenCredential) captor.getValue().getToken();
    assertEquals(token, credential.getToken());
  }

  @Test
  void testAuthenticate_NoTokenFound() {
    // Given
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getCookie(AuthCookieUtils.COOKIE_NAME)).thenReturn(null);

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    SecurityIdentity identity = result.await().indefinitely();
    assertNull(identity);
    verify(identityProviderManager, never()).authenticate(any());
  }

  @Test
  void testAuthenticate_EmptyAuthorizationHeader() {
    // Given
    when(request.getHeader("Authorization")).thenReturn("");
    when(request.getCookie(AuthCookieUtils.COOKIE_NAME)).thenReturn(null);

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    SecurityIdentity identity = result.await().indefinitely();
    assertNull(identity);
    verify(identityProviderManager, never()).authenticate(any());
  }

  @Test
  void testAuthenticate_MalformedAuthorizationHeader() {
    // Given
    when(request.getHeader("Authorization")).thenReturn("Basic username:password");
    when(request.getCookie(AuthCookieUtils.COOKIE_NAME)).thenReturn(null);

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    SecurityIdentity identity = result.await().indefinitely();
    assertNull(identity);
    verify(identityProviderManager, never()).authenticate(any());
  }

  @Test
  void testAuthenticate_EmptyCookieValue() {
    // Given
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getCookie(AuthCookieUtils.COOKIE_NAME)).thenReturn(authCookie);
    when(authCookie.getValue()).thenReturn("");

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    SecurityIdentity identity = result.await().indefinitely();
    assertNull(identity);
    verify(identityProviderManager, never()).authenticate(any());
  }

  @Test
  void testAuthenticate_NullCookieValue() {
    // Given
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getCookie(AuthCookieUtils.COOKIE_NAME)).thenReturn(authCookie);
    when(authCookie.getValue()).thenReturn(null);

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    SecurityIdentity identity = result.await().indefinitely();
    assertNull(identity);
    verify(identityProviderManager, never()).authenticate(any());
  }

  @Test
  void testAuthenticate_InvalidTokenRemovesCookie() {
    // Given
    String token = "invalid.jwt.token";
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getCookie(AuthCookieUtils.COOKIE_NAME)).thenReturn(authCookie);
    when(authCookie.getValue()).thenReturn(token);
    when(identityProviderManager.authenticate(any(TokenAuthenticationRequest.class)))
        .thenReturn(Uni.createFrom().failure(new RuntimeException("Invalid token")));

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    SecurityIdentity identity = result.await().indefinitely();
    assertNull(identity);

    // Verify cookie removal
    ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
    verify(response).addCookie(cookieCaptor.capture());
    Cookie expiredCookie = cookieCaptor.getValue();
    assertEquals(AuthCookieUtils.COOKIE_NAME, expiredCookie.getName());
    assertEquals("", expiredCookie.getValue());
    assertEquals(0, expiredCookie.getMaxAge());
  }

  @Test
  void testAuthenticate_AuthorizationHeaderTakesPrecedenceOverCookie() {
    // Given
    String headerToken = "header.jwt.token";
    String cookieToken = "cookie.jwt.token";
    String authHeader = "Bearer " + headerToken;

    when(request.getHeader("Authorization")).thenReturn(authHeader);
    // Cookie stubbing not needed since header takes precedence
    when(identityProviderManager.authenticate(any(TokenAuthenticationRequest.class)))
        .thenReturn(Uni.createFrom().item(securityIdentity));

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    SecurityIdentity identity = result.await().indefinitely();
    assertEquals(securityIdentity, identity);

    // Verify the header token was used, not the cookie token
    ArgumentCaptor<TokenAuthenticationRequest> captor =
        ArgumentCaptor.forClass(TokenAuthenticationRequest.class);
    verify(identityProviderManager).authenticate(captor.capture());
    JsonWebTokenCredential credential = (JsonWebTokenCredential) captor.getValue().getToken();
    assertEquals(headerToken, credential.getToken());
  }

  @Test
  void testAuthenticate_BearerTokenWithSpaces() {
    // Given
    String token = "token.with.spaces";
    String authHeader = "Bearer   " + token + "   ";
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(identityProviderManager.authenticate(any(TokenAuthenticationRequest.class)))
        .thenReturn(Uni.createFrom().item(securityIdentity));

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    SecurityIdentity identity = result.await().indefinitely();
    assertEquals(securityIdentity, identity);

    // Verify the token was trimmed
    ArgumentCaptor<TokenAuthenticationRequest> captor =
        ArgumentCaptor.forClass(TokenAuthenticationRequest.class);
    verify(identityProviderManager).authenticate(captor.capture());
    JsonWebTokenCredential credential = (JsonWebTokenCredential) captor.getValue().getToken();
    assertEquals(token, credential.getToken());
  }

  @Test
  void testGetChallenge() {
    // When
    Uni<ChallengeData> result = mechanism.getChallenge(routingContext);

    // Then
    ChallengeData challenge = result.await().indefinitely();
    assertEquals(401, challenge.status);
    assertEquals("Bearer", challenge.getHeaders().get("WWW-Authenticate"));
  }

  @Test
  void testGetCredentialTypes() {
    // When
    Set<Class<? extends AuthenticationRequest>> types = mechanism.getCredentialTypes();

    // Then
    assertEquals(1, types.size());
    assertTrue(types.contains(TokenAuthenticationRequest.class));
  }

  @Test
  void testGetCredentialTransport() {
    // When
    Uni<HttpCredentialTransport> result = mechanism.getCredentialTransport(routingContext);

    // Then
    HttpCredentialTransport transport = result.await().indefinitely();
    assertEquals(HttpCredentialTransport.Type.AUTHORIZATION, transport.getTransportType());
    // Note: getTransportName() method may not exist, just verify the type
    assertNotNull(transport);
  }

  @Test
  void testAuthenticate_InvalidTokenNoCookieToRemove() {
    // Given
    String token = "invalid.jwt.token";
    String authHeader = "Bearer " + token;
    when(request.getHeader("Authorization")).thenReturn(authHeader);
    when(request.getCookie(AuthCookieUtils.COOKIE_NAME)).thenReturn(null);
    when(identityProviderManager.authenticate(any(TokenAuthenticationRequest.class)))
        .thenReturn(Uni.createFrom().failure(new RuntimeException("Invalid token")));

    // When
    Uni<SecurityIdentity> result = mechanism.authenticate(routingContext, identityProviderManager);

    // Then
    SecurityIdentity identity = result.await().indefinitely();
    assertNull(identity);

    // Verify no cookie removal attempt when no cookie exists
    verify(response, never()).addCookie(any());
  }
}
