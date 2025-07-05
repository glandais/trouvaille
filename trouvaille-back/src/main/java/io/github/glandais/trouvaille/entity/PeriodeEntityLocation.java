package io.github.glandais.trouvaille.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PeriodeEntityLocation {
  jour("jour"),
  semaine("semaine"),
  mois("mois");

  final String label;
}
