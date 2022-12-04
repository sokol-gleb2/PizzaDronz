package uk.ac.ed.inf;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * PizzaDronz App
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {

        long timeAtStart = System.nanoTime();

//        String dateGiven = args[0];
//        String url = args[1];
        String dateGiven = "2023-01-12";
        String url = "https://ilp-rest.azurewebsites.net/";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date date = simpleDateFormat.parse(dateGiven);

            Date lastDatePossible = simpleDateFormat.parse("2023-05-30");
            Date firstDatePossible = simpleDateFormat.parse("2023-01-01");
            if (date.after(lastDatePossible)) {
                throw new Exception("Date is out of bounds. No orders past 2023-05-30");
            } else if (date.before(firstDatePossible)) {
                throw new Exception("Date is out of bounds. No orders before 2023-01-01");
            }

            try {
                Drone drone = new Drone(date, url);

                JsonArray jsonOrders = new JsonArray();
                JsonArray jsonFlights = new JsonArray();
                List<Double[]> droneSteps = new ArrayList<>();


                int tickCounter = 0;
                for (int i = 0; i < drone.ordersForTheDay.size(); i++) {
                    var order = drone.ordersForTheDay.get(i);
                    JsonObject jsonOrder = new JsonObject();
                    jsonOrder.addProperty("orderNo", order.orderNo);
                    jsonOrder.addProperty("outcome", order.orderOutcome.toString());
                    jsonOrder.addProperty("costInPence", order.priceTotalInPence);
                    jsonOrders.add(jsonOrder);


                    var flight = drone.flightsForTheDay.get(i);
                    int tick = (int) ((drone.ticks.get(i)/(2*flight.get(0).movesTaken))/1000000); // turn into milliseconds

                    for (int thereAndBack = 0; thereAndBack < 2; thereAndBack++) {
                        for (int j = 0; j < flight.get(thereAndBack).path.size()-1; j++) {
                            Double[] coordinates = new Double[2];
                            var point = flight.get(thereAndBack).path.get(j);
                            var next_point = flight.get(thereAndBack).path.get(j+1);
                            JsonObject jsonFlight = new JsonObject();
                            jsonFlight.addProperty("orderNo", order.orderNo);
                            jsonFlight.addProperty("fromLongitude", point.lng());
                            jsonFlight.addProperty("fromLatitude", point.lat());
                            String angle = "";
                            if (flight.get(thereAndBack).directions.get(j) == CompassDirection.Hover) {
                                angle = Double.toString(Double.NaN);
                            } else {
                                angle = Double.toString(flight.get(thereAndBack).directions.get(j).ordinal()*Math.PI/8);
                            }
                            jsonFlight.addProperty("angle", angle);
                            jsonFlight.addProperty("toLongitude", next_point.lng());
                            jsonFlight.addProperty("toLatitude", next_point.lat());
                            tickCounter += tick;
                            jsonFlight.addProperty("ticksSinceStartOfCalculation", tickCounter);
                            jsonFlights.add(jsonFlight);




                            coordinates[0] = point.lng();
                            coordinates[1] = point.lat();
                            droneSteps.add(coordinates);

                        }

                        Double[] coordinates = new Double[2];
                        coordinates[0] = flight.get(thereAndBack).path.get(flight.get(thereAndBack).path.size()-1).lng();
                        coordinates[1] = flight.get(thereAndBack).path.get(flight.get(thereAndBack).path.size()-1).lat();
                        droneSteps.add(coordinates);

                    }

                }
                for (var cancelledOrder : drone.cancelledOrders) {
                    JsonObject jsonOrder = new JsonObject();
                    jsonOrder.addProperty("orderNo", cancelledOrder.orderNo);
                    jsonOrder.addProperty("outcome", cancelledOrder.orderOutcome.toString());
                    jsonOrder.addProperty("costInPence", cancelledOrder.priceTotalInPence);
                    jsonOrders.add(jsonOrder);
                }
                try {

                    String deliveriesFileName = "deliveries-"+dateGiven+".json";
                    String flightpathFileName = "flightpath-"+dateGiven+".json";
                    String droneFileName = "drone-"+dateGiven+".geojson";

                    FileWriter deliveriesFileWriter = new FileWriter(deliveriesFileName);
                    deliveriesFileWriter.write(jsonOrders.toString());
                    deliveriesFileWriter.close();

                    FileWriter flightFileWriter = new FileWriter(flightpathFileName);
                    flightFileWriter.write(jsonFlights.toString());
                    flightFileWriter.close();


                    // writing GeoJSON: -------------------------------------------
                    FileWriter droneFileWriter = new FileWriter(droneFileName);
                    JsonObject droneMove = new JsonObject();
                    droneMove.addProperty("type", "FeatureCollection");
                    JsonArray features = new JsonArray();

                    JsonObject feature = new JsonObject();
                    feature.addProperty("type", "Feature");
                    feature.addProperty("properties", "{}");

                    JsonObject geometryJson = new JsonObject();
                    geometryJson.addProperty("type", "LineString");
                    StringBuilder DS = new StringBuilder();
                    for (var step : droneSteps) {
                        DS.append("[").append(step[0]).append(",").append(step[1]).append("], ");
                    }

                    geometryJson.addProperty("coordinates", DS.substring(0, DS.length()-2));

                    feature.addProperty("geometry", geometryJson.toString());

                    features.add(feature);

                    droneMove.addProperty("features", features.toString());
                    droneFileWriter.write(droneMove.toString());
                    droneFileWriter.close();
                    // ------------------------------------------------------------


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (RuntimeException e) {
                throw new Exception("URL is not formed correctly. Please check it and try again");
            }


        } catch (ParseException e) {
            throw new Exception("Date of wrong format. Please format it like so: YYYY-MM-DD");
        }


        long timeAtEnd = System.nanoTime();
        long timeTaken = timeAtEnd - timeAtStart;
        System.out.println("The code took " + TimeUnit.SECONDS.convert(timeTaken, TimeUnit.NANOSECONDS) + "seconds to execute.");


    }
}
