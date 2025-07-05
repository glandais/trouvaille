package io.github.glandais.trouvaille.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "trouvaille.bot")
public interface BotConfig {
  String channelId();

  String token();
}
