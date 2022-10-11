package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws MalformedURLException {
        LngLat lngLat = new LngLat(-3.19, 55.942617);
        System.out.println(lngLat.inCentralArea());
        System.out.println(lngLat.distanceTo(new LngLat(-3.192473, 55.946233)));
        System.out.println(lngLat.closeTo(new LngLat(-3.19, 55.9426)));
        URL url = new URL("https://ilp-rest.azurewebsites.net/restaurants");
        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(url);
        for (Restaurant restaurant : restaurants) {
            System.out.println(restaurant.name);
            List<Menu> menus = restaurant.getMenu();
            for (Menu menu: menus) {
                System.out.println(Arrays.toString(menu.getMenu()));
            }
        }
    }
}
