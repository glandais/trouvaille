package io.github.glandais.trouvaille.client.mattermost.dto;

import lombok.Data;

@Data
public class User {
  private String id;
  private String username;
  private String nickname;
}
