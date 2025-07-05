package io.github.glandais.trouvaille.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.*;
import org.bson.types.ObjectId;

@MongoEntity(collection = "Annonce")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnonceEntity {

  public ObjectId id;

  public ObjectId utilisateur;

  public AnnonceEntityStatut statut;

  public AnnonceEntityType type;

  public AnnonceEntityNature nature;

  public String titre;

  public String description;

  public Double prix;

  public PeriodeEntityLocation periodeLocation;

  public CoordinatesEntity coordinates;

  public String ville;

  @Builder.Default public List<ObjectId> photos = new ArrayList<>();

  public Date dateCreation;

  public Date dateModification;

  public String mattermostRootId;
}
