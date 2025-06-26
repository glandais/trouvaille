package io.github.glandais.trouvaille.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "trouvaille.oauth")
public interface OAuthConfig {
  String clientId();

  String clientSecret();
}
