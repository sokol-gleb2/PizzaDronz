package uk.ac.ed.inf;

public class Order {

    public int getDeliveryCost(Restaurant[] restaurants, String[] orders) {
        int cost = 0;
        // need to find an efficient way to check that orders are from the same restaurant
        // dictionary (Map<String, int>): HashMap <-- O(1) for insertion and lookup
        // HashTable = thread-safe and can be shared between multiple threads in the application.
        // HashMap vs TreeMap vs LinkedHashMap = https://www.tutorialspoint.com/Difference-between-TreeMap-HashMap-and-LinkedHashMap-in-Java#:~:text=HashMap%20has%20complexity%20of%20O,key%20and%20multiple%20null%20values.


        for (String order : orders) {

        }
        return cost;
    }

}
