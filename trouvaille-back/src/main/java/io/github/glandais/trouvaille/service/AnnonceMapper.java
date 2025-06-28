package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.AnnonceEntityNature;
import io.github.glandais.trouvaille.entity.AnnonceEntityStatut;
import io.github.glandais.trouvaille.entity.AnnonceEntityType;
import io.github.glandais.trouvaille.openapi.beans.AnnonceBase;
import io.github.glandais.trouvaille.openapi.beans.AnnonceNature;
import io.github.glandais.trouvaille.openapi.beans.AnnonceStatut;
import io.github.glandais.trouvaille.openapi.beans.AnnonceType;
import io.github.glandais.trouvaille.openapi.beans.AnnonceWithStatut;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedSourcePolicy = ReportingPolicy.ERROR)
public abstract class AnnonceMapper {

  protected ObjectId mapObjectId(String objectId) {
    return new ObjectId(objectId);
  }

  public abstract AnnonceEntity mapAnnonceCreate(AnnonceBase data);

  public abstract void updateAnnonceEntity(
      @MappingTarget AnnonceEntity annonceEntity, AnnonceWithStatut data);

  public abstract AnnonceEntityType mapAnnonceType(AnnonceType type);

  public abstract AnnonceEntityNature mapAnnonceNature(AnnonceNature nature);

  public abstract AnnonceEntityStatut mapAnnonceStatut(AnnonceStatut statut);
}
