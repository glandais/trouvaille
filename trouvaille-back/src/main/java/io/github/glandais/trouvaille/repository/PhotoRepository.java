package io.github.glandais.trouvaille.repository;

import io.github.glandais.trouvaille.entity.PhotoEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PhotoRepository implements PanacheMongoRepository<PhotoEntity> {
}
