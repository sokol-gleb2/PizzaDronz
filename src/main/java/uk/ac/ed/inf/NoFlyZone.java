package uk.ac.ed.inf;


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
