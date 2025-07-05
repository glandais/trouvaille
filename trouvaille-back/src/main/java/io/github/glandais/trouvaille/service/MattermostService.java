package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.client.MattermostClient;
import io.github.glandais.trouvaille.client.dto.CreatePostRequest;
import io.github.glandais.trouvaille.client.dto.PostResponse;
import io.github.glandais.trouvaille.config.BotConfig;
import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.AnnonceEntityType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
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

  public void createAnnonce(AnnonceEntity annonceEntity) {
    CreatePostRequest post = initPost(annonceEntity, true);
    PostResponse postResponse = callCreatePost(post);
    if (postResponse != null) {
      annonceEntity.setMattermostRootId(postResponse.getId());
    }
  }

  private CreatePostRequest initPost(AnnonceEntity annonceEntity, boolean creation) {
    CreatePostRequest post = new CreatePostRequest();
    post.setChannelId(botConfig.channelId());

    StringBuilder sb = new StringBuilder();
    if (!creation) {
      sb.append("Mise à jour !\n\n");
    } else {
      sb.append("Nouvelle annonce !\n\n");
    }
    sb.append(annonceEntity.getType())
        .append("/")
        .append(annonceEntity.getNature())
        .append(" - @")
        .append(userService.getCurrentUser().getUsername())
        .append(" - ");
    sb.append(annonceEntity.getPrix()).append("€");
    if (annonceEntity.getType() == AnnonceEntityType.location
        && annonceEntity.getPeriodeLocation() != null) {
      sb.append("/").append(annonceEntity.getPeriodeLocation());
    }
    sb.append(" - ").append("**").append(annonceEntity.getTitre()).append("**");

    int photoCount = annonceEntity.getPhotos().size();
    sb.append(" - ").append(photoCount).append(" photo");
    if (photoCount > 1) {
      sb.append("s");
    }

    sb.append("\n\n")
        .append(frontUrl)
        .append("/annonces/")
        .append(annonceEntity.getId().toHexString());

    sb.append("\n\n").append(annonceEntity.getDescription());

    if (photoCount > 0) {
      sb.append("\n\n");
    }
    for (ObjectId photo : annonceEntity.getPhotos()) {
      sb.append("![photo](")
          .append(frontUrl)
          .append("/api/v1/photos/")
          .append(photo.toHexString())
          .append("/thumb")
          .append(")");
    }

    post.setMessage(sb.toString());
    return post;
  }

  public void updateAnnonce(AnnonceEntity annonceEntity) {
    CreatePostRequest post = initPost(annonceEntity, false);
    if (annonceEntity.getMattermostRootId() != null) {
      post.setRootId(annonceEntity.getMattermostRootId());
    }
    callCreatePost(post);
  }

  private PostResponse callCreatePost(CreatePostRequest post) {
    try {
      return mattermostClient.createPost("Bearer " + botConfig.token(), post);
    } catch (RuntimeException e) {
      log.error("Failed to create post", e);
    }
    return null;
  }
}
