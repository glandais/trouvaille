package io.github.glandais.trouvaille.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.net.URI;

@MongoEntity(collection = "User")
@Getter
@Setter
public class UserEntity {

    public ObjectId id;

    public String pseudo;

    public URI avatarUrl;

}
