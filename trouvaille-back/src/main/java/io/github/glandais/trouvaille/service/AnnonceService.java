package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.AnnonceEntityStatut;
import io.github.glandais.trouvaille.openapi.beans.*;
import io.github.glandais.trouvaille.repository.AnnonceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@ApplicationScoped
@RequiredArgsConstructor
public class AnnonceService {

    final AnnonceRepository annonceRepository;

    final AnnonceEntityMapper annonceEntityMapper;

    final AnnonceMapper annonceMapper;

    final UserService userService;

    public Annonces listAnnonces(AnnonceType type, AnnonceNature nature, BigInteger page, BigInteger limit, String search, String userPseudo, BigDecimal prixMin, BigDecimal prixMax, Double latitude, Double longitude, BigDecimal distanceMax, String sortBy, String sortOrder) {
        return new Annonces();
    }

    public Annonce createAnnonce(AnnonceCreate data) {
        AnnonceEntity annonceEntity = annonceMapper.mapAnnonceCreate(data);
        annonceEntity.setId(new ObjectId());
        annonceEntity.setUtilisateur(userService.getCurrentUser().getId());
        annonceEntity.setStatut(AnnonceEntityStatut.active);
        annonceEntity.setDateCreation(new Date());
        annonceEntity.setDateModification(new Date());
        annonceRepository.persist(annonceEntity);
        return getAnnonce(annonceEntity.getId().toString());
    }

    public Annonce getAnnonce(String id) {
        AnnonceEntity annonceEntity = annonceRepository.findById(new ObjectId(id));
        checkAnnonceExists(annonceEntity);
        return annonceEntityMapper.mapAnnonceEntity(annonceEntity);
    }

    public Annonce putAnnonce(String id, AnnonceUpdate data) {
        AnnonceEntity annonceEntity = annonceRepository.findById(new ObjectId(id));
        checkAnnonceOwnership(annonceEntity);
        annonceMapper.updateAnnonceEntity(annonceEntity, data);
        annonceEntity.setDateModification(new Date());
        annonceRepository.update(annonceEntity);
        return getAnnonce(id);
    }

    public void deleteAnnonce(String id) {
        AnnonceEntity annonceEntity = annonceRepository.findById(new ObjectId(id));
        checkAnnonceOwnership(annonceEntity);
        annonceRepository.deleteById(new ObjectId(id));
    }

    private void checkAnnonceOwnership(AnnonceEntity annonceEntity) {
        checkAnnonceExists(annonceEntity);
        ObjectId currentUserId = userService.getCurrentUser().getId();
        if (!annonceEntity.utilisateur.equals(currentUserId)) {
            throw new ForbiddenException("You can only modify your own annonces");
        }
    }

    private void checkAnnonceExists(AnnonceEntity annonceEntity) {
        if (annonceEntity == null) {
            throw new NotFoundException("Annonce not found");
        }
    }
}
