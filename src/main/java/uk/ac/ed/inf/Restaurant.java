/**
 * Class responsible for giving structure to the Restaurants pulled from the server.
 * Each Restaurant is made up of name, location (longitude, latitude) and list of pizzas of offer (Menu[])
 */

package uk.ac.ed.inf;

import java.net.URL;

public class Restaurant {
    String name;
    double longitude;
    double latitude;
    Menu[] menu;

    public Restaurant(String name, double longitude, double latitude, Menu[] menu) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.menu = menu;
    }


    public Menu[] getMenu() {
        return this.menu;
    }

    static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) {
        return Database.getResAndMenu(serverBaseAddress);
    }

}
