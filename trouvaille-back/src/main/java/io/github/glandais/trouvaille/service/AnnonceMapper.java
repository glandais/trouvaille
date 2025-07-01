package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.entity.*;
import java.util.List;
import org.bson.types.ObjectId;
import org.mapstruct.*;

@Mapper(componentModel = "cdi", unmappedSourcePolicy = ReportingPolicy.ERROR)
public abstract class AnnonceMapper {

  protected ObjectId mapObjectId(String objectId) {
    return new ObjectId(objectId);
  }

  @Mapping(source = "photos", target = "photos", qualifiedByName = "mapPhotos")
  public abstract AnnonceEntity mapAnnonceCreate(AnnonceBase data);

  @Named("mapPhotos")
  public abstract List<ObjectId> mapPhotos(List<Photo> data);

  public ObjectId mapPhoto(Photo data) {
    if (data == null) {
      return null;
    }
    return mapObjectId(data.getId());
  }

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
