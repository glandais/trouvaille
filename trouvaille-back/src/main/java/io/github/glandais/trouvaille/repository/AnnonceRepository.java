package io.github.glandais.trouvaille.repository;

import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnnonceRepository implements PanacheMongoRepository<AnnonceEntity> {
}
