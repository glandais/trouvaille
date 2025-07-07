package io.github.glandais.trouvaille.auth;

import io.vertx.core.http.Cookie;
import jakarta.ws.rs.core.NewCookie;

/**
 * Utilitaires pour la gestion des cookies d'authentification
 */
public class AuthCookieUtils {

  public static final String COOKIE_NAME = "auth_token";
  public static final String COOKIE_PATH = "/api";
  public static final int COOKIE_MAX_AGE_SECONDS = 86400; // 24 heures

  /**
   * Crée un cookie d'authentification avec le token JWT
   */
  public static NewCookie createAuthCookie(String token) {
    return new NewCookie.Builder(COOKIE_NAME)
        .value(token)
        .path(COOKIE_PATH)
        .httpOnly(true)
        .secure(true)
        .sameSite(NewCookie.SameSite.STRICT)
        .maxAge(COOKIE_MAX_AGE_SECONDS)
        .build();
  }

  /**
   * Crée un cookie d'authentification expiré pour supprimer le cookie existant
   */
  public static NewCookie createExpiredAuthCookie() {
    return new NewCookie.Builder(COOKIE_NAME)
        .value("")
        .path(COOKIE_PATH)
        .httpOnly(true)
        .secure(true)
        .sameSite(NewCookie.SameSite.STRICT)
        .maxAge(0) // Expire immédiatement
        .build();
  }

  /**
   * Crée un cookie Vert.x expiré pour supprimer le cookie existant
   */
  public static Cookie createExpiredVertxCookie() {
    return Cookie.cookie(COOKIE_NAME, "")
        .setPath(COOKIE_PATH)
        .setHttpOnly(true)
        .setSecure(true)
        .setSameSite(io.vertx.core.http.CookieSameSite.STRICT)
        .setMaxAge(0); // Expire immédiatement
  }
}
