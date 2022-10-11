package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThreadSafeSingleton {
    private static ThreadSafeSingleton instance;

    ThreadSafeSingleton(){}

    public static synchronized ThreadSafeSingleton getInstance(){
        if(instance == null){
            instance = new ThreadSafeSingleton();
        }
        return instance;
    }

    public static Restaurant[] getResAndMenu(URL url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String jsonS = br.readLine();
            return new Gson().fromJson(jsonS, Restaurant[].class);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<LngLat> coordinates(String[] args) {
        List<LngLat> centralCoor = new ArrayList<>();


        try {
            String baseURL = args[0];
            String echoBasis = args [1];
            if (! baseURL.endsWith("/")){
                baseURL += "/";
            }

            URL url = new URL(baseURL + echoBasis);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            JsonArray readerG = new JsonParser().parse(br.readLine()).getAsJsonArray();
            for (int i = 0; i < readerG.size(); i++) {
                JsonObject coor = (JsonObject) readerG.get(i);
                LngLat centralPoint = new LngLat(coor.get("longitude").getAsDouble(), coor.get("latitude").getAsDouble());
                centralCoor.add(centralPoint);

            }

//            JSONArray reader = new JSONArray(br.readLine());
//            for (int i = 0; i < reader.length(); i++) {
//                JSONObject coor = reader.getJSONObject(i);
//                LngLat centralPoint = new LngLat(Double.parseDouble(coor.get("longitude").toString()), Double.parseDouble(coor.get("latitude").toString()));
//                centralCoor.add(centralPoint);
//            }




            conn.disconnect();

        }catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return centralCoor;
    }

}
