package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * PizzaDronz App
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        // code here:
//        Database db = new Database();
//        URL url = new URL("https://ilp-rest.azurewebsites.net/orders");
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        Date date = simpleDateFormat.parse("2023-01-04");
//        var orders = db.getOrders(url, date);
////        for (var order: orders) {
////            order.orderChecks();
////            System.out.println("/n");
////        }
//        URL url_restaurants = new URL("https://ilp-rest.azurewebsites.net/restaurants");
//        var restaurants = Restaurant.getRestaurantsFromRestServer(url_restaurants);
////        for (var order : orders) {
////            if (order.getPriceTotalInPence() == order.getDeliveryCost(restaurants, order.getOrderItems())) {
////                System.out.println("true");
////            }else {
////                System.out.println(order.getPriceTotalInPence());
////                System.out.println(order.getDeliveryCost(restaurants, order.getOrderItems()));
////                System.out.println(order.getOrderOutcome());
////            }
////        }
//        for (var order : orders) {
//            if (!order.orderChecks()) {
//                System.out.println(order.orderOutcome);
//            }else {
//                Flight flight = new Flight(order.restaurantCoor, new LngLat(-3.186874, 55.944494));
//                flight.doSomething();
//            }
//        }
//
//
//        System.out.println("coor:");
//        URL noFlyZonesURL = new URL("https://ilp-rest.azurewebsites.net/noFlyZones");
//        var noFlyZones = db.getNoFlyZones(noFlyZonesURL);
//        for (var noFlyZone : noFlyZones) {
//            System.out.println(noFlyZone.getName());
//            System.out.println(noFlyZone.getCoordinates());
//        }


        URL noFlyZonesURL = new URL("https://ilp-rest.azurewebsites.net");
        Flight flight = new Flight(new LngLat(3.00, 0.00), new LngLat(3.0003, 0.0003));
        flight.calculatePath("https://ilp-rest.azurewebsites.net");
        System.out.println(flight.path);




    }
}
