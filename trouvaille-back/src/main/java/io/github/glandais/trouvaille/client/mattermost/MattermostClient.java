package io.github.glandais.trouvaille.client.mattermost;

import io.github.glandais.trouvaille.client.mattermost.dto.CreatePostRequest;
import io.github.glandais.trouvaille.client.mattermost.dto.PostResponse;
import io.github.glandais.trouvaille.client.mattermost.dto.TokenResponse;
import io.github.glandais.trouvaille.client.mattermost.dto.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "oauth2")
@Path("/")
public interface MattermostClient {

  @POST
  @Path("/oauth/access_token")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  TokenResponse exchangeToken(
      @FormParam("grant_type") String grantType,
      @FormParam("client_id") String clientId,
      @FormParam("client_secret") String clientSecret,
      @FormParam("redirect_uri") String redirectUri,
      @FormParam("code") String code);

  @GET
  @Path("/api/v4/users/me")
  @Produces(MediaType.APPLICATION_JSON)
  User getCurrentUser(@HeaderParam("Authorization") String authorization);

  @POST
  @Path("/api/v4/posts")
  @Produces(MediaType.APPLICATION_JSON)
  PostResponse createPost(
      @HeaderParam("Authorization") String authorization, CreatePostRequest post);
}
