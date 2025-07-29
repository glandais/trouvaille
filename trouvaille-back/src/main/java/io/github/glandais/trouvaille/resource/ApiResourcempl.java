package io.github.glandais.trouvaille.resource;

import io.github.glandais.trouvaille.api.ApiApi;
import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.auth.AuthCookieUtils;
import io.github.glandais.trouvaille.config.FrontConfig;
import io.github.glandais.trouvaille.service.AnnonceService;
import io.github.glandais.trouvaille.service.AuthService;
import io.github.glandais.trouvaille.service.PhotoService;
import io.github.glandais.trouvaille.service.TagService;
import io.github.glandais.trouvaille.service.UserService;
import io.quarkus.security.Authenticated;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.core.*;
import java.io.File;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Authenticated
public class ApiResourcempl implements ApiApi {

  final AnnonceService annonceService;
  final PhotoService photoService;
  final AuthService authService;
  final UserService userService;
  final TagService tagService;
  final FrontConfig frontConfig;

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
  public Response getPhoto(String photoId, Integer width, Integer height, String accept) {
    return photoService.getPhoto(photoId, width, height, accept);
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

  @Override
  @RolesAllowed("admin")
  public Response listUsers(Integer page, Integer limit, String search) {
    if (page == null) page = 1;
    if (limit == null) limit = 20;

    Response.ResponseBuilder responseBuilder =
        Response.ok(userService.listUsers(page, limit, search));
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  @RolesAllowed("admin")
  public Response updateUserAdmin(String userId, UserAdminUpdate data) {
    Response.ResponseBuilder responseBuilder =
        Response.ok(userService.updateUserAdmin(userId, data.getAdmin()));
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  @RolesAllowed("admin")
  public Response listTags() {
    Response.ResponseBuilder responseBuilder = Response.ok(tagService.getAllTags());
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  @RolesAllowed("admin")
  public Response createTag(TagCreateUpdate data) {
    Response.ResponseBuilder responseBuilder =
        Response.status(Response.Status.CREATED).entity(tagService.createTag(data));
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
  }

  @Override
  @RolesAllowed("admin")
  public Response updateTag(String tagId, TagCreateUpdate data) {
    return tagService
        .updateTag(tagId, data)
        .map(
            tag -> {
              Response.ResponseBuilder responseBuilder = Response.ok(tag);
              setAuthCookieIfFromHeader(responseBuilder);
              return responseBuilder.build();
            })
        .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  @Override
  public Response getAvailableTags() {
    Response.ResponseBuilder responseBuilder = Response.ok(tagService.getAllActiveTags());
    setAuthCookieIfFromHeader(responseBuilder);
    return responseBuilder.build();
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
}
