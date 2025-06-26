package io.github.glandais.trouvaille.repository;

import io.github.glandais.trouvaille.entity.UserEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheMongoRepository<UserEntity> {}
