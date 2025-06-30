package io.github.glandais.trouvaille.resource;

import io.github.glandais.trouvaille.openapi.ApiResource;
import io.github.glandais.trouvaille.openapi.beans.*;
import io.github.glandais.trouvaille.service.AnnonceService;
import io.github.glandais.trouvaille.service.AuthService;
import io.github.glandais.trouvaille.service.PhotoService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Authenticated
public class ApiResourcempl implements ApiResource {

  final AnnonceService annonceService;
  final PhotoService photoService;
  final AuthService authService;

  @Override
  public Annonces listAnnonces(AnnonceSearch data) {
    return annonceService.listAnnonces(data);
  }

  @Override
  public BigDecimal countAnnonces(AnnonceSearch data) {
    return BigDecimal.valueOf(annonceService.countAnnonces(data));
  }

  @Override
  public Annonce createAnnonce(AnnonceBase data) {
    return annonceService.createAnnonce(data);
  }

  @Override
  public Annonce getAnnonce(String id) {
    return annonceService.getAnnonce(id);
  }

  @Override
  public Annonce putAnnonce(String id, AnnonceWithStatut data) {
    return annonceService.putAnnonce(id, data);
  }

  @Override
  public void deleteAnnonce(String id) {
    annonceService.deleteAnnonce(id);
  }

  @Override
  public String createPhoto(InputStream data) {
    return photoService.createPhoto(data);
  }

  @Override
  public void deletePhoto(String photoId) {
    photoService.deletePhoto(photoId);
  }

  @Override
  public Response getPhotoFull(String photoId) {
    return photoService.getPhotoFull(photoId);
  }

  @Override
  public Response getPhotoThumb(String photoId) {
    return photoService.getPhotoThumb(photoId);
  }

  @Override
  @PermitAll
  public OAuthTokenResponse exchangeOAuthToken(OAuthTokenRequest data) {
    return authService.exchangeOAuthToken(data);
  }
}
