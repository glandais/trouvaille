package io.github.glandais.trouvaille.repository;

import io.github.glandais.trouvaille.entity.TagEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.bson.types.ObjectId;

@ApplicationScoped
public class TagRepository implements PanacheMongoRepository<TagEntity> {

  public List<TagEntity> findAllActiveOrderByNom() {
    return find("active = ?1", true).list().stream()
        .sorted((a, b) -> a.nom.compareToIgnoreCase(b.nom))
        .toList();
  }

  public List<TagEntity> findAllOrderByNom() {
    return findAll().list().stream().sorted((a, b) -> a.nom.compareToIgnoreCase(b.nom)).toList();
  }

  public boolean existsByNom(String nom) {
    return count("nom", nom) > 0;
  }

  public boolean existsByNomAndIdNot(String nom, ObjectId id) {
    return count("nom = ?1 and _id != ?2", nom, id) > 0;
  }
}
