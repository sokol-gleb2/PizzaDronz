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
        return menu;
    }

    static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) {
        return Database.getResAndMenu(serverBaseAddress);
    }

}
