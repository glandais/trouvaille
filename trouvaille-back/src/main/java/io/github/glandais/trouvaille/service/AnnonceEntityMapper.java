package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.entity.*;
import io.github.glandais.trouvaille.repository.UserRepository;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.bson.types.ObjectId;
import org.mapstruct.*;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class AnnonceEntityMapper {

  @Inject UserRepository userRepository;

  @Mapping(target = "removePhotosItem", ignore = true)
  @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "mapUtilisateurId")
  public abstract Annonce mapAnnonceEntity(AnnonceEntity annonceEntity);

  @Mapping(target = "removePhotosItem", ignore = true)
  @Mapping(target = "distance", source = "distance", qualifiedByName = "mapDistance")
  @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "mapUtilisateurId")
  public abstract AnnonceList mapAnnonceEntityToAnnonceList(
      AnnonceEntityWithDistance annonceEntity);

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
}
