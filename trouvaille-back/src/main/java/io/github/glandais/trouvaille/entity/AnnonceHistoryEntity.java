package io.github.glandais.trouvaille.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Getter
@Setter
@MongoEntity(collection = "AnnonceHistory")
public class AnnonceHistoryEntity extends AnnonceBaseEntity {

  public ObjectId annonceId;
}
