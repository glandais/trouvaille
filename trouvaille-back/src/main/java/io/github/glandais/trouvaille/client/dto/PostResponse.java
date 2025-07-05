package io.github.glandais.trouvaille.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PostResponse {

  @JsonProperty("id")
  private String id;
}
