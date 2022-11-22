package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Flight {

    LngLat startingPoint;
    LngLat endingPoint;
    int movesTaken;
    List<LngLat> path;
    List<CompassDirection> directions;

    final double STEP_SIZE = 0.00015;

    public Flight(LngLat startingPoint, LngLat endingPoint) {
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
    }

    public void doSomething() {
        System.out.println(startingPoint);
        System.out.println(endingPoint);
    }

    // LngLat from, LngLat to, CompassDirection cd
    public void calculatePath(String urlGiven) throws MalformedURLException {
        this.path = new ArrayList<>();
        this.path.add(this.startingPoint);
        this.directions = new ArrayList<>();
        var urlS = "";
        if (urlGiven.endsWith("/")) {
            urlS = urlGiven.substring(0, urlGiven.length() - 1);
        }
        urlS += urlGiven + "/noFlyZones";
        URL url = new URL(urlS);

        Database db = new Database();

        var allNoFlyZones = db.getNoFlyZones(url);
        // convex-hull problem
        var noFlyZones = noFlyZonesIntersected(allNoFlyZones);
        double angle = 0;
        if (noFlyZones.size() == 0) {
            // straight line to end point
            if ((this.endingPoint.lng() != this.startingPoint.lng()) && (this.endingPoint.lat() != this.startingPoint.lat())) {
                var tan = (this.endingPoint.lng() - this.startingPoint.lng()) / (this.endingPoint.lat() - this.startingPoint.lat());
                if (this.endingPoint.lat() > this.startingPoint.lat() && this.endingPoint.lng() > this.startingPoint.lng()) {
                    //1st quartile
                    angle = (Math.atan(tan));
                } else if (this.endingPoint.lat() > this.startingPoint.lat() && this.endingPoint.lng() < this.startingPoint.lng()) {
                    //4th quartile
                    angle = 2 * Math.PI - (Math.atan(tan));
                } else if (this.endingPoint.lat() < this.startingPoint.lat() && this.endingPoint.lng() > this.startingPoint.lng()) {
                    //2nd quartile
                    angle = Math.PI + (Math.atan(tan));
                } else if (this.endingPoint.lat() < this.startingPoint.lat() && this.endingPoint.lng() < this.startingPoint.lng()) {
                    //3rd quartile
                    angle = Math.PI - (Math.atan(tan));
                }
            } else {
                if (this.endingPoint.lng() == this.startingPoint.lng()) {
                    if (this.endingPoint.lat() > this.startingPoint.lat()) {
                        angle = 0;
                    } else {
                        angle = Math.PI;
                    }
                } else if (this.endingPoint.lat() == this.startingPoint.lat()) {
                    if (this.endingPoint.lng() > this.startingPoint.lng()) {
                        angle = Math.PI / 2;
                    } else {
                        angle = 3 * Math.PI / 2;
                    }
                }
            }


            // find how to express the line with directions we have:

            // in terms of vectors: v_need = [end.lng - start.lng, end.lat - start.lat] = a*e1 + b+e2 + ... + n*e16
            //                                      where ek = [0 0 ... 1 ... 0]
            int index = (int) (angle / (Math.PI / 8));
            if (angle % (Math.PI / 8) == 0) {
                var direction = CompassDirection.values()[index];
                var distance = Math.sqrt(Math.pow(this.endingPoint.lat() - this.startingPoint.lat(), 2) + Math.pow(this.endingPoint.lng() - this.startingPoint.lng(), 2));

                for (int i = 0; i < (int) (distance / STEP_SIZE); i++) {
                    this.path.add(this.path.get(this.path.size() - 1).nextPosition(direction));
                }
            } else {
                var closest = angle - (Math.PI / 8 * index);
                if (closest > Math.PI / 16) {
                    index += 1;
                }
                System.out.println(CompassDirection.values()[index]);
                System.out.println(angle);
                // PROBLEM: the ending point should always be this.endingPoint:
                //          need to call it like so: this.straightLineMapping(this.path.get(this.path.size() - 1).nextPosition(CompassDirection.values()[index]), this.endingPoint);
                // Correct the code below as well!!
                this.straightLineMapping(this.path.get(this.path.size() - 1), this.path.get(this.path.size() - 1).nextPosition(CompassDirection.values()[index]));
            }


        } else {
            // convex-hull it
            var startInCentralArea = startingPoint.inCentralArea();
            var endInCentralArea = endingPoint.inCentralArea();
            if (startInCentralArea == endInCentralArea) {
                // no need to worry
                List<LngLat> allPoints = noFlyZones.get(0).coordinates;
                for (int i = 1; i < noFlyZones.size(); i++) {
                    allPoints.addAll(noFlyZones.get(i).coordinates);
                }
                var routes = convex_hull(allPoints);
                var topRoute = routes.get(0);
                var bottomRoute = routes.get(1);
            } else {
                // need to make sure we don't cross into it again

            }
        }


    }

    private void straightLineMapping(LngLat startingPoint, LngLat endingPoint) {
        System.out.println(endingPoint);
        if (startingPoint.closeTo(endingPoint)) {
            this.path.add(endingPoint);
        } else {

            double angle = 0;
            if ((endingPoint.lng() != startingPoint.lng()) && (endingPoint.lat() != startingPoint.lat())) {
                var tan = (endingPoint.lng() - startingPoint.lng()) / (endingPoint.lat() - startingPoint.lat());
                if (endingPoint.lat() > startingPoint.lat() && endingPoint.lng() > startingPoint.lng()) {
                    //1st quartile
                    angle = (Math.atan(tan));
                } else if (endingPoint.lat() > startingPoint.lat() && endingPoint.lng() < startingPoint.lng()) {
                    //4th quartile
                    angle = 2 * Math.PI - (Math.atan(tan));
                } else if (endingPoint.lat() < startingPoint.lat() && endingPoint.lng() > startingPoint.lng()) {
                    //2nd quartile
                    angle = Math.PI + (Math.atan(tan));
                } else if (endingPoint.lat() < startingPoint.lat() && endingPoint.lng() < startingPoint.lng()) {
                    //3rd quartile
                    angle = Math.PI - (Math.atan(tan));
                }
            } else {
                if (endingPoint.lng() == startingPoint.lng()) {
                    if (endingPoint.lat() > startingPoint.lat()) {
                        angle = 0;
                    } else {
                        angle = Math.PI;
                    }
                } else if (endingPoint.lat() == startingPoint.lat()) {
                    if (endingPoint.lng() > startingPoint.lng()) {
                        angle = Math.PI / 2;
                    } else {
                        angle = 3 * Math.PI / 2;
                    }
                }
            }
            System.out.println(angle);

            int index = (int) (angle / (Math.PI / 8));
            if (angle % (Math.PI / 8) == 0) {
                System.out.println("straight going");
                var direction = CompassDirection.values()[index];
                System.out.println(direction);
                var distance = Math.sqrt(Math.pow(endingPoint.lat() - startingPoint.lat(), 2) + Math.pow(endingPoint.lng() - startingPoint.lng(), 2));

                for (int i = 0; i < (int) (distance / STEP_SIZE); i++) {
                    this.path.add(this.path.get(this.path.size() - 1).nextPosition(direction));
                }
            } else {
                var closest = angle - (Math.PI / 8 * index);
                if (closest > Math.PI / 16) {
                    index += 1;
                }
                this.path.add(endingPoint);
                System.out.println(CompassDirection.values()[index]);
                this.straightLineMapping(this.path.get(this.path.size() - 1), this.path.get(this.path.size() - 1).nextPosition(CompassDirection.values()[index]));
            }
        }


    }

    private List<NoFlyZone> noFlyZonesIntersected(List<NoFlyZone> noFlyZones) {
        List<NoFlyZone> onTheWay = new ArrayList<>();
        List<LngLat> shortestLine = new ArrayList<>();
        shortestLine.add(this.startingPoint);
        shortestLine.add(this.endingPoint);


        for (var noFlyZone : noFlyZones) {
            for (int i = 0; i < noFlyZone.coordinates.size(); i++) {
                List<LngLat> line = new ArrayList<>();
                line.add(noFlyZone.coordinates.get(i));
                line.add(noFlyZone.coordinates.get((i + 1) % noFlyZone.coordinates.size()));
                if (intersect(shortestLine, line)) {
                    onTheWay.add(noFlyZone);
                    break;
                }
            }
        }
        return onTheWay;
    }

    // borrowed from LngLat:
    private boolean intersect(List<LngLat> line1, List<LngLat> line2) {
        //check if the point in ON the line:
        double gradient2 = (line2.get(1).lat() - line2.get(0).lat()) / (line2.get(1).lng() - line2.get(0).lng());
        double intersect2 = line2.get(0).lat() - (line2.get(0).lng() * gradient2);
        if (line1.get(0).lng() * gradient2 + intersect2 == line1.get(0).lat()) {
            return true;
        }
        if (line1.get(0).lng() < line2.get(0).lng() || line1.get(0).lng() < line2.get(1).lng()) { //checking lng is in the right range
            return (line1.get(0).lat() < line2.get(0).lat() && line1.get(0).lat() > line2.get(1).lat()) ||
                    (line1.get(0).lat() > line2.get(0).lat() && line1.get(0).lat() < line2.get(1).lat()); //checking lat is in the right range and returning the truth value
        }

        return false;
    }

    private List<List<LngLat>> convex_hull(List<LngLat> points) {
        List<List<LngLat>> routes = new ArrayList<>();

        List<LngLat> topRoute = new ArrayList<>();
        topRoute.add(this.startingPoint);


        List<LngLat> bottomRoute = new ArrayList<>();
        bottomRoute.add(this.startingPoint);

        topRoute.add(this.endingPoint);
        bottomRoute.add(this.endingPoint);


        List<LngLat> topTriangle = new ArrayList<>();
        topTriangle.add(this.startingPoint);
        topTriangle.add(this.endingPoint);

        List<LngLat> bottomTriangle = new ArrayList<>();
        bottomTriangle.add(this.startingPoint);
        bottomTriangle.add(this.endingPoint);

        while (points.size() > 0) {
            var topPoint = points.get(0);
            var bottomPoint = points.get(0);
            for (var point : points) {
                if (point.lat() > topPoint.lat()) {
                    topPoint = point;
                } else if (point.lat() < bottomPoint.lat()) {
                    bottomPoint = point;
                }
            }

            // adding top and bottom points to routes in order of lng:
            for (int i = 1; i < topRoute.size() - 1; i++) {
                var topRoutePoint = topRoute.get(i);
                if (Math.abs(topPoint.lng()) <= Math.abs(topRoutePoint.lng())) {
                    topRoute.add(i, topPoint);
                    break;
                }
            }

            for (int i = 1; i < bottomRoute.size() - 1; i++) {
                var bottomRoutePoint = bottomRoute.get(i);
                if (Math.abs(bottomPoint.lng()) <= Math.abs(bottomRoutePoint.lng())) {
                    topRoute.add(i, bottomPoint);
                    break;
                }
            }
            //-------------------------------------------------------------

            topTriangle.add(topPoint);
            bottomTriangle.add(bottomPoint);

            for (var point : points) {
                // Setting each point with a line of (lng+10, lat)
                List<LngLat> checkForIntersects = new ArrayList<>();
                checkForIntersects.add(point);
                checkForIntersects.add(new LngLat(point.lng() + 10, point.lat()));

                // Checking which ones are inside the top triangle:
                for (int i = 0; i < topTriangle.size(); i++) {
                    List<LngLat> topTriangleLine = new ArrayList<>();
                    topTriangleLine.add(topTriangle.get(i));
                    topTriangleLine.add(topTriangle.get(i % topTriangle.size()));
                    if (intersect(checkForIntersects, topTriangleLine)) {
                        points.remove(point);
                        break;
                    }
                }

                // Checking which ones are inside the bottom triangle:
                for (int i = 0; i < bottomTriangle.size(); i++) {
                    List<LngLat> bottomTriangleLine = new ArrayList<>();
                    bottomTriangleLine.add(topTriangle.get(i));
                    bottomTriangleLine.add(topTriangle.get(i % bottomTriangle.size()));
                    if (intersect(checkForIntersects, bottomTriangleLine)) {
                        points.remove(point);
                        break;
                    }
                }
            }
        }


        routes.add(topRoute);
        routes.add(bottomRoute);

        return routes;
    }

}
