package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;

public record LngLat(double lng, double lat) {
    public boolean inCentralArea() {
        ThreadSafeSingleton threadSafeSingleton = new ThreadSafeSingleton();
        String[] args = {"https://ilp-rest.azurewebsites.net/", "centralArea"};
        var centralCoor = threadSafeSingleton.coordinates(args);

        int lineIntersect = 0;
        List<LngLat> line1 = new ArrayList<>();
        line1.add(this);
        line1.add(new LngLat(this.lng+10, this.lat));
        List<LngLat> line2 = new ArrayList<>();
        for (int i = 0; i < centralCoor.size(); i++) {
            line2.add(centralCoor.get(i));
            line2.add(centralCoor.get((i+1)%centralCoor.size()));
            if (this.intersect(line1, line2)) {
                lineIntersect ++;
            }
            line2.clear();
        }
//        line2.add(centralCoor.get(centralCoor.size()-1));
//        line2.add(centralCoor.get(0));
//        if (this.intersect(line1, line2)) {
//            System.out.println("+1");
//            lineIntersect ++;
//        }
        System.out.println("Line intersect" + lineIntersect);
        return lineIntersect % 2 != 0;
    }

    private boolean intersect(List<LngLat> line1, List<LngLat> line2) {
        //check if the point in ON the line:
        double gradient2 = (line2.get(1).lat - line2.get(0).lat)/(line2.get(1).lng - line2.get(0).lng);
        double intersect2 = line2.get(0).lat - (line2.get(0).lng * gradient2);
        if (line1.get(0).lng * gradient2 + intersect2 == line1.get(0).lat) {
            return true;
        }
        if (line1.get(0).lng < line2.get(0).lng || line1.get(0).lng < line2.get(1).lng) {
            return (line1.get(0).lat < line2.get(0).lat && line1.get(0).lat > line2.get(1).lat) ||
                    (line1.get(0).lat > line2.get(0).lat && line1.get(0).lat < line2.get(1).lat);
        }

        return false;
    }

    public double distanceTo(LngLat targetPoint) {
        return Math.pow(Math.pow((targetPoint.lng - this.lng),2) + Math.pow((targetPoint.lat - this.lat), 2), 0.5);
    }

    public boolean closeTo(LngLat l2) {
        // NOTE: Think about double and all that nonesense i.e. is this accurate??
        return (this.distanceTo(l2) < 0.00015);
    }

    public LngLat nextPosition(int compassDirection) {
        int angle;
        double newLng;
        double newLat;
        if (compassDirection < 90) {
            angle = 90 - compassDirection;
            newLng = this.lng + Math.acos(angle)*0.00015;
            newLat = this.lat + Math.asin(angle)*0.00015;
        } else if (compassDirection < 180) {
            angle = compassDirection - 90;
            newLng = this.lng + Math.acos(angle)*0.00015;
            newLat = this.lat - Math.asin(angle)*0.00015;
        } else if (compassDirection < 270) {
            angle = 270 - compassDirection;
            newLng = this.lng - Math.acos(angle)*0.00015;
            newLat = this.lat - Math.asin(angle)*0.00015;
        } else {
            angle = compassDirection - 270;
            newLng = this.lng - Math.acos(angle)*0.00015;
            newLat = this.lat + Math.asin(angle)*0.00015;
        }

        return new LngLat(newLng, newLat);



    }
}
