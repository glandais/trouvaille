package io.github.glandais.trouvaille.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrixEntityUnite {
  euro("€"),

  demi("\uD83C\uDF7A"),

  soft("\uD83C\uDF79");

  final String label;
}
