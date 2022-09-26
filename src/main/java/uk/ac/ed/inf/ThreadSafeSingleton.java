package uk.ac.ed.inf;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

    public List<Double> coordinates(String[] args) {
        List<Double> centralCoor = new ArrayList<>();
        centralCoor.add(2.33);
        centralCoor.add(2.33);
        centralCoor.add(2.33);
        centralCoor.add(2.33);

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

//            System.out.println("Output from Server .... \n");
//            while ((output = br.readLine()) != null) {
//                System.out.println(output);
//            }
//            String reader = br.readLine();
//            String newStr = reader.replace("[", "");
//            String finalReader = newStr.replace("]", "");
//            List<JSONObject> centralCoordinates = new ArrayList<JSONObject>(Arrays.asList(finalReader.split(",")));
            JSONArray reader = new JSONArray(br.readLine());
            for (int i = 0; i < reader.length(); i++) {
                JSONObject coor = reader.getJSONObject(i);
                System.out.println(coor.get("latitude"));
            }

//            for (Object jo : reader) {
//                System.out.println(jo);
//                JSONObject jsonObject = new JSONObject(jo);
//                System.out.println(jsonObject);
////                System.out.println(jsonObject.get("latitude"));
//            }



//            JSONObject central = new JSONObject(br.readLine());
//            System.out.println(central);


            conn.disconnect();

        }catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return centralCoor;
    }

}
