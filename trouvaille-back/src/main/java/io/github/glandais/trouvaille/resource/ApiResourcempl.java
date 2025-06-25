package io.github.glandais.trouvaille.resource;

import io.github.glandais.trouvaille.openapi.ApiResource;
import io.github.glandais.trouvaille.openapi.beans.*;
import io.github.glandais.trouvaille.service.AnnonceService;
import io.github.glandais.trouvaille.service.PhotoService;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

@RequiredArgsConstructor
public class ApiResourcempl implements ApiResource {

    final AnnonceService annonceService;

    final PhotoService photoService;

    @Override
    public Annonces listAnnonces(AnnonceType type, AnnonceNature nature, BigInteger page, BigInteger limit, String search, String userPseudo, BigDecimal prixMin, BigDecimal prixMax, Double latitude, Double longitude, BigDecimal distanceMax, String sortBy, String sortOrder) {
        return annonceService.listAnnonces(type, nature, page, limit, search, userPseudo, prixMin, prixMax, latitude, longitude, distanceMax, sortBy, sortOrder);
    }

    @Override
    public Annonce createAnnonce(AnnonceCreate data) {
        return annonceService.createAnnonce(data);
    }

    @Override
    public Annonce getAnnonce(String id) {
        return annonceService.getAnnonce(id);
    }

    @Override
    public Annonce putAnnonce(String id, AnnonceUpdate data) {
        return annonceService.putAnnonce(id, data);
    }

    @Override
    public void deleteAnnonce(String id) {
        annonceService.deleteAnnonce(id);
    }

    @Override
    public String createPhoto(InputStream data) {
        return photoService.createPhoto(data);
    }

    @Override
    public void deletePhoto(String photoId) {
        photoService.deletePhoto(photoId);
    }
}
