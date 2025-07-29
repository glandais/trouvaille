package io.github.glandais.trouvaille.client.imgproxy;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "imgproxy")
@Path("/")
public interface ImgProxyClient {

  @GET
  @Path("/insecure/rs:fit:{width}:{height}/plain/{path}")
  Response getImage(
      @HeaderParam("Accept") @NotNull String accept,
      @PathParam("width") int width,
      @PathParam("height") int height,
      @PathParam("path") @Encoded String path);

  @GET
  @Path("/insecure/rs:fit:{width}:{height}/plain/{path}")
  InputStream getImageContent(
      @HeaderParam("Accept") @NotNull String accept,
      @PathParam("width") int width,
      @PathParam("height") int height,
      @PathParam("path") @Encoded String path);
}
