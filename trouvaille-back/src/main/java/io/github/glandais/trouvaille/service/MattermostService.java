package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.client.MattermostClient;
import io.github.glandais.trouvaille.client.dto.CreatePostRequest;
import io.github.glandais.trouvaille.client.dto.PostResponse;
import io.github.glandais.trouvaille.config.BotConfig;
import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class MattermostService {

  @Inject @RestClient MattermostClient mattermostClient;

  @Inject UserService userService;

  @Inject BotConfig botConfig;

  @ConfigProperty(name = "quarkus.http.cors.origins")
  String frontUrl;

  @Location("post.md")
  Template postTemplate;

  public void createAnnonce(AnnonceEntity annonceEntity) {
    if (!isPublishMattermost()) {
      return;
    }
    CreatePostRequest post = initPost(annonceEntity, true);
    PostResponse postResponse = callCreatePost(post);
    if (postResponse != null) {
      annonceEntity.setMattermostRootId(postResponse.getId());
    }
  }

  public void updateAnnonce(AnnonceEntity annonceEntity) {
    if (!isPublishMattermost()) {
      return;
    }
    CreatePostRequest post = initPost(annonceEntity, false);
    if (annonceEntity.getMattermostRootId() != null) {
      post.setRootId(annonceEntity.getMattermostRootId());
    }
    callCreatePost(post);
  }

  private boolean isPublishMattermost() {
    return (!botConfig.channelId().isEmpty() && !botConfig.token().isEmpty());
  }

  private CreatePostRequest initPost(AnnonceEntity annonceEntity, boolean creation) {
    CreatePostRequest createPostRequest = new CreatePostRequest();
    createPostRequest.setChannelId(botConfig.channelId());
    String post = getPost(annonceEntity, creation);
    createPostRequest.setMessage(post);
    return createPostRequest;
  }

  private String getPost(AnnonceEntity annonceEntity, boolean creation) {
    return postTemplate
        .data(
            "annonce",
            annonceEntity,
            "creation",
            creation,
            "username",
            userService.getCurrentUser().getUsername(),
            "frontUrl",
            frontUrl)
        .render();
  }

  private PostResponse callCreatePost(CreatePostRequest post) {
    try {
      return mattermostClient.createPost("Bearer " + botConfig.token(), post);
    } catch (RuntimeException e) {
      log.error("Failed to create post on Mattermost", e);
    }
    return null;
  }
}
