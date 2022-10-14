/**
 *
 * This record is used to represent coordinates
 * Takes in (longitude, latitude) - both doubles
 *
 */


package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public record LngLat(double lng, double lat) {

    /**
     * This method calculates whether the current LngLat record (point on the map) is
     * within the "Central Area".
     * This method calls the Singleton class and passes the URL as parameter. It gets back
     * the coordinates for the Central Area (<b>we don't assume there are 4 - it could be a polygon
     * of any shape</b>).
     * The method then proceeds to calculate whether the point is inside the polygon using the
     * following technique:
     * <ol>
     *     <li>
     *         Draw a horizontal line from the current point (make sure it's long enough to
     *         go through the entire polygon).
     *         <ul>
     *             <li>
     *                 In this instance the length is 10 deg. - plenty given that we focus on area
     *                 whose longitude is bounded by (-4, -3)
     *             </li>
     *         </ul>
     *      </li>
     *      <li>
     *          Calculate the number of intersects made by the line
     *      </li>
     *      <li>
     *          If the number of intersects is divisible by , return false; else - true
     *      </li>
     * </ol>
     *
     * <br>
     * A trick case: if the current point is on the border - the default would return false.
     * Therefore, the method undergoes a special check inside intersect().
     *
     * Time complexity of the computations depends on the number of edges in the polygon.
     * Hence, ~ &#x0398(n).
     * We say space complexity is constant because of line2.clear() command (~ &#x0398(1))
     *
     * @return whether current point is inside the Central Area
     * @exception MalformedURLException to deal with URL
     *
     */
    public boolean inCentralArea() throws MalformedURLException {
        Database database = new Database();
        URL url = new URL("https://ilp-rest.azurewebsites.net/centralArea");
        var centralCoor = database.coordinates(url);

        int lineIntersect = 0;
        List<LngLat> line1 = new ArrayList<>();
        line1.add(this);
        line1.add(new LngLat(this.lng+10, this.lat));
        List<LngLat> line2 = new ArrayList<>();
        for (int i = 0; i < centralCoor.size(); i++) {
            line2.add(centralCoor.get(i));
            line2.add(centralCoor.get((i+1)%centralCoor.size())); //this was added to also include the line that connect last and first point of centralCoor list
            if (this.intersect(line1, line2)) {
                lineIntersect ++;
            }
            line2.clear(); //clearing so we can use it again from blank
        }
        return lineIntersect % 2 != 0;
    }

    /**
     * This is a private method made primarily to aid the inCentralArea() method. It calculates whether
     * there is an intersect between 2 straight lines.
     * <br>
     * As mentioned before, this method also checks for the special case that our current point (line1.get(0)) is ON line2.
     * If line2 has equation: y2 = m2*x2 + c2, then if our current point (x1, y1) satisfies y1 = m2*x1 + c2, then we can
     * return true.
     * <br>
     * If current point is not on line2, then we proceed with finding if there is an intersect.
     * Instead of taking the mathematical approach of finding the line equation for both lines, then equating and seeing
     * if there are common (x, y) coordinates, I chose a less computationally expensive method of checking the position
     * of the current point in comparison to the 2 points that make up line2. Looking at the code:
     * <br>
     * First if statement checks that <b>lng</b> of our current point (CP) comes before <b>lng</b> of the other 2 points.
     * <ul>
     *     <li>
     *         If yes, we proceed to check that the <b>lat</b> of CP is between the <b>lat</b> of 2 other points.
     *         <ul>
     *             <li>If holds, return true.</li>
     *             <li>If not - false.</li>
     *         </ul>
     *     </li>
     *     <li>
     *         If no, return false.
     *     </li>
     * </ul>
     *
     * This is a computationally efficient method with constant time complexity ~ &#x0398(1)
     *
     * @param line1 2 points making up a line - given as a list of LngLat records of length 2
     * @param line2 2 points making up a line - given as a list of LngLat records of length 2
     * @return true if line1 and line2 intersect; false - otherwise
     */
    private boolean intersect(List<LngLat> line1, List<LngLat> line2) {
        //check if the point in ON the line:
        double gradient2 = (line2.get(1).lat - line2.get(0).lat)/(line2.get(1).lng - line2.get(0).lng);
        double intersect2 = line2.get(0).lat - (line2.get(0).lng * gradient2);
        if (line1.get(0).lng * gradient2 + intersect2 == line1.get(0).lat) {
            return true;
        }
        if (line1.get(0).lng < line2.get(0).lng || line1.get(0).lng < line2.get(1).lng) { //checking lng is in the right range
            return (line1.get(0).lat < line2.get(0).lat && line1.get(0).lat > line2.get(1).lat) ||
                    (line1.get(0).lat > line2.get(0).lat && line1.get(0).lat < line2.get(1).lat); //checking lat is in the right range and returning the truth value
        }

        return false;
    }





    /**
     *
     * Calculates Pythagorean distance between current point and a point given as parameter.
     *
     * @param targetPoint point of interest
     * @return Pythagorean distance between CP and targetPoint
     */
    public double distanceTo(LngLat targetPoint) {
        // Uses the math library for Math.pow(x, a) = x^a
        return Math.pow(Math.pow((targetPoint.lng - this.lng),2) + Math.pow((targetPoint.lat - this.lat), 2), 0.5);
    }

    public boolean closeTo(LngLat l2) {
        return (this.distanceTo(l2) < 0.00015);
    }




    /**
     *
     * This method calculates the coordinates of the drone after a new move.
     * This method takes enum CompassDirection as direction of the next move. Using enum - limits the choices to the ones
     * allowed.
     *
     * First line also tells you that Hovering has been taken into account as a move - the coordinated don't change.
     *
     * @param compassDirection enum CompassDirection
     * @return LngLat object with the updated coordinates - depending on the move chosen
     */
    public LngLat nextPosition(CompassDirection compassDirection) {
        if (compassDirection == CompassDirection.Hover) {return new LngLat(this.lng, this.lat);} // hovering = no change
        double newLng;
        double newLat;
        int index = compassDirection.ordinal(); // gets the index of the selected enum from all the enums i.e. CompassDirection.North.ordinal() = 0;
        double angle = index*Math.PI/8; // 16 main compass direction, therefore, each direction = 2*pi/16 = pi/8 rad


        // The following block of code uses simple geometry to calculate lengths of a right angle triangle given an
        // angle and hypotenuse length (= 0.00015 deg as specified in the doc).
        // The reason there's if statement for every pi/2 angle is because signs differ depending on which quadrant
        // we are dealing with.
        if (angle <= Math.PI/2) {
            double angleForCalc = Math.PI/2 - angle;
            newLng = this.lng + Math.cos(angleForCalc)*0.00015;
            newLat = this.lat + Math.sin(angleForCalc)*0.00015;
        } else if (angle <= Math.PI) {
            double angleForCalc = Math.PI - angle;
            newLng = this.lng + Math.sin(angleForCalc)*0.00015;
            newLat = this.lat - Math.cos(angleForCalc)*0.00015;
        } else if (angle <= Math.PI*1.5) {
            double angleForCalc = Math.PI*1.5 - angle;
            newLng = this.lng - Math.cos(angleForCalc)*0.00015;
            newLat = this.lat - Math.sin(angleForCalc)*0.00015;
        } else {
            double angleForCalc = Math.PI*2 - angle;
            newLng = this.lng - Math.sin(angleForCalc)*0.00015;
            newLat = this.lat + Math.cos(angleForCalc)*0.00015;
        }
        return new LngLat(newLng, newLat);

    }
}
