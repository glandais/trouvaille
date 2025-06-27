package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.AnnonceEntityWithDistance;
import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.openapi.beans.*;
import io.github.glandais.trouvaille.repository.UserRepository;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class AnnonceEntityMapper {

  @Inject UserRepository userRepository;

  @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "mapUtilisateurId")
  public abstract Annonce mapAnnonceEntity(AnnonceEntity annonceEntity);

  @Mapping(target = "distance", source = "distance", qualifiedByName = "mapDistance")
  @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "mapUtilisateurId")
  public abstract AnnonceList mapAnnonceEntityToAnnonceList(
      AnnonceEntityWithDistance annonceEntity);

  protected String mapObjectId(ObjectId objectId) {
    return objectId.toHexString();
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

  public abstract AnnonceType mapStringToAnnonceType(String type);

  public abstract AnnonceNature mapStringToAnnonceNature(String nature);

  public abstract PeriodeLocation mapStringToPeriodeLocation(String periodeLocation);
}
