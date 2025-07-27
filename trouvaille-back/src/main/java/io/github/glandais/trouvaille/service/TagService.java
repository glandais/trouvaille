package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.api.model.Tag;
import io.github.glandais.trouvaille.api.model.TagCreateUpdate;
import io.github.glandais.trouvaille.entity.TagEntity;
import io.github.glandais.trouvaille.repository.TagRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ValidationException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

@ApplicationScoped
@RequiredArgsConstructor
public class TagService {

  final TagRepository tagRepository;

  final AnnonceEntityMapper annonceEntityMapper;

  public List<Tag> getAllTags() {
    return tagRepository.findAllOrderByNom().stream()
        .map(annonceEntityMapper::mapTagEntity)
        .collect(Collectors.toList());
  }

  public List<Tag> getAllActiveTags() {
    return tagRepository.findAllActiveOrderByNom().stream()
        .map(annonceEntityMapper::mapTagEntity)
        .collect(Collectors.toList());
  }

  public Tag createTag(TagCreateUpdate tagCreate) {
    // Check if tag with this name already exists
    if (tagRepository.existsByNom(tagCreate.getNom())) {
      throw new ValidationException("Un tag avec ce nom existe déjà");
    }

    TagEntity tagEntity = new TagEntity(tagCreate.getNom(), tagCreate.getCouleur());
    tagRepository.persist(tagEntity);

    return annonceEntityMapper.mapTagEntity(tagEntity);
  }

  public Optional<Tag> updateTag(String id, TagCreateUpdate tagUpdate) {
    try {
      ObjectId objectId = new ObjectId(id);
      Optional<TagEntity> tagEntityOpt = tagRepository.findByIdOptional(objectId);

      if (tagEntityOpt.isEmpty()) {
        return Optional.empty();
      }

      TagEntity tagEntity = tagEntityOpt.get();

      // Check if another tag with this name already exists
      if (tagRepository.existsByNomAndIdNot(tagUpdate.getNom(), objectId)) {
        throw new ValidationException("Un tag avec ce nom existe déjà");
      }

      tagEntity.nom = tagUpdate.getNom();
      tagEntity.couleur = tagUpdate.getCouleur();
      tagEntity.active = tagUpdate.getActive();
      tagEntity.updateModificationDate();

      tagRepository.update(tagEntity);

      return Optional.of(annonceEntityMapper.mapTagEntity(tagEntity));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
