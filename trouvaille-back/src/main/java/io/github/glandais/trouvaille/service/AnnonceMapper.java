package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.entity.*;
import org.bson.types.ObjectId;
import org.mapstruct.*;

@Mapper(componentModel = "cdi", unmappedSourcePolicy = ReportingPolicy.ERROR)
public abstract class AnnonceMapper {

  protected ObjectId mapObjectId(String objectId) {
    return new ObjectId(objectId);
  }

  public abstract AnnonceEntity mapAnnonceCreate(AnnonceBase data);

  public abstract void updateAnnonceEntity(
      @MappingTarget AnnonceEntity annonceEntity, AnnonceWithStatut data);

  @EnumMapping(
      nameTransformationStrategy = MappingConstants.CASE_TRANSFORMATION,
      configuration = "lower")
  public abstract AnnonceEntityType mapAnnonceType(AnnonceType type);

  @EnumMapping(
      nameTransformationStrategy = MappingConstants.CASE_TRANSFORMATION,
      configuration = "lower")
  public abstract AnnonceEntityNature mapAnnonceNature(AnnonceNature nature);

  @EnumMapping(
      nameTransformationStrategy = MappingConstants.CASE_TRANSFORMATION,
      configuration = "lower")
  public abstract AnnonceEntityStatut mapAnnonceStatut(AnnonceStatut statut);

  @EnumMapping(
      nameTransformationStrategy = MappingConstants.CASE_TRANSFORMATION,
      configuration = "lower")
  public abstract PeriodeEntityLocation mapPeriodeLocation(PeriodeLocation periodeLocation);
}
