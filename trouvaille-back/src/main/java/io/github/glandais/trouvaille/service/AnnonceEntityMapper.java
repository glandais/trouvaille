package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.openapi.beans.Annonce;
import io.github.glandais.trouvaille.openapi.beans.Utilisateur;
import io.github.glandais.trouvaille.repository.UserRepository;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class AnnonceEntityMapper {

    @Inject
    UserRepository userRepository;

    @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "mapUtilisateurId")
    public abstract Annonce mapAnnonceEntity(AnnonceEntity annonceEntity);

    protected String mapObjectId(ObjectId objectId) {
        return objectId.toHexString();
    }

    @Named("mapUtilisateurId")
    protected Utilisateur mapUtilisateurId(ObjectId userId) {
        UserEntity userEntity = userRepository.findById(userId);
        return mapUserEntity(userEntity);
    }

    protected abstract Utilisateur mapUserEntity(UserEntity userEntity);

}
