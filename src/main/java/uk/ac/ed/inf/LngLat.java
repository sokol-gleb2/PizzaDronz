package uk.ac.ed.inf;

public record LngLat(double lng, double lat) {
    public boolean inCentralArea() {
        boolean inCentralArea = false;
        ThreadSafeSingleton threadSafeSingleton = new ThreadSafeSingleton();
        String[] args = {" https://ilp-rest.azurewebsites.net/", "centralArea"};
        threadSafeSingleton.coordinates(args);
        return inCentralArea;
    }
}
