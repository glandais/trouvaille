package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.entity.*;
import io.github.glandais.trouvaille.repository.PhotoRepository;
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

  @Mapping(target = "removePhotosItem", ignore = true)
  @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "mapUtilisateurId")
  @Mapping(target = "photos", source = "photos", qualifiedByName = "mapPhotos")
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

  @Mapping(target = "removePhotosItem", ignore = true)
  @Mapping(target = "distance", source = "distance", qualifiedByName = "mapDistance")
  @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "mapUtilisateurId")
  @Mapping(target = "photos", source = "photos", qualifiedByName = "mapPhotos")
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

  public Photo mapPhoto(PhotoEntity photoEntity) {
    if (photoEntity == null) {
      return null;
    }
    Photo photo = new Photo();
    String id = mapObjectId(photoEntity.getId());
    photo.setId(id);
    photo.setWidth(photoEntity.getWidth());
    photo.setHeight(photoEntity.getHeight());
    photo.setFullUrl("/api/v1/photos/" + id + "/full");
    photo.setThumbUrl("/api/v1/photos/" + id + "/thumb");
    return photo;
  }

  protected abstract Utilisateur mapUserEntity(UserEntity userEntity);

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
}
