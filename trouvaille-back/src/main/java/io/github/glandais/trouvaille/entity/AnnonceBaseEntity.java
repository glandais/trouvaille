package io.github.glandais.trouvaille.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Getter
@Setter
public class AnnonceBaseEntity {

  public ObjectId id;

  public ObjectId utilisateur;

  public AnnonceEntityStatut statut;

  public AnnonceEntityType type;

  public AnnonceEntityNature nature;

  public String titre;

  public String description;

  public Double prix;

  public PrixEntityUnite prixUnite;

  public PeriodeEntityLocation periodeLocation;

  public CoordinatesEntity coordinates;

  public String ville;

  public List<ObjectId> photos = new ArrayList<>();

  public Date dateCreation;

  public Date dateModification;

  public String mattermostRootId;
}
