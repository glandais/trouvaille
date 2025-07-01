package io.github.glandais.trouvaille.resource;

import io.github.glandais.trouvaille.api.ApiApi;
import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.config.FrontConfig;
import io.github.glandais.trouvaille.service.AnnonceService;
import io.github.glandais.trouvaille.service.AuthService;
import io.github.glandais.trouvaille.service.PhotoService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import java.io.File;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Authenticated
public class ApiResourcempl implements ApiApi {

  final AnnonceService annonceService;
  final PhotoService photoService;
  final AuthService authService;
  final FrontConfig frontConfig;

  @Override
  public Response listAnnonces(AnnonceSearch data) {
    return Response.ok(annonceService.listAnnonces(data)).build();
  }

  @Override
  public Response countAnnonces(AnnonceSearch data) {
    return Response.ok(annonceService.countAnnonces(data)).build();
  }

  @Override
  public Response createAnnonce(AnnonceBase data) {
    return Response.status(Response.Status.CREATED)
        .entity(annonceService.createAnnonce(data))
        .build();
  }

  @Override
  public Response getAnnonce(String id) {
    return Response.ok(annonceService.getAnnonce(id)).build();
  }

  @Override
  public Response putAnnonce(String id, AnnonceWithStatut data) {
    return Response.ok(annonceService.putAnnonce(id, data)).build();
  }

  @Override
  public Response deleteAnnonce(String id) {
    annonceService.deleteAnnonce(id);
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @Override
  public Response createPhoto(File data) {
    return Response.ok(photoService.createPhoto(data)).build();
  }

  @Override
  public Response deletePhoto(String photoId) {
    photoService.deletePhoto(photoId);
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @Override
  public Response getPhotoFull(String photoId) {
    return Response.ok(photoService.getPhotoFull(photoId)).build();
  }

  @Override
  public Response getPhotoThumb(String photoId) {
    return Response.ok(photoService.getPhotoThumb(photoId)).build();
  }

  @Override
  @PermitAll
  public Response exchangeOAuthToken(OAuthTokenRequest data) {
    OAuthTokenResponse oAuthTokenResponse = authService.exchangeOAuthToken(data);
    NewCookie authCookie =
        new NewCookie.Builder("auth_token")
            .value(oAuthTokenResponse.getAccessToken())
            .path("/api")
            .httpOnly(true)
            .secure(true)
            .sameSite(NewCookie.SameSite.STRICT)
            .maxAge(86400)
            .build();
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
}
