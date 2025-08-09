package io.github.glandais.trouvaille.auth;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import jakarta.ws.rs.core.NewCookie;
import org.junit.jupiter.api.Test;

class AuthCookieUtilsTest {

  @Test
  void testCreateAuthCookie_ValidToken() {
    // Given
    String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

    // When
    NewCookie cookie = AuthCookieUtils.createAuthCookie(token);

    // Then
    assertEquals(AuthCookieUtils.COOKIE_NAME, cookie.getName());
    assertEquals(token, cookie.getValue());
    assertEquals(AuthCookieUtils.COOKIE_PATH, cookie.getPath());
    assertTrue(cookie.isHttpOnly());
    assertTrue(cookie.isSecure());
    assertEquals(NewCookie.SameSite.NONE, cookie.getSameSite());
    assertEquals(AuthCookieUtils.COOKIE_MAX_AGE_SECONDS, cookie.getMaxAge());
  }

  @Test
  void testCreateAuthCookie_EmptyToken() {
    // Given
    String token = "";

    // When
    NewCookie cookie = AuthCookieUtils.createAuthCookie(token);

    // Then
    assertEquals(AuthCookieUtils.COOKIE_NAME, cookie.getName());
    assertEquals("", cookie.getValue());
    assertEquals(AuthCookieUtils.COOKIE_PATH, cookie.getPath());
    assertTrue(cookie.isHttpOnly());
    assertTrue(cookie.isSecure());
    assertEquals(NewCookie.SameSite.NONE, cookie.getSameSite());
    assertEquals(AuthCookieUtils.COOKIE_MAX_AGE_SECONDS, cookie.getMaxAge());
  }

  @Test
  void testCreateAuthCookie_NullToken() {
    // Given
    String token = null;

    // When
    NewCookie cookie = AuthCookieUtils.createAuthCookie(token);

    // Then
    assertEquals(AuthCookieUtils.COOKIE_NAME, cookie.getName());
    assertNull(cookie.getValue());
    assertEquals(AuthCookieUtils.COOKIE_PATH, cookie.getPath());
    assertTrue(cookie.isHttpOnly());
    assertTrue(cookie.isSecure());
    assertEquals(NewCookie.SameSite.NONE, cookie.getSameSite());
    assertEquals(AuthCookieUtils.COOKIE_MAX_AGE_SECONDS, cookie.getMaxAge());
  }

  @Test
  void testCreateExpiredVertxCookie() {
    // When
    Cookie cookie = AuthCookieUtils.createExpiredVertxCookie();

    // Then
    assertEquals(AuthCookieUtils.COOKIE_NAME, cookie.getName());
    assertEquals("", cookie.getValue());
    assertEquals(AuthCookieUtils.COOKIE_PATH, cookie.getPath());
    assertTrue(cookie.isHttpOnly());
    assertTrue(cookie.isSecure());
    assertEquals(CookieSameSite.NONE, cookie.getSameSite());
    assertEquals(0, cookie.getMaxAge());
  }

  @Test
  void testCookieConstants() {
    // Verify constants are correctly defined
    assertEquals("auth_token", AuthCookieUtils.COOKIE_NAME);
    assertEquals("/api", AuthCookieUtils.COOKIE_PATH);
    assertEquals(86400, AuthCookieUtils.COOKIE_MAX_AGE_SECONDS); // 24 hours
  }

  @Test
  void testCreateAuthCookie_SecurityProperties() {
    // Given
    String token = "test.token";

    // When
    NewCookie cookie = AuthCookieUtils.createAuthCookie(token);

    // Then - Verify security properties
    assertTrue(cookie.isHttpOnly(), "Cookie should be HttpOnly to prevent XSS");
    assertTrue(cookie.isSecure(), "Cookie should be Secure for HTTPS");
    assertEquals(
        NewCookie.SameSite.NONE,
        cookie.getSameSite(),
        "SameSite should be NONE for cross-origin requests");
  }

  @Test
  void testCreateExpiredVertxCookie_SecurityProperties() {
    // When
    Cookie cookie = AuthCookieUtils.createExpiredVertxCookie();

    // Then - Verify security properties
    assertTrue(cookie.isHttpOnly(), "Expired cookie should be HttpOnly");
    assertTrue(cookie.isSecure(), "Expired cookie should be Secure");
    assertEquals(
        CookieSameSite.NONE, cookie.getSameSite(), "Expired cookie should have SameSite NONE");
    assertEquals(0, cookie.getMaxAge(), "Expired cookie should have MaxAge 0");
  }

  @Test
  void testCreateAuthCookie_LongToken() {
    // Given - A very long token (like a real JWT)
    String longToken =
        "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJyc2ExIn0.eyJleHAiOjE2MzAwNzg0MDAsImlhdCI6MTYzMDA3NDgwMCwiYXV0aF90aW1lIjoxNjMwMDc0ODAwLCJqdGkiOiI5NzhjNWQ4Mi1mYTcyLTQxYjMtYjJmOS1iNmY5M2E2NzBmM2YiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwODAvYXV0aC9yZWFsbXMvbXlyZWFsbSIsImF1ZCI6WyJyZWFsbS1tYW5hZ2VtZW50IiwiYWNjb3VudCJdLCJzdWIiOiJiZjEzYmUyYy00ZGE0LTQ5OGMtOGI5MS0yYjBiMWE4ODVlNzAiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJjbGllbnQtaWQifQ.signature";

    // When
    NewCookie cookie = AuthCookieUtils.createAuthCookie(longToken);

    // Then
    assertEquals(longToken, cookie.getValue(), "Long token should be preserved exactly");
    assertEquals(AuthCookieUtils.COOKIE_NAME, cookie.getName());
  }
}
