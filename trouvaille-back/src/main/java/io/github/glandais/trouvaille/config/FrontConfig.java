package io.github.glandais.trouvaille.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "trouvaille.front")
public interface FrontConfig {
  String authorizeUri();

  String clientId();
}
