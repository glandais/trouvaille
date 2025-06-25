package io.github.glandais.trouvaille.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@MongoEntity(collection = "Annonce")
@Getter
@Setter
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

    public List<ObjectId> photos = new ArrayList<>();

    public Date dateCreation;

    public Date dateModification;

}
