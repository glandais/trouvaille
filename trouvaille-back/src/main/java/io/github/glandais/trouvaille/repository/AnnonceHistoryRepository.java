package io.github.glandais.trouvaille.repository;

import io.github.glandais.trouvaille.entity.AnnonceHistoryEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AnnonceHistoryRepository implements PanacheMongoRepository<AnnonceHistoryEntity> {}
