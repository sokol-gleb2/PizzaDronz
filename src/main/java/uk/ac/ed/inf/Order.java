package uk.ac.ed.inf;

import java.util.*;

public class Order {

    Order(){}

    /**
     * This method gets the cost (in pence) of delivering the order provided as a parameter. It also includes
     * 1000p as a delivery fee. The method also does a check that it's a valid order (i.e. pizzas listed are
     * from the same restaurant.
     * <br>
     * Instead of working with an array of Restaurant objects and having to go through the entire array when
     * checking that each pizza is from the same restaurant, I've turned it into a HashMap with &#x0398(1)
     * for insertion and lookup. Each key (the restaurant) in the HashMap points to another HashMap of menus with the same
     * efficient time complexity. Therefore, making the lookup of <u>any</u> pizza from <u>any</u> restaurant a
     * function of <b>constant</b> time complexity.
     * <br>
     * Side note: It is possible to order same pizza more than once
     *
     * @param restaurants array of Restaurant objects
     * @param orders Strings of pizza names
     * @return cost of delivery in pence
     * @throws Exception InvalidPizzaCombinationException - if pizzas ordered don't come from the same restaurant
     * @throws Exception InvalidNumberOfPizzas - if there are more than 4 pizzas - as that's max number of pizzas a
     * drone can carry
     */

    public int getDeliveryCost(Restaurant[] restaurants, String...orders) throws Exception {
        // Order can have a max of 4 pizzas:
        if (orders.length > 4) {
            throw new Exception("InvalidNumberOfPizzas");
        }

        int cost = 0;

        // The following block of code turns Restaurant[] into HashMap<String, HashMap<String, Integer>>:
        String[] restaurantInCheck = new String[1]; // this variable is key for drastically reducing time complexity of search. Explains later its use
        HashMap<String, HashMap<String, Integer>> checkOrders = new HashMap<>();
        for (Restaurant restaurant : restaurants) {
            HashMap<String, Integer> prices = new HashMap<>();
            for (Menu menu : restaurant.menu) {
                prices.put(menu.name, menu.priceInPence);
                if (Objects.equals(orders[0], menu.name)) {
                    // The following lets the method know that the first pizza ordered on the list is from this restaurant.
                    // Afterwards when check whether the following pizzas came from the same place, we only need to check
                    // this one restaurant - which saves us a lot of time later down the line.
                    restaurantInCheck[0] = restaurant.name;

                    cost += menu.priceInPence;
                }
            }
            checkOrders.put(restaurant.name, prices);
        }

        // the following block of code goes through each pizza ordered and performs the combination check
        // if invalid, throws an exception
        for (int i = 1; i < orders.length; i++) {
            if (checkOrders.get(restaurantInCheck[0]).get(orders[i]) == null) {
                throw new Exception("InvalidPizzaCombinationException");
            }else {
                cost += checkOrders.get(restaurantInCheck[0]).get(orders[i]);
            }
        }

        // adding 100p for delivery
        cost += 100;
        return cost;
    }

}
