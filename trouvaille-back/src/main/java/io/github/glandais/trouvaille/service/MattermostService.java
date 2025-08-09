package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.client.mattermost.MattermostClient;
import io.github.glandais.trouvaille.client.mattermost.dto.CreatePostRequest;
import io.github.glandais.trouvaille.client.mattermost.dto.PostResponse;
import io.github.glandais.trouvaille.config.BotConfig;
import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class MattermostService {

  @Inject AnnonceEntityMapper annonceEntityMapper;

  @Inject @RestClient MattermostClient mattermostClient;

  @Inject UserService userService;

  @Inject BotConfig botConfig;

  @ConfigProperty(name = "quarkus.http.cors.origins")
  String frontUrl;

  @Location("post.md")
  Template postTemplate;

  public void createAnnonce(AnnonceEntity annonceEntity) {
    CreatePostRequest post = initPost(annonceEntity, true);
    PostResponse postResponse = callCreatePost(post);
    if (postResponse != null) {
      annonceEntity.setMattermostRootId(postResponse.getId());
    }
  }

  public void updateAnnonce(AnnonceEntity annonceEntity) {
    CreatePostRequest post = initPost(annonceEntity, false);
    if (annonceEntity.getMattermostRootId() != null) {
      post.setRootId(annonceEntity.getMattermostRootId());
    }
    callCreatePost(post);
  }

  private CreatePostRequest initPost(AnnonceEntity annonceEntity, boolean creation) {
    CreatePostRequest createPostRequest = new CreatePostRequest();
    createPostRequest.setChannelId(botConfig.channelId());
    String post = getPost(annonceEntity, creation);
    createPostRequest.setMessage(post);
    return createPostRequest;
  }

  private String getPost(AnnonceEntity annonceEntity, boolean creation) {
    List<String> tags = annonceEntityMapper.mapTagsLabelsFromIds(annonceEntity.getTags());
    String prixString = formatDouble(annonceEntity.getPrix());
    return postTemplate
        .data("annonce", annonceEntity)
        .data("tags", tags)
        .data("prix", prixString)
        .data("creation", creation)
        .data("username", userService.getCurrentUser().getUsername())
        .data("frontUrl", frontUrl)
        .render();
  }

  public static String formatDouble(double value) {
    if (value == Math.floor(value)) {
      return String.valueOf((int) value);
    } else {
      DecimalFormat df =
          new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.FRANCE));
      return df.format(value);
    }
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
