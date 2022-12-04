/**
 * Class for calculating the flight between any two points, taking into account no-fly-zones and Central Area
 * Updates path (consisting of coordinates), directions (consisting of CompassDirection),
 * and movesTaken (number of moves the flight takes).
 */


package uk.ac.ed.inf;

import java.awt.geom.Line2D;
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
    List<NoFlyZone> noFlyZones;
    List<Long> ticks = new ArrayList<>();

    final double STEP_SIZE = 0.00015;
    Database db = new Database();

    public Flight(LngLat startingPoint, LngLat endingPoint) {
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.path = new ArrayList<>();
        this.directions = new ArrayList<>();
    }

    /**
     * This function is responsible for calculating the path between startingPoint and endingPoint.
     * It first looks at whether there are no-fly-zones on the way.
     * If yes, Then calculate path with Convex-Hull and do some more checks regarding Central Area.
     * If no, Then figure out the quickest straight line path algorithm.
     * The two scenarios trigger two recursive functions that are different, but the principal is the same.
     * @param urlGiven - URL passed through the command line. Checks are done to see if it ends with '/'
     * @throws MalformedURLException - for the URL
     */

    public void calculatePath(String urlGiven) throws MalformedURLException {

        // Formatting the URL: ------------------------------------------------------------------
        var urlS = "";
        if (urlGiven.endsWith("/")) {
            urlS = urlGiven.substring(0, urlGiven.length() - 1);
        } else {
            urlS = urlGiven;
        }
        urlS += "/noFlyZones";
        URL url = new URL(urlS);
        // --------------------------------------------------------------------------------------


        // In case there are no no-fly-zones: ---------------------------------------------------
        var allNoFlyZones = db.getNoFlyZones(url);
        if (allNoFlyZones.size() == 0) {
            this.noFlyZones = new ArrayList<>();
        } else {
            this.noFlyZones = noFlyZonesIntersected(allNoFlyZones);
        }
        // ---------------------------------------------------------------------------------------


        double angle = 0;
        if (noFlyZones.size() == 0) { // no no-fly-zones on the way


            // Calculating angle alpha between startingPoint and endingPoint: --------------------
            if ((this.endingPoint.lng() != this.startingPoint.lng()) && (this.endingPoint.lat() != this.startingPoint.lat())) {
                var tan = Math.abs(this.endingPoint.lng() - this.startingPoint.lng()) / Math.abs(this.endingPoint.lat() - this.startingPoint.lat());
                if (this.endingPoint.lat() > this.startingPoint.lat() && this.endingPoint.lng() > this.startingPoint.lng()) {
                    //1st quartile
                    angle = (Math.atan(tan));
                } else if (this.endingPoint.lat() > this.startingPoint.lat() && this.endingPoint.lng() < this.startingPoint.lng()) {
                    //4th quartile
                    angle = 2 * Math.PI - (Math.atan(tan));
                } else if (this.endingPoint.lat() < this.startingPoint.lat() && this.endingPoint.lng() > this.startingPoint.lng()) {
                    //2nd quartile
                    angle = Math.PI - (Math.atan(tan));
                } else if (this.endingPoint.lat() < this.startingPoint.lat() && this.endingPoint.lng() < this.startingPoint.lng()) {
                    //3rd quartile
                    angle = Math.PI + (Math.atan(tan));
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
            // -----------------------------------------------------------------------------------

            // find how to express the line with directions we have: -----------------------------
            int index = (int) (angle / (Math.PI / 8));
            if (angle % (Math.PI / 8) == 0) {     // if angle coincides with one of the 16 main directions
                var direction = CompassDirection.values()[index];
                var distance = Math.sqrt(Math.pow(this.endingPoint.lat() - this.startingPoint.lat(), 2) + Math.pow(this.endingPoint.lng() - this.startingPoint.lng(), 2));
                this.path.add(this.startingPoint);
                for (int i = 0; i < (int) (distance / STEP_SIZE)-1; i++) {
                    this.path.add(this.path.get(this.path.size() - 1).nextPosition(direction));
                }
                this.path.add(this.endingPoint);
            } else {
                var closest = angle - ((Math.PI / 8) * index);
                if (closest > Math.PI / 16) {  // best direction index
                    if (CompassDirection.values()[index + 1] != CompassDirection.Hover) {
                        index = (index + 1);
                    } else {
                        index = 0;
                    }
                }

                this.directions.add(CompassDirection.values()[index]);
                this.movesTaken += 1;
                this.path.add(this.startingPoint);
                this.straightLineMapping(this.startingPoint.nextPosition(CompassDirection.values()[index])); //trigger a recursive function
            }
            // -----------------------------------------------------------------------------------


        } else {
            // Calculating path using the convex-hull method:


            List<LngLat> allPoints = noFlyZones.get(0).coordinates;
            for (int i = 1; i < noFlyZones.size(); i++) {
                allPoints.addAll(noFlyZones.get(i).coordinates);
            }

            var routes = convex_hull(allPoints); // returns 2 paths: "top" and "bottom" as they are referred to throughout
            var topRoute = routes.get(0);
            var bottomRoute = routes.get(1);
            var pathToTake = "";


            // Checking that half of NFZ is not outside of Central Area: -------------------------
                // This is quite computationally expensive - adds more than 20 seconds to the
                // running of the application
            var startInCentralArea = startingPoint.inCentralArea();
            var endInCentralArea = endingPoint.inCentralArea();
            double distanceTop = 0;
            double distanceBottom = 0;
            if (startInCentralArea == endInCentralArea) {
                for (int i = 0; i < bottomRoute.size() - 1; i++) {
                    if (bottomRoute.get(i).inCentralArea() != startInCentralArea) {
                        distanceBottom += bottomRoute.get(i).distanceTo(bottomRoute.get(i+1)); // two birds with one stone - calculating in case all the points meet the criteria of confinement of CA
                        pathToTake = "top";
                        break;
                    }
                }
                if (bottomRoute.get(bottomRoute.size()-1).inCentralArea() != startInCentralArea) {
                    pathToTake = "top";
                }
                if (!pathToTake.equals("top")) {
                    for (int i = 0; i < topRoute.size() - 1; i++) {
                        distanceTop += topRoute.get(i).distanceTo(topRoute.get(i+1));
                        if (topRoute.get(i).inCentralArea() != startInCentralArea) {
                            pathToTake = "bottom";
                            break;
                        }
                    }
                    if (topRoute.get(topRoute.size()-1).inCentralArea() != startInCentralArea) {
                        pathToTake = "top";
                    }
                }
            }


            // For faster calculation time:
            /*
            for (int i = 0; i < topRoute.size()-1; i++) {
                distanceTop += topRoute.get(i).distanceTo(topRoute.get(i+1));
            }
            for (int i = 0; i < bottomRoute.size()-1; i++) {
                distanceBottom += bottomRoute.get(i).distanceTo(bottomRoute.get(i+1));
            }
            */

            // -----------------------------------------------------------------------------------

            // If they are both satisfied by the confinements of the central area, we calculate which one is shorter:
            if (pathToTake.equals("")) {
                if (distanceTop <= distanceBottom) {
                    pathToTake = "top";
                } else {
                    pathToTake = "bottom";
                }
            }
            // -----------------------------------------------------------------------------------


            if (pathToTake.equals("top")) {

                this.getRoutePoint2Point(this.startingPoint, topRoute.get(1));

                for (int i = 1; i < topRoute.size() - 1; i++) {
                    LngLat temp = this.path.get(this.path.size()-1);
                    this.path.remove(this.path.get(this.path.size()-1));

                    this.getRoutePoint2Point(temp, topRoute.get(i+1));
                }
                this.path.remove(this.path.get(this.path.size()-1));
                this.path.add(this.endingPoint);
            } else {
                // take the bottom route

                this.getRoutePoint2Point(this.startingPoint, bottomRoute.get(1));
                for (int i = 1; i < bottomRoute.size() - 1; i++) {
                    LngLat temp = this.path.get(this.path.size()-1);
                    this.path.remove(this.path.get(this.path.size()-1));
                    this.getRoutePoint2Point(temp, bottomRoute.get(i+1));
                }
                this.path.remove(this.path.get(this.path.size()-1));
                this.path.add(this.endingPoint);
            }


        }

        // Adding a Hover to the end of the flight: --------------------------------
        this.path.add(this.endingPoint.nextPosition(CompassDirection.Hover));
        this.movesTaken += 1;
        this.directions.add(CompassDirection.Hover);
        // -------------------------------------------------------------------------
    }

    /**
     * The following function is triggered when we have no-fly-zones on the way.
     * It's a recursive function.
     * Start of by calculating the angle between the starting point and the ending point given to us like before.
     * Also like before, we do different checks in the angle and finding optimal CompassDirection for the next move.
     * Unlike before, we do extra checks to make sure our next step doesn't lead us into a no-fly-zone.
     * After everything has been calculated and checked, we call the function within itself with updated starting point
     * ( by using LngLat's nextPosition() ).
     *
     * @param startingPoint - where we start off. We don't use this.startingPoint here as we can be starting from one of the no-fly-zone points and heading to another
     * @param endingPoint - where we wish to finish. Reason for not using this.endingPoint is the same as the one above.
     */
    private void getRoutePoint2Point(LngLat startingPoint, LngLat endingPoint) {
        if (!startingPoint.closeTo(endingPoint)) {

            double angle = 0;
            if ((endingPoint.lng() != startingPoint.lng()) && (endingPoint.lat() != startingPoint.lat())) {
                var tan = Math.abs(endingPoint.lng() - startingPoint.lng()) / Math.abs(endingPoint.lat() - startingPoint.lat());
                if (endingPoint.lat() > startingPoint.lat() && endingPoint.lng() > startingPoint.lng()) {
                    //1st quartile
                    angle = (Math.atan(tan));
                } else if (endingPoint.lat() > startingPoint.lat() && endingPoint.lng() < startingPoint.lng()) {
                    //4th quartile
                    angle = 2 * Math.PI - (Math.atan(tan));
                } else if (endingPoint.lat() < startingPoint.lat() && endingPoint.lng() > startingPoint.lng()) {
                    //2nd quartile
                    angle = Math.PI - (Math.atan(tan));
                } else if (endingPoint.lat() < startingPoint.lat() && endingPoint.lng() < startingPoint.lng()) {
                    //3rd quartile
                    angle = Math.PI + (Math.atan(tan));
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


            int index = (int) (angle / (Math.PI / 8));
            if (angle % (Math.PI / 8) == 0) {

                var direction = CompassDirection.values()[index];

                var distance = Math.sqrt(Math.pow(this.endingPoint.lat() - startingPoint.lat(), 2) + Math.pow(this.endingPoint.lng() - startingPoint.lng(), 2));

                this.path.add(startingPoint);
                for (int i = 0; i < (int) (distance / STEP_SIZE); i++) {
                    this.path.add(this.path.get(this.path.size() - 1).nextPosition(direction));
                    this.directions.add(direction);
                    this.movesTaken += 1;
                }
            } else {
                var closest = angle - (Math.PI / 8 * index);
                if (closest > Math.PI / 16) {
                    int tempIndex;
                    if (CompassDirection.values()[index + 1] != CompassDirection.Hover) {
                        tempIndex = (index + 1);
                    } else {
                        tempIndex = 0;
                    }
                    if (!this.inNoFlyZone(startingPoint.nextPosition(CompassDirection.values()[tempIndex]))) {
                        index = tempIndex;
                    } // else : index stays as the 2nd best direction

                }

                this.path.add(startingPoint);
                this.directions.add(CompassDirection.values()[index]);
                this.movesTaken += 1;
                this.getRoutePoint2Point(startingPoint.nextPosition(CompassDirection.values()[index]), endingPoint);
            }
        } else {
            this.path.add(startingPoint);
        }
    }

    /**
     * Function dedicated to checking whether a given point is inside a No-Fly-Zone - similar to LngLat's inCentralArea().
     * @param point - LngLat point we want to check is inside a No-Fly-Zone
     * @return - true or false
     */
    private boolean inNoFlyZone(LngLat point) {

        Line2D l1 = new Line2D.Double(point.lng(), point.lat(), point.lng()+10, point.lat());

        for (int i = 0; i < this.noFlyZones.size(); i++) {
            int lineIntersect = 0;
            for (int j = 0; j < this.noFlyZones.get(i).coordinates.size(); j++) {
                Line2D l2 = new Line2D.Double(this.noFlyZones.get(i).coordinates.get(j).lng(), this.noFlyZones.get(i).coordinates.get(j).lat(), this.noFlyZones.get(i).coordinates.get((j+1)%this.noFlyZones.get(i).coordinates.size()).lng(), this.noFlyZones.get(i).coordinates.get((j+1)%this.noFlyZones.get(i).coordinates.size()).lat());
                if (l1.intersectsLine(l2)) {
                    lineIntersect ++;
                }
            }
            if (lineIntersect % 2 != 0) {
                return true;
            }
        }
        return false;

    }

    /**
     * The following function is triggered when we have NO no-fly-zones on the way.
     * Very similar to getRoutePoint2Point, except this time we use LngLat's closeTo() to check whether we have
     * arrived at this.endingPoint.
     * @param startingPoint
     */
    private void straightLineMapping(LngLat startingPoint) {

        if (startingPoint.closeTo(this.endingPoint)) {
            this.path.add(this.endingPoint);
        } else {

            double angle = 0;
            if ((this.endingPoint.lng() != startingPoint.lng()) && (this.endingPoint.lat() != startingPoint.lat())) {
                var tan = Math.abs(this.endingPoint.lng() - startingPoint.lng()) / Math.abs(this.endingPoint.lat() - startingPoint.lat());
                if (this.endingPoint.lat() > startingPoint.lat() && this.endingPoint.lng() > startingPoint.lng()) {
                    //1st quartile
                    angle = (Math.atan(tan));
                } else if (this.endingPoint.lat() > startingPoint.lat() && this.endingPoint.lng() < startingPoint.lng()) {
                    //4th quartile
                    angle = 2 * Math.PI - (Math.atan(tan));
                } else if (this.endingPoint.lat() < startingPoint.lat() && this.endingPoint.lng() > startingPoint.lng()) {
                    //2nd quartile
                    angle = Math.PI - (Math.atan(tan));
                } else if (this.endingPoint.lat() < startingPoint.lat() && this.endingPoint.lng() < startingPoint.lng()) {
                    //3rd quartile
                    angle = Math.PI + (Math.atan(tan));
                }
            } else {
                if (this.endingPoint.lng() == startingPoint.lng()) {
                    if (this.endingPoint.lat() > startingPoint.lat()) {
                        angle = 0;
                    } else {
                        angle = Math.PI;
                    }
                } else if (this.endingPoint.lat() == startingPoint.lat()) {
                    if (this.endingPoint.lng() > startingPoint.lng()) {
                        angle = Math.PI / 2;
                    } else {
                        angle = 3 * Math.PI / 2;
                    }
                }
            }


            int index = (int) (angle / (Math.PI / 8));
            if (angle % (Math.PI / 8) == 0) {

                var direction = CompassDirection.values()[index];

                var distance = Math.sqrt(Math.pow(this.endingPoint.lat() - startingPoint.lat(), 2) + Math.pow(this.endingPoint.lng() - startingPoint.lng(), 2));

                for (int i = 0; i < (int) (distance / STEP_SIZE)-1; i++) {
                    this.path.add(this.path.get(this.path.size() - 1).nextPosition(direction));
                }
                this.path.add(this.endingPoint);
            } else {
                var closest = angle - ((Math.PI / 8) * index);
                if (closest > Math.PI / 16) {
                    if (CompassDirection.values()[index + 1] != CompassDirection.Hover) {
                        index = (index + 1);
                    } else {
                        index = 0;
                    }
                }

                this.path.add(startingPoint);
                this.directions.add(CompassDirection.values()[index]);
                this.movesTaken += 1;
                this.straightLineMapping(startingPoint.nextPosition(CompassDirection.values()[index]));
            }
        }


    }

    /**
     * Function dedicated to finding the No-Fly-Zones that are in the way between this.startingPoint and this.endingPoint.
     * @param noFlyZones - a list of all the no-fly-zones pulled from the server.
     * @return - List of NoFlyZones that are in our way.
     */
    private List<NoFlyZone> noFlyZonesIntersected(List<NoFlyZone> noFlyZones) {
        List<NoFlyZone> onTheWay = new ArrayList<>();
        Line2D shortestLine = new Line2D.Double(this.startingPoint.lng(), this.startingPoint.lat(), this.endingPoint.lng(), this.endingPoint.lat());

        for (var noFlyZone : noFlyZones) {
            for (int i = 0; i < noFlyZone.coordinates.size(); i++) {
                Line2D line = new Line2D.Double(noFlyZone.coordinates.get(i).lng(), noFlyZone.coordinates.get(i).lat(), noFlyZone.coordinates.get((i + 1) % noFlyZone.coordinates.size()).lng(), noFlyZone.coordinates.get((i + 1) % noFlyZone.coordinates.size()).lat());

                if (line.intersectsLine(shortestLine)) {
                    onTheWay.add(noFlyZone);
                    break;
                }
            }
        }
        return onTheWay;
    }


    /**
     * Function responsible for calculating the points that outline the no-fly-zones that are in our way.
     * Uses a divide and conquer algorithm that has a time complexity of O(n).
     * @param points - all the points from all the no-fly-zones in the way.
     * @return - "top" and "bottom" routes that we can take to get from this.startingPoint to this.endingPoint
     * (Routes returned include this.startingPoint to this.endingPoint)
     */
    private List<List<LngLat>> convex_hull(List<LngLat> points) {
        List<List<LngLat>> routes = new ArrayList<>();

        List<LngLat> topRoute = new ArrayList<>();
        topRoute.add(this.startingPoint);


        List<LngLat> bottomRoute = new ArrayList<>();
        bottomRoute.add(this.startingPoint);

        topRoute.add(this.endingPoint);
        bottomRoute.add(this.endingPoint);

        // split into top and bottom:
        List<LngLat> allTopPoints = new ArrayList<>();
        List<LngLat> allBottomPoints = new ArrayList<>();

        Line2D baseLine = new Line2D.Double(this.startingPoint.lng(), this.startingPoint.lat(), this.endingPoint.lng(), this.endingPoint.lat());

        for (var point : points) {

            Line2D line = new Line2D.Double(point.lng(), point.lat(), point.lng(), point.lat()-10);

            if (line.intersectsLine(baseLine)) {
                allTopPoints.add(point);
            } else {
                allBottomPoints.add(point);
            }
        }

        while (allTopPoints.size() > 0) {

            var topPoint = allTopPoints.get(0);
            for (var point : allTopPoints) {
                if (point.lat() > topPoint.lat()) {
                    topPoint = point;
                }
            }

            // adding top points to topRoute in order of lng: ------
            for (int i = 0; i < topRoute.size(); i++) {

                var topRoutePoint = topRoute.get(i);
                if (this.startingPoint.lng() > this.endingPoint.lng()) {

                    // direction = left
                    if (topPoint.lng() >= topRoutePoint.lng()) {
                        topRoute.add(i, topPoint);
                        break;
                    }
                } else {
                    // direction = right

                    if (topPoint.lng() <= topRoutePoint.lng()) {
                        topRoute.add(i, topPoint);
                        break;
                    }
                }
            }
            // --------------------------------------------------------------

            List<LngLat> topTriangle = new ArrayList<>();
            topTriangle.add(this.startingPoint);
            topTriangle.add(this.endingPoint);
            topTriangle.add(topPoint);

            allTopPoints.remove(topPoint);

            List<LngLat> topPointsToRemove = new ArrayList<>();
            for (var point : allTopPoints) {
                // Setting each point with a line of (lng+10, lat)

                Line2D checkForIntersects = new Line2D.Double(point.lng(), point.lat(), point.lng()+10, point.lat());
                // Checking which ones are inside the top triangle:
                for (int i = 0; i < topTriangle.size(); i++) {

                    Line2D topTriangleLine = new Line2D.Double(topTriangle.get(i).lng(), topTriangle.get(i).lat(), topTriangle.get(i % topTriangle.size()).lng(), topTriangle.get(i % topTriangle.size()).lat());

                    if (topTriangleLine.intersectsLine(checkForIntersects)) {
                        topPointsToRemove.add(point);
                        break;
                    }
                }
            }
            for (var rem : topPointsToRemove) {
                allTopPoints.remove(rem);
            }
        }



        // Now the bottom points: -----------------------------------------------------------
        while (allBottomPoints.size() > 0) {
            var bottomPoint = allBottomPoints.get(0);
            for (var point : allBottomPoints) {
                if (point.lat() < bottomPoint.lat()) {
                    bottomPoint = point;
                }
            }


            // adding bottom points to bottomRoute in order of lng: ------
            for (int i = 0; i < bottomRoute.size(); i++) {
                var bottomRoutePoint = bottomRoute.get(i);
                if (this.startingPoint.lng() > this.endingPoint.lng()) {
                    // direction = left
                    if (bottomPoint.lng() >= bottomRoutePoint.lng()) {
                        bottomRoute.add(i, bottomPoint);
                        break;
                    }
                } else {
                    // direction = right
                    if (bottomPoint.lng() <= bottomRoutePoint.lng()) {
                        bottomRoute.add(i, bottomPoint);
                        break;
                    }
                }
            }
            // --------------------------------------------------------------

            List<LngLat> bottomTriangle = new ArrayList<>();
            bottomTriangle.add(this.startingPoint);
            bottomTriangle.add(this.endingPoint);
            bottomTriangle.add(bottomPoint);

            allBottomPoints.remove(bottomPoint);
            List<LngLat> bottomPointsToRemove = new ArrayList<>();
            for (var point : allBottomPoints) {
                // Setting each point with a line of (lng+10, lat)
                Line2D checkForIntersects = new Line2D.Double(point.lng(), point.lat(), point.lng()+10, point.lat());

                // Checking which ones are inside the top triangle:
                for (int i = 0; i < bottomTriangle.size(); i++) {
                    Line2D bottomTriangleLine = new Line2D.Double(bottomTriangle.get(i).lng(), bottomTriangle.get(i).lat(), bottomTriangle.get(i % bottomTriangle.size()).lng(), bottomTriangle.get(i % bottomTriangle.size()).lat());

                    if (bottomTriangleLine.intersectsLine(checkForIntersects)) {
                        bottomPointsToRemove.add(point);
                        break;
                    }
                }
            }
            for (var rem : bottomPointsToRemove) {
                allTopPoints.remove(rem);
            }
        }
        // ----------------------------------------------------------------------------------


        routes.add(topRoute);
        routes.add(bottomRoute);

        return routes;
    }

}
