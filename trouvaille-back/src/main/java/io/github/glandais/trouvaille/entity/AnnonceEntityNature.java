package io.github.glandais.trouvaille.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnnonceEntityNature {
  offre("Offre"),
  demande("Demande");

  final String label;
}
