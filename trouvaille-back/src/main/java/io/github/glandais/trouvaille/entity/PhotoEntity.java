package io.github.glandais.trouvaille.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@MongoEntity(collection = "Photo")
@Getter
@Setter
public class PhotoEntity {

    public ObjectId id;

    public ObjectId utilisateur;

}
