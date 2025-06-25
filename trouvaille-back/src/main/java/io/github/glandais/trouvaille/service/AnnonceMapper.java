package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.AnnonceEntityNature;
import io.github.glandais.trouvaille.entity.AnnonceEntityType;
import io.github.glandais.trouvaille.openapi.beans.AnnonceCreate;
import io.github.glandais.trouvaille.openapi.beans.AnnonceNature;
import io.github.glandais.trouvaille.openapi.beans.AnnonceType;
import io.github.glandais.trouvaille.openapi.beans.AnnonceUpdate;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi",
        unmappedSourcePolicy = ReportingPolicy.ERROR)
public abstract class AnnonceMapper {

    protected ObjectId mapObjectId(String objectId) {
        return new ObjectId(objectId);
    }

    @Mapping(source = "photosIds", target = "photos")
    public abstract AnnonceEntity mapAnnonceCreate(AnnonceCreate data);

    @Mapping(source = "photosIds", target = "photos")
    public abstract void updateAnnonceEntity(@MappingTarget AnnonceEntity annonceEntity, AnnonceUpdate data);

    public abstract AnnonceEntityType mapAnnonceType(AnnonceType type);

    public abstract AnnonceEntityNature mapAnnonceNature(AnnonceNature nature);
}
