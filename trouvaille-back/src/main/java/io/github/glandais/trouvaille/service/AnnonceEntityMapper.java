package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.entity.*;
import io.github.glandais.trouvaille.repository.PhotoRepository;
import io.github.glandais.trouvaille.repository.TagRepository;
import io.github.glandais.trouvaille.repository.UserRepository;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.mapstruct.*;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class AnnonceEntityMapper {

  @Inject UserRepository userRepository;

  @Inject PhotoRepository photoRepository;

  @Inject TagRepository tagRepository;

  @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "mapUtilisateurId")
  @Mapping(target = "photos", source = "photos", qualifiedByName = "mapPhotos")
  @Mapping(target = "tags", source = "tags", qualifiedByName = "mapTags")
  @Mapping(target = "prix.montant", source = "prix")
  @Mapping(target = "prix.unite", source = "prixUnite")
  public abstract Annonce mapAnnonceEntity(AnnonceEntity annonceEntity);

  @AfterMapping
  public void afterMappingAnnonce(@MappingTarget Annonce annonce) {
    if (annonce == null) {
      return;
    }
    if (annonce.getPrix().getUnite() == null) {
      annonce.getPrix().setUnite(PrixUnite.EURO);
    }
  }

  @Mapping(target = "distance", source = "distance", qualifiedByName = "mapDistance")
  @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "mapUtilisateurId")
  @Mapping(target = "photos", source = "photos", qualifiedByName = "mapPhotos")
  @Mapping(target = "tags", source = "tags", qualifiedByName = "mapTags")
  @Mapping(target = "prix.montant", source = "prix")
  @Mapping(target = "prix.unite", source = "prixUnite")
  public abstract AnnonceList mapAnnonceEntityToAnnonceList(
      AnnonceEntityWithDistance annonceEntity);

  @AfterMapping
  public void afterMappingAnnonceList(@MappingTarget AnnonceList annonce) {
    if (annonce == null) {
      return;
    }
    if (annonce.getPrix().getUnite() == null) {
      annonce.getPrix().setUnite(PrixUnite.EURO);
    }
  }

  protected String mapObjectId(ObjectId objectId) {
    return objectId.toHexString();
  }

  protected OffsetDateTime toOffsetDateTime(final Date date) {
    if (date == null) {
      return null;
    }
    return date.toInstant().atOffset(ZoneOffset.UTC);
  }

  @Named("mapDistance")
  protected Double mapDistance(Double distance) {
    if (distance == null) {
      return null;
    }
    return Math.round(distance * 10.0) / 10.0;
  }

  @Named("mapUtilisateurId")
  protected Utilisateur mapUtilisateurId(ObjectId userId) {
    UserEntity userEntity = userRepository.findById(userId);
    return mapUserEntity(userEntity);
  }

  @Named("mapPhotos")
  protected List<Photo> mapPhotos(List<ObjectId> photoIds) {
    if (photoIds == null) {
      return List.of();
    }
    return photoIds.stream()
        .map(photoId -> photoRepository.findByIdOptional(photoId))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(this::mapPhoto)
        .toList();
  }

  public abstract Photo mapPhoto(PhotoEntity photoEntity);

  protected abstract Utilisateur mapUserEntity(UserEntity userEntity);

  @Named("mapTags")
  protected List<Tag> mapTagsFromIds(List<ObjectId> tagIds) {
    return mapTags(tagRepository.list("_id in ?1", tagIds));
  }

  protected abstract List<Tag> mapTags(List<TagEntity> list);

  public abstract Tag mapTagEntity(TagEntity tagEntity);

  @EnumMapping(
      nameTransformationStrategy = MappingConstants.CASE_TRANSFORMATION,
      configuration = "upper")
  public abstract AnnonceType mapAnnonceType(AnnonceEntityType type);

  @EnumMapping(
      nameTransformationStrategy = MappingConstants.CASE_TRANSFORMATION,
      configuration = "upper")
  public abstract AnnonceNature mapAnnonceNature(AnnonceEntityNature nature);

  @EnumMapping(
      nameTransformationStrategy = MappingConstants.CASE_TRANSFORMATION,
      configuration = "upper")
  public abstract AnnonceStatut mapAnnonceStatut(AnnonceEntityStatut statut);

  @EnumMapping(
      nameTransformationStrategy = MappingConstants.CASE_TRANSFORMATION,
      configuration = "upper")
  public abstract PeriodeLocation mapStringToPeriodeLocation(PeriodeEntityLocation periodeLocation);

  @EnumMapping(
      nameTransformationStrategy = MappingConstants.CASE_TRANSFORMATION,
      configuration = "upper")
  public abstract PrixUnite mapPrixUnit(PrixEntityUnite unite);

  @Mapping(target = "annonceId", source = "id")
  @Mapping(target = "id", qualifiedByName = "generateId")
  public abstract AnnonceHistoryEntity copy(AnnonceEntity annonceEntity);

  @Named("generateId")
  protected ObjectId generateId(ObjectId source) {
    return new ObjectId();
  }
}
