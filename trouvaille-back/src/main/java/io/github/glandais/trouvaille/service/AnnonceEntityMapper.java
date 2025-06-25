package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.CoordinatesEntity;
import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.openapi.beans.*;
import io.github.glandais.trouvaille.repository.UserRepository;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.mapstruct.*;

import java.util.List;

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

    @Named("mapDistance")
    protected Double mapDistance(AnnonceEntity annonceEntity, @Context Coordinates to) {
        CoordinatesEntity from = annonceEntity.getCoordinates();

        // If no coordinates available, return null
        if (from == null || to == null ||
                to.getLatitude() == null || to.getLongitude() == null) {
            return null;
        }

        return calculateHaversineDistance(
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude()
        );
    }

    /**
     * Calculate distance between two points using Haversine formula
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers, rounded to nearest 100 meters
     */
    private Double calculateHaversineDistance(double lat1, double lon1, Double lat2, Double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;

        // Convert degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate differences
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Apply Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        // Round to nearest 100 meters (0.1 km)
        return Math.round(distance * 10.0) / 10.0;
    }

    @Mapping(target = "distance", source = "annonceEntity", qualifiedByName = "mapDistance")
    public abstract AnnonceList mapAnnonceEntity(AnnonceEntity annonceEntity, @Context Coordinates coordinates);

    public List<AnnonceList> mapAnnonceEntities(List<AnnonceEntity> entities, Double latitude, Double longitude) {
        if (entities == null) {
            return null;
        }
        Coordinates coordinates = new Coordinates();
        coordinates.setLatitude(latitude);
        coordinates.setLongitude(longitude);
        return entities.stream().map(e -> mapAnnonceEntity(e, coordinates)).toList();
    }

    public abstract AnnonceType mapStringToAnnonceType(String type);

    public abstract AnnonceNature mapStringToAnnonceNature(String nature);

    public abstract PeriodeLocation mapStringToPeriodeLocation(String periodeLocation);
}
