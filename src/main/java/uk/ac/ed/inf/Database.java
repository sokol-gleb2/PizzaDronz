/**
 * The following is a Singleton class made to connect to the RESTful API and retrieve data from there.
 *
 * It is a simple version - no muti-thread handling - as that reduces the performance because of the cost
 * associated with the synchronized method.
 */

package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static Database instance;

    Database(){}

    public static Database getInstance(){
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

}
