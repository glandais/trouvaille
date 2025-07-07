package io.github.glandais.trouvaille.resource;

import io.github.glandais.trouvaille.api.ApiApi;
import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.auth.AuthCookieUtils;
import io.github.glandais.trouvaille.config.FrontConfig;
import io.github.glandais.trouvaille.service.AnnonceService;
import io.github.glandais.trouvaille.service.AuthService;
import io.github.glandais.trouvaille.service.PhotoService;
import io.quarkus.security.Authenticated;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Authenticated
public class ApiResourcempl implements ApiApi {

  final AnnonceService annonceService;
  final PhotoService photoService;
  final AuthService authService;
  final FrontConfig frontConfig;

  @Context Request request;
  @Context RoutingContext routingContext;

  @Override
  public Response listAnnonces(AnnonceSearch data) {
    Response.ResponseBuilder responseBuilder = Response.ok(annonceService.listAnnonces(data));
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  public Response countAnnonces(AnnonceSearch data) {
    Response.ResponseBuilder responseBuilder = Response.ok(annonceService.countAnnonces(data));
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  public Response createAnnonce(AnnonceBase data) {
    Response.ResponseBuilder responseBuilder =
        Response.status(Response.Status.CREATED).entity(annonceService.createAnnonce(data));
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  public Response getAnnonce(String id) {
    Response.ResponseBuilder responseBuilder = Response.ok(annonceService.getAnnonce(id));
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  public Response putAnnonce(String id, AnnonceWithStatut data) {
    Response.ResponseBuilder responseBuilder = Response.ok(annonceService.putAnnonce(id, data));
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  public Response deleteAnnonce(String id) {
    annonceService.deleteAnnonce(id);
    Response.ResponseBuilder responseBuilder = Response.status(Response.Status.NO_CONTENT);
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  public Response createPhoto(File data) {
    Response.ResponseBuilder responseBuilder = Response.ok(photoService.createPhoto(data));
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  public Response deletePhoto(String photoId) {
    photoService.deletePhoto(photoId);
    Response.ResponseBuilder responseBuilder = Response.status(Response.Status.NO_CONTENT);
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  public Response getPhotoFull(String photoId) {
    return buildPhotoResponse(photoService.getPhotoFull(photoId));
  }

  @Override
  public Response getPhotoThumb(String photoId) {
    return buildPhotoResponse(photoService.getPhotoThumb(photoId));
  }

  @Override
  @PermitAll
  public Response exchangeOAuthToken(OAuthTokenRequest data) {
    OAuthTokenResponse oAuthTokenResponse = authService.exchangeOAuthToken(data);
    NewCookie authCookie = AuthCookieUtils.createAuthCookie(oAuthTokenResponse.getAccessToken());
    return Response.ok(oAuthTokenResponse).cookie(authCookie).build();
  }

  @Override
  @PermitAll
  public Response getConfig() {
    FrontConfiguration configuration = new FrontConfiguration();
    configuration.setAuthorizeUri(frontConfig.authorizeUri());
    configuration.setClientId(frontConfig.clientId());
    return Response.ok(configuration).build();
  }

  /**
   * Vérifie si le token JWT vient du header Authorization et non d'un cookie existant,
   * et le définit comme cookie pour les futures requêtes
   */
  private void setAuthCookieIfFromHeader(Response.ResponseBuilder responseBuilder) {
    String authorizationHeader = routingContext.request().getHeader("Authorization");

    // Vérifier si le token vient du header Authorization
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      String token = authorizationHeader.substring("Bearer ".length()).trim();

      // Vérifier si le token n'est pas déjà présent dans un cookie
      Cookie authCookie = routingContext.request().getCookie(AuthCookieUtils.COOKIE_NAME);
      boolean hasAuthCookie = authCookie != null && token.equals(authCookie.getValue());

      // Si le token vient du header et n'est pas déjà dans un cookie, le définir comme cookie
      if (!hasAuthCookie) {
        NewCookie newAuthCookie = AuthCookieUtils.createAuthCookie(token);
        responseBuilder.cookie(newAuthCookie);
      }
    }
  }

  private Response buildPhotoResponse(File photoFile) {
    try {
      BasicFileAttributes attrs =
          Files.readAttributes(photoFile.toPath(), BasicFileAttributes.class);

      // Generate ETag based on file size and last modified time
      String etag = photoFile.length() + "-" + attrs.lastModifiedTime().toMillis();

      // Get last modified date
      Date lastModified = Date.from(attrs.lastModifiedTime().toInstant());

      // Create entity tag for conditional requests
      EntityTag entityTag = new EntityTag(etag);

      // Check conditional requests
      Response.ResponseBuilder builder = request.evaluatePreconditions(lastModified, entityTag);
      if (builder != null) {
        // Return 304 Not Modified if content hasn't changed
        return builder.header("Cache-Control", "private, max-age=86400").build();
      }

      Response.ResponseBuilder responseBuilder =
          Response.ok(photoFile)
              .header("ETag", etag)
              .header(
                  "Last-Modified",
                  DateTimeFormatter.RFC_1123_DATE_TIME.format(
                      lastModified.toInstant().atZone(ZoneId.of("GMT"))))
              .header("Cache-Control", "private, max-age=86400"); // Cache for 24 hours
      setAuthCookieIfFromHeader(responseBuilder);
      return responseBuilder.build();

    } catch (IOException e) {
      // Fallback to simple response if file attributes can't be read
      return Response.ok(photoFile).build();
    }
  }
}
