package uk.ac.ed.inf;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    //{"name":"Civerinos Slice",
    // "longitude":-3.1912869215011597,
    // "latitude":55.945535152517735,
    // "menu":[{"name":"Margarita","priceInPence":1000},{"name":"Calzone","priceInPence":1400}]}
    String name;
    double longitude;
    double latitude;
    List<Menu> menu;
//    public static class Menu {
//        String name;
//        String priceInPence;
//    }


    public List<Menu> getMenu() {
        return menu;
    }

    static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) {
        return ThreadSafeSingleton.getResAndMenu(serverBaseAddress);
    }

}
