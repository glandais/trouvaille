package io.github.glandais.trouvaille.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;

@MongoEntity(collection = "Annonce")
public class AnnonceEntity extends AnnonceBaseEntity {}
