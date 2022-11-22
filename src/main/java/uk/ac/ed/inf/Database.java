/**
 * The following is a Singleton class made to connect to the RESTful API and retrieve data from there.
 *
 * It is a simple version - no muti-thread handling - as that reduces the performance because of the cost
 * associated with the synchronized method.
 */

package uk.ac.ed.inf;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Database {
    private static Database instance;

    Database(){}
    // syncronized so only one thread is allowed
    public static synchronized Database getInstance(){
        if(instance == null){
            instance = new Database();
        }
        return instance;
    }

    /**
     *
     * This method gets the restaurant and menu data from the REST API.
     * It uses the standard libraries HttpURLConnection and BufferedReader to communicate with
     * the server.
     * Outside library is Gson - JSON parser created by Google.
     * A useful feature in Gson is deserialisation of JSON into Objects - as we can see being done by .fromJSON()
     *
     * @param url that returns JSON of Restaurant names, locations, and their menus
     * @return array of Restaurants stores on the server
     */
    public static Restaurant[] getResAndMenu(URL url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) { //can't connect
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String jsonS = br.readLine();
            return new Gson().fromJson(jsonS, Restaurant[].class); //deserialisation


        } catch (IOException e) {
            throw new RuntimeException(e); //can't connect
        }
    }

    /**
     *
     * This method get the coordinates of Central Area from REST API.
     * It uses the same libraries as mentioned before.
     *
     * @param url that returns JSON of Central Area coordinates
     * @return ArrayList of LngLat objects corresponding to each point of the polygon that makes Central Area
     */
    public List<LngLat> coordinates(URL url) {
        List<LngLat> centralCoor = new ArrayList<>();


        try {

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) { //can't connect
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            // turns data into JsonArray:
            JsonArray readerG = new JsonParser().parse(br.readLine()).getAsJsonArray();

            // Goes through JsonArray and gets the lng and lat of each point
            for (int i = 0; i < readerG.size(); i++) {
                JsonObject coor = (JsonObject) readerG.get(i);
                LngLat centralPoint = new LngLat(coor.get("longitude").getAsDouble(), coor.get("latitude").getAsDouble());
                centralCoor.add(centralPoint);

            }

            conn.disconnect();

        }catch (MalformedURLException e) { //invalid URL
            e.printStackTrace();
        } catch (IOException e) { //can't connect
            throw new RuntimeException(e);
        }

        return centralCoor;
    }


    public List<Order> getOrders(URL url, Date dateGiven) {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) { //can't connect
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            List<Order> orders = new ArrayList<>();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String jsonS = br.readLine();
            JsonArray readerG = JsonParser.parseString(jsonS).getAsJsonArray();

            for (int i = 0; i < readerG.size(); i++) {
                JsonObject object = (JsonObject) readerG.get(i);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = dateFormat.parse(object.get("orderDate").getAsString());
                if (date.equals(dateGiven)) {
                    orders.add(new Gson().fromJson(object, Order.class));
                }
            }

            return orders;



        } catch (IOException e) {
            throw new RuntimeException(e); //can't connect
        } catch (ParseException e) {
            throw new RuntimeException(e); // date exception
        }
    }

    public List<NoFlyZone> getNoFlyZones(URL url) {

        List<NoFlyZone> noFlyZones = new ArrayList<>();


        try {

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) { //can't connect
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            // turns data into JsonArray:
            String jsonS = br.readLine();
            JsonArray readerG = JsonParser.parseString(jsonS).getAsJsonArray();
            for (int i = 0; i < readerG.size(); i++) {
                List<LngLat> tempCoor = new ArrayList<>();
                JsonObject object = (JsonObject) readerG.get(i);
                var zoneCoordinates = object.get("coordinates");
                for (var point : zoneCoordinates.getAsJsonArray()) {
                    LngLat c = new LngLat(point.getAsJsonArray().get(0).getAsDouble(), point.getAsJsonArray().get(1).getAsDouble());
                    tempCoor.add(c);
                }
                var name = object.get("name").getAsString();
                var noFlyZone = new NoFlyZone(name, tempCoor);
                noFlyZones.add(noFlyZone);
//                tempCoor.clear();

            }


            conn.disconnect();

        }catch (MalformedURLException e) { //invalid URL
            e.printStackTrace();
        } catch (IOException e) { //can't connect
            throw new RuntimeException(e);
        }

        return noFlyZones;
    }


}
