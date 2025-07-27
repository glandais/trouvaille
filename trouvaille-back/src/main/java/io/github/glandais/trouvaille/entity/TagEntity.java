package io.github.glandais.trouvaille.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@MongoEntity(collection = "Tag")
@NoArgsConstructor
public class TagEntity extends PanacheMongoEntity {

  public String nom;
  public String couleur;
  public LocalDateTime dateCreation;
  public LocalDateTime dateModification;
  public boolean active;

  public TagEntity(String nom, String couleur) {
    this.nom = nom;
    this.couleur = couleur;
    this.dateCreation = LocalDateTime.now();
    this.dateModification = LocalDateTime.now();
    this.active = true;
  }

  public void updateModificationDate() {
    this.dateModification = LocalDateTime.now();
  }
}
