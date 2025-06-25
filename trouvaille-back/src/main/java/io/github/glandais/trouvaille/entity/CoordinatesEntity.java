
package io.github.glandais.trouvaille.entity;

public class CoordinatesEntity {

    public static final int LONGITUDE = 0;
    public static final int LATITUDE = 1;

    public String type = "Point";

    public double[] coordinates = new double[]{0.0, 0.0};

    public double getLongitude() {
        return coordinates[LONGITUDE];
    }

    public void setLongitude(double longitude) {
        coordinates[LONGITUDE] = longitude;
    }

    public double getLatitude() {
        return coordinates[LATITUDE];
    }

    public void setLatitude(double latitude) {
        coordinates[LATITUDE] = latitude;
    }

}
