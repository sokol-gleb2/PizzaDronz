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
//        System.out.println("Line intersect" + lineIntersect);
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

    public LngLat nextPosition(CompassDirection compassDirection) {
        double newLng;
        double newLat;
        int index = compassDirection.ordinal(); // gets the index of the selected enum from all the enums i.e. CompassDirection.North.ordinal() = 0;
        double angle = index*Math.PI/6;

//        North,
//                NorthNorthEast,
//                NorthEast,
//                EastNorthEast,
//                East,
//                EastSouthEast,
//                SouthEast,
//                SouthSouthEast,
//                South,
//                SouthSouthWest,
//                SouthWest,
//                WestSouthWest,
//                West,
//                WestNorthWest,
//                NorthWest,
//                NorthNorthWest
//        if (compassDirection == CompassDirection.North){
//
//        }else if (compassDirection == CompassDirection.NorthNorthEast) {
//
//        }else if (compassDirection == CompassDirection.NorthEast) {
//
//        }else if (compassDirection == CompassDirection.EastNorthEast) {
//
//        }else if (compassDirection == CompassDirection.East) {
//
//        }else if (compassDirection == CompassDirection.EastSouthEast) {
//
//        }else if (compassDirection == CompassDirection.SouthEast) {
//
//        }else if (compassDirection == CompassDirection.SouthSouthEast) {
//
//        }else if (compassDirection == CompassDirection.South) {
//
//        }else if (compassDirection == CompassDirection.SouthSouthWest) {
//
//        }else if (compassDirection == CompassDirection.SouthWest) {
//
//        }else if (compassDirection == CompassDirection.WestSouthWest) {
//
//        }else if (compassDirection == CompassDirection.West) {
//
//        }
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
