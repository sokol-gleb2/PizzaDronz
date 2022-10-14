package uk.ac.ed.inf;

import java.util.*;

public class Order {

    public int getDeliveryCost(Restaurant[] restaurants, String...orders) throws Exception {
        // Q : Does orderOutcome need an enum??

        int cost = 0;
        // need to find an efficient way to check that orders are from the same restaurant
        // dictionary (Map<String, int>): HashMap <-- O(1) for insertion and lookup
        // HashTable = thread-safe and can be shared between multiple threads in the application.
        // HashMap vs TreeMap vs LinkedHashMap = https://www.tutorialspoint.com/Difference-between-TreeMap-HashMap-and-LinkedHashMap-in-Java#:~:text=HashMap%20has%20complexity%20of%20O,key%20and%20multiple%20null%20values.

        String[] restaurantInCheck = new String[1];
        HashMap<String, HashMap<String, Integer>> checkOrders = new HashMap<>();
        for (Restaurant restaurant : restaurants) {
            HashMap<String, Integer> prices = new HashMap<>();
            for (Menu menu : restaurant.menu) {
                prices.put(menu.name, menu.priceInPence);
                if (Objects.equals(orders[0], menu.name)) {
                    restaurantInCheck[0] = restaurant.name;
                    cost += menu.priceInPence;
                }
            }
            checkOrders.put(restaurant.name, prices);
        }

        for (int i = 1; i < orders.length; i++) {
            if (checkOrders.get(restaurantInCheck[0]).get(orders[i]) == null) {
                throw new Exception("InvalidPizzaCombinationException");
            }else {
                cost += checkOrders.get(restaurantInCheck[0]).get(orders[i]);
            }
        }

        // adding 1000p for delivery
        cost += 1;
        return cost;
    }

}
