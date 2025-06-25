
package io.github.glandais.trouvaille.entity;

import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class CoordinatesEntity {

    public static final int LONGITUDE = 0;
    public static final int LATITUDE = 1;

    public String type;

    public List<Double> coordinates;

    public CoordinatesEntity() {
        this.type = "Point";
        this.coordinates = new ArrayList<>(2);
        this.coordinates.add(0.0);
        this.coordinates.add(0.0);
    }

    @BsonIgnore
    public double getLongitude() {
        return coordinates.get(LONGITUDE);
    }

    @BsonIgnore
    public void setLongitude(double longitude) {
        coordinates.set(LONGITUDE, longitude);
    }

    @BsonIgnore
    public double getLatitude() {
        return coordinates.get(LATITUDE);
    }

    @BsonIgnore
    public void setLatitude(double latitude) {
        coordinates.set(LATITUDE, latitude);
    }

}
