package io.github.glandais.trouvaille.client.imgproxy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class ImgProxyService {

  @Inject @RestClient ImgProxyClient imgProxyClient;

  public Response getPhoto(String accept, String fullPath, Integer width, Integer height) {
    return imgProxyClient.getImage(accept, width, height, "local:///" + fullPath);
  }

  public InputStream getPhotoContent(
      String accept, String fullPath, Integer width, Integer height) {
    return imgProxyClient.getImageContent(accept, width, height, "local:///" + fullPath);
  }
}
