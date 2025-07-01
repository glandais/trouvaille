package io.github.glandais.trouvaille.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.types.ObjectId;

@MongoEntity(collection = "Photo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoEntity {

  public ObjectId id;

  public ObjectId utilisateur;

  public int width;

  public int height;
}
