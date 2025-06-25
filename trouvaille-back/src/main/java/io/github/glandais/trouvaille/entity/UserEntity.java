package io.github.glandais.trouvaille.entity;

import io.quarkus.arc.All;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import org.bson.types.ObjectId;

@MongoEntity(collection = "User")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    public ObjectId id;

    public String externalId;

    public String username;

    public String nickname;

}
