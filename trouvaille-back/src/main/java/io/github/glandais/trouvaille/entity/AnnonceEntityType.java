package io.github.glandais.trouvaille.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnnonceEntityType {
  vente("Vente"),
  location("Location");

  final String label;
}
