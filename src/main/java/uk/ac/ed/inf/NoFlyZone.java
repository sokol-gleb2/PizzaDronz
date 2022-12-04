package uk.ac.ed.inf;
/**
 * Class responsible for giving structure to the No-Fly-Zones pulled from the server.
 * Each NFZ is made up of a name and a list of (lng, lat) coordinates that make up the polygon.
 */

import java.util.List;

public class NoFlyZone {
    String name;
    List<LngLat> coordinates;

    public NoFlyZone(String nameGiven, List<LngLat> coordinatesGiven) {
        this.name = nameGiven;
        this.coordinates = coordinatesGiven;
    }

    public String getName() {
        return this.name;
    }

    public List<LngLat> getCoordinates() {
        return this.coordinates;
    }

}
