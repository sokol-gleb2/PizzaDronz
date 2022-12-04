package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Order {
    //{"orderNo":"55724045",
    // "orderDate":"2023-01-01",
    // "customer":"Damian Boscarello",
    // "creditCardNumber":"4855200933750832",
    // "creditCardExpiry":"05/27",
    // "cvv":"001",
    // "priceTotalInPence":2600,
    // "orderItems":["Meat Lover","Vegan Delight"]}

    public OrderOutcome orderOutcome;
    public String orderNo;
    public String orderDate;
    public String customer;
    public String creditCardNumber;
    private String creditCardExpiry;
    private String cvv;
    int priceTotalInPence;
    String[] orderItems;
    LngLat restaurantCoor;



    Order(String orderNo, String orderDate, String customer, String creditCardNumber, String creditCardExpiry, String cvv, int priceTotalInPence, String[] orderItems) throws ParseException {
//        this.orderOutcome = orderOutcome;
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.customer = customer;
        this.creditCardNumber = creditCardNumber;
//        this.creditCardExpiry = simpleDateFormat.parse(creditCardExpiry);
        this.creditCardExpiry = creditCardExpiry;
        this.cvv = cvv;
        this.priceTotalInPence = priceTotalInPence;
        this.orderItems = orderItems;
    }

    public String[] getOrderItems() {
        return this.orderItems;
    }

    public int getPriceTotalInPence() {
        return this.priceTotalInPence;
    }

    public OrderOutcome getOrderOutcome() {
        return this.orderOutcome;
    }

    public LngLat getRestaurantCoor() {
        return this.restaurantCoor;
    }

    public boolean orderChecks() throws Exception {
        // Checking expiry date is after order date: -----------------------------
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/yy");
        SimpleDateFormat expiryDateFormat = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat orderDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Getting the last day of month to check expiry:
        Calendar calendar = Calendar.getInstance();
        Date expiry = simpleDateFormat.parse(this.creditCardExpiry);
        calendar.setTime(expiry);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Creating a new Date with the last day of month that we got above
        Date fullExpiry = expiryDateFormat.parse(maxDay+"/"+this.creditCardExpiry);

        if (!fullExpiry.after(orderDateFormat.parse(this.orderDate))) {
            this.orderOutcome = OrderOutcome.InvalidExpiryDate;
            return false;
        }
        // -----------------------------------------------------------------------

        // Checking CVV: ---------------------------------------------------------
        if (this.cvv.length() != 3 && this.cvv.length() != 4) {
            this.orderOutcome = OrderOutcome.InvalidCvv;
            return false;
        }
        // Checking each character is a number
        for (int i = 0; i < this.cvv.length(); i++) {
            if (!Character.isDigit(this.cvv.charAt(i))) {
                this.orderOutcome = OrderOutcome.InvalidCvv;
                return false;
            }
        }
        // -----------------------------------------------------------------------

        // Checking delivery cost and pizza validity: ----------------------------
        URL url_restaurants = new URL("https://ilp-rest.azurewebsites.net/restaurants");
        var restaurants = Restaurant.getRestaurantsFromRestServer(url_restaurants);
        var deliveryCost = getDeliveryCost(restaurants, this.orderItems);
        if (deliveryCost != 0 && deliveryCost != this.priceTotalInPence) {
            this.orderOutcome = OrderOutcome.InvalidTotal;
            return false;
        } else if (deliveryCost == 0) {
            return false;
        }
        // -----------------------------------------------------------------------

        // Checking card number: -------------------------------------------------
        // Visa's begin with 4 ; MC's begin with 5
        // length = 16
        // Luhn Algorithm: https://en.wikipedia.org/wiki/Luhn_algorithm
        //|| !this.creditCardNumber.startsWith("4") || !this.creditCardNumber.startsWith("5")
        if (this.creditCardNumber.length() != 16) {
            this.orderOutcome = OrderOutcome.InvalidCardNumber;
            return false;
        } else {
            int sum = 0;
            int parity = 16%2;
            for (int i =1; i < 17; i++) {
                if (i%2 == parity) {
                    sum += Integer.parseInt(String.valueOf(this.creditCardNumber.charAt(i-1)));
                }else if (Integer.parseInt(String.valueOf(this.creditCardNumber.charAt(i-1))) > 4) {
                    sum += 2*Integer.parseInt(String.valueOf(this.creditCardNumber.charAt(i-1))) - 9;
                }else {
                    sum += 2*Integer.parseInt(String.valueOf(this.creditCardNumber.charAt(i-1)));
                }
            }

            if (sum%10 != 0) {
                this.orderOutcome = OrderOutcome.InvalidCardNumber;
                return false;
            }
        }
        // -----------------------------------------------------------------------

        // if all are correct then we set:
        this.orderOutcome = OrderOutcome.ValidButNotDelivered;

        return true;
    }

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

    public int getDeliveryCost(Restaurant[] restaurants, String[] orders) throws Exception {
        // Order can have a max of 4 pizzas:
        if (orders.length > 4 || orders.length == 0) {
            this.orderOutcome = OrderOutcome.InvalidPizzaCount;
            return 0;
        }

        int cost = 0;

        // The following block of code turns Restaurant[] into HashMap<String, HashMap<String, Integer>>:
        String[] restaurantInCheck = new String[1]; // this variable is key for drastically reducing time complexity of search. Explains later its use
//        LngLat restaurantCoor; // for more efficient
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
                    this.restaurantCoor = new LngLat(restaurant.longitude, restaurant.latitude);
                    cost += menu.priceInPence;
                }
            }
            checkOrders.put(restaurant.name, prices);
        }

        // the following block of code goes through each pizza ordered and performs the combination check
        // if invalid, throws an exception
        for (int i = 1; i < orders.length; i++) {
            if (checkOrders.get(restaurantInCheck[0]).get(orders[i]) == null) {
//                throw new Exception("InvalidPizzaCombinationException");
                this.orderOutcome = OrderOutcome.InvalidPizzaCombinationMultipleSuppliers;
                return 0;
            }else {
                cost += checkOrders.get(restaurantInCheck[0]).get(orders[i]);
            }
        }

        // adding 100p for delivery
        cost += 100;
        return cost;
    }

}
