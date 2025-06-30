package io.github.glandais.trouvaille.resource;

import io.github.glandais.trouvaille.api.ApiApi;
import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.config.FrontConfig;
import io.github.glandais.trouvaille.service.AnnonceService;
import io.github.glandais.trouvaille.service.AuthService;
import io.github.glandais.trouvaille.service.PhotoService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
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
  public Annonces listAnnonces(AnnonceSearch data) {
    return annonceService.listAnnonces(data);
  }

  @Override
  public Integer countAnnonces(AnnonceSearch data) {
    return annonceService.countAnnonces(data);
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
  public String createPhoto(File data) {
    return photoService.createPhoto(data);
  }

  @Override
  public void deletePhoto(String photoId) {
    photoService.deletePhoto(photoId);
  }

  @Override
  public File getPhotoFull(String photoId) {
    return photoService.getPhotoFull(photoId);
  }

  @Override
  public File getPhotoThumb(String photoId) {
    return photoService.getPhotoThumb(photoId);
  }

  @Override
  @PermitAll
  public OAuthTokenResponse exchangeOAuthToken(OAuthTokenRequest data) {
    return authService.exchangeOAuthToken(data);
  }

  @Override
  @PermitAll
  public FrontConfiguration getConfig() {
    FrontConfiguration configuration = new FrontConfiguration();
    configuration.setAuthorizeUri(frontConfig.authorizeUri());
    configuration.setClientId(frontConfig.clientId());
    return configuration;
  }
}
