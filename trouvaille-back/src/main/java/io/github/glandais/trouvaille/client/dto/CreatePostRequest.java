package io.github.glandais.trouvaille.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreatePostRequest {

  @JsonProperty("channel_id")
  private String channelId;

  @JsonProperty("message")
  private String message;

  @JsonProperty("root_id")
  private String rootId;
}
